package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.base.BaseGamepadScreen;
import szm.orde4c.game.base.TileMapActor;
import szm.orde4c.game.effect.ExplosionEffect;
import szm.orde4c.game.entity.*;
import szm.orde4c.game.entity.stationary.Area;
import szm.orde4c.game.entity.stationary.Environment;
import szm.orde4c.game.entity.stationary.Solid;
import szm.orde4c.game.entity.submarine.*;
import szm.orde4c.game.ui.AttributeBar;
import szm.orde4c.game.ui.Direction;
import szm.orde4c.game.ui.MenuLabel;
import szm.orde4c.game.ui.PauseMenu;
import szm.orde4c.game.util.AreaObjectType;
import szm.orde4c.game.util.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class LevelScreen extends BaseGamepadScreen {
    private PlayerInfo[] playerInfos;
    private Submarine submarine;
    private float submarineStartX;
    private float submarineStartY;
    AttributeBar healthBar;
    AttributeBar shieldBar;
    AttributeBar energyBar;
    Fish fish;

    PauseMenu pauseMenu;


    public LevelScreen(PlayerInfo[] playerInfos) {
        super();
        this.playerInfos = playerInfos;
        submarine.initializePlayers(playerInfos);


        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(submarine);
    }

    @Override
    public void initialize() {
        loadMap();
        loadSubmarine();
        initializeUI();

        fish = new Fish(800, 1000, mainStage);

    }


    private void initializeUI() {
        healthBar = new AttributeBar(1, Color.RED, Submarine::getHealthPercent, uiStage);
        shieldBar = new AttributeBar(3, Color.CYAN, Submarine::getShieldPercent, uiStage);
        energyBar = new AttributeBar(1, Color.BLUE, Submarine::getEnergyPercent, uiStage);

        uiTable.pad(50);
        uiTable.add(healthBar).expandX().left().pad(30);
        uiTable.row();
        uiTable.add(shieldBar).expandX().left().pad(30);
        uiTable.row();
        uiTable.add(energyBar).expand().top().left().pad(30);

        ArrayList<MenuLabel> menuOptions = new ArrayList<>();
        MenuLabel loadGame = new MenuLabel("Exit to Main Menu", BaseGame.largeLabelStyle) {
            @Override
            public void execute() {
                BaseGame.setActiveScreen(new MainMenuScreen());
            }
        };
        menuOptions.add(loadGame);

        MenuLabel exit = new MenuLabel("Exit to Desktop", BaseGame.largeLabelStyle) {
            @Override
            public void execute() {
                Gdx.app.exit();
            }
        };
        menuOptions.add(exit);

        pauseMenu = new PauseMenu(menuOptions, 0.5f, uiStage);
        pauseMenu.setVisible(false);

    }

    private void updateUI() {
        healthBar.update(submarine);
        shieldBar.update(submarine);
        energyBar.update(submarine);
    }

    @Override
    public void update(float dt) {

        if (paused) {
            return;
        }

        for (BaseActor torpedoActor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Torpedo")) {
            if (((new Vector2(torpedoActor.getX(), torpedoActor.getY())).sub(submarine.getX() + submarine.getOriginX(), submarine.getY() + submarine.getOriginY())).len() > 6000) {
                torpedoActor.remove();
                continue;
            }
            List<BaseActor> actors = BaseActor.getList(mainStage, "szm.orde4c.game.entity.stationary.Environment");
            actors.addAll(BaseActor.getList(mainStage, "szm.orde4c.game.entity.Drillable"));
            for (BaseActor environmentActor : actors) {
                if (torpedoActor.overlaps(environmentActor)) {

                    BaseActor explosion = new BaseActor(0, 0, mainStage);
                    explosion.loadTexture("platform.png");
                    explosion.setSize(300, 300);
                    explosion.setBoundaryPolygon(10);
                    Vector2 rotatedTorpedoEnd = new Vector2(torpedoActor.getWidth(), torpedoActor.getHeight() / 2.0f).rotate(torpedoActor.getRotation());
                    explosion.centerAtPosition(torpedoActor.getX() + rotatedTorpedoEnd.x, torpedoActor.getY() + rotatedTorpedoEnd.y);
                    explosion.setOpacity(0.5f);


                    ExplosionEffect boom = new ExplosionEffect();
                    boom.centerAtPosition(torpedoActor.getX() + rotatedTorpedoEnd.x, torpedoActor.getY() + rotatedTorpedoEnd.y);
                    boom.start();
                    mainStage.addActor(boom);

                    torpedoActor.remove();

                    for (BaseActor otherEnvironmentActor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Drillable")) {
                        if (explosion.overlaps(otherEnvironmentActor)) {
                            otherEnvironmentActor.remove();
                        }
                    }

                    Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
                    if (Intersector.overlapConvexPolygons(submarine.getBoundaryPolygon(), explosion.getBoundaryPolygon(), mtv)) {
                        float explosionRadius = explosion.getWidth() / 2.0f;
                        float overlapAmount = mtv.depth;

                        float damage = 11 + 22 / explosionRadius * overlapAmount;
                        submarine.damage(MathUtils.floor(damage));
                    }

                    explosion.addAction(Actions.delay(1.0f));
                    explosion.addAction(Actions.after(Actions.fadeOut(2.0f)));
                }
            }
        }

        submarine.alignCamera();

//        fish.makeMove();
        updateUI();

        for (BaseActor actor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Damageable")) {
            if (((Damageable) actor).isDestroyed()) {
                actor.remove();
            }
        }
    }

    private void loadSubmarine() {
        TileMapActor tileMapActor = new TileMapActor("submarine/submarine.tmx", mainStage);
        Polygon hitbox = ((PolygonMapObject) tileMapActor.getPolygonList("Hitbox").get(0)).getPolygon();

        submarine = new Submarine(submarineStartX, submarineStartY, hitbox, mainStage);
        submarine.setBoundaryPolygonOffsetX(hitbox.getX());
        submarine.setBoundaryPolygonOffsetY(hitbox.getY());

        // Load submarine walls/floor
        for (MapObject solidObject : tileMapActor.getRectangleList("Solid")) {
            MapProperties solidObjectProperties = solidObject.getProperties();
            float solidObjectX = (float) solidObjectProperties.get("x");
            float solidObjectY = (float) solidObjectProperties.get("y");
            float solidObjectWidth = (float) solidObjectProperties.get("width");
            float solidObjectHeight = (float) solidObjectProperties.get("height");
            float solidObjectRotation = 0;
            try {
                solidObjectRotation = -(float) solidObjectProperties.get("rotation");
            } catch (Exception e) {
            }
            Solid solid = new Solid(solidObjectX, solidObjectY, solidObjectWidth, solidObjectHeight, solidObjectRotation, mainStage);
            submarine.addActor(solid);
        }

        for (MapObject solidObject : tileMapActor.getPolygonList("Solid")) {
            PolygonMapObject solidPolygonObject = (PolygonMapObject) solidObject;
            MapProperties solidPolygonProperties = solidObject.getProperties();
            float solidObjectX = (float) solidPolygonProperties.get("x");
            float solidObjectY = (float) solidPolygonProperties.get("y");
            Polygon solidObjectBoundaryPolygon = solidPolygonObject.getPolygon();

            Solid solid = new Solid(solidObjectX, solidObjectY, solidObjectBoundaryPolygon, mainStage);
            submarine.addActor(solid);
        }

        // Load submarine ladders
        for (MapObject ladderObject : tileMapActor.getRectangleList("Ladder")) {
            MapProperties ladderObjectProperties = ladderObject.getProperties();
            float ladderObjectX = (float) ladderObjectProperties.get("x");
            float ladderObjectY = (float) ladderObjectProperties.get("y");
            float ladderObjectWidth = (float) ladderObjectProperties.get("width");
            float ladderObjectHeight = (float) ladderObjectProperties.get("height");
            Ladder ladder = new Ladder(ladderObjectX, ladderObjectY, ladderObjectWidth, ladderObjectHeight, mainStage);
            submarine.addActor(ladder);
        }

        for (MapObject object : tileMapActor.getRectangleList("Elevator")) {
            MapProperties elevatorProps = object.getProperties();

            float elevatorX = (float) elevatorProps.get("x");
            float elevatorY = (float) elevatorProps.get("y");
            float elevatorWidth = (float) elevatorProps.get("width");
            float elevatorHeight = (float) elevatorProps.get("height");
            String elevatorGroup = (String) elevatorProps.get("group");
            int elevatorId = Integer.parseInt((String) elevatorProps.get("id"));
            Elevator elevator = new Elevator(elevatorX, elevatorY, elevatorWidth, elevatorHeight, mainStage);

            boolean existingGroup = false;
            for (BaseActor elevatorGroupActor : BaseActor.getList(submarine, "szm.orde4c.game.entity.submarine.ElevatorGroup")) {
                ElevatorGroup group = (ElevatorGroup) elevatorGroupActor;
                if (group.getGroup().equals(elevatorGroup)) {
                    existingGroup = true;
                    group.addElevator(elevatorId, elevator);
                }
            }
            if (!existingGroup) {
                ElevatorGroup newElevatorGroup = new ElevatorGroup((String) elevatorProps.get("group"), mainStage);
                newElevatorGroup.addElevator(elevatorId, elevator);
                submarine.addActor(newElevatorGroup);
            }
            submarine.addActor(elevator);
        }

        for (MapObject stationObject : tileMapActor.getRectangleList("Station")) {
            MapProperties stationProperties = stationObject.getProperties();
            String stationType = (String) stationProperties.get("type");
            try {
                if (stationType.equals("Engine")) {
                    addEngineStation(stationProperties);
                } else if (stationType.equals("Arm")) {
                    addArmStation(stationProperties, tileMapActor);
                } else if (stationType.equals("Torpedo")) {
                    addTorpedoStation(stationProperties, tileMapActor);
                } else if (stationType.equals("Shield")) {
                    addShieldStation(stationProperties);
                } else if (stationType.equals("Reflector")) {
                    // TODO add reflector station
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (MapObject object : tileMapActor.getRectangleList("Start")) {
            MapProperties props = object.getProperties();
            submarine.addPlayerStartPosition(new Vector2((float) props.get("x"), (float) props.get("y")));
        }
        submarine.setPosition(submarineStartX, submarineStartY);
    }

    private void addEngineStation(MapProperties engineStationProperties) {
        float stationX = (float) engineStationProperties.get("x");
        float stationY = (float) engineStationProperties.get("y");
        float stationWidth = (float) engineStationProperties.get("width");
        float stationHeight = (float) engineStationProperties.get("height");
        new EngineStation(stationX, stationY, stationWidth, stationHeight, submarine, mainStage);
    }

    private void addArmStation(MapProperties armStationProperties, TileMapActor tileMapActor) {
        String armId = (String) armStationProperties.get("id");
        float stationX = (float) armStationProperties.get("x");
        float stationY = (float) armStationProperties.get("y");
        float stationWidth = (float) armStationProperties.get("width");
        float stationHeight = (float) armStationProperties.get("height");


        MapProperties armProperties = tileMapActor.getRectangleList(armId).get(0).getProperties();
        float armX = (float) armProperties.get("x");
        float armY = (float) armProperties.get("y");
        Direction armDirection = Direction.valueOf((String) armProperties.get("direction"));

        Arm arm = new Arm(armX, armY, armDirection, mainStage);

        new ArmStation(stationX, stationY, stationWidth, stationHeight, arm, submarine, mainStage);
    }

    private void addTorpedoStation(MapProperties torpedoStationProperties, TileMapActor tileMapActor) {
        String torpedoStartId = (String) torpedoStationProperties.get("id");
        float stationX = (float) torpedoStationProperties.get("x");
        float stationY = (float) torpedoStationProperties.get("y");
        float stationWidth = (float) torpedoStationProperties.get("width");
        float stationHeight = (float) torpedoStationProperties.get("height");

        MapProperties torpedoStartProps = tileMapActor.getRectangleList(torpedoStartId).get(0).getProperties();
        float torpedoStartX = (float) torpedoStartProps.get("x");
        float torpedoStartY = (float) torpedoStartProps.get("y");

        new TorpedoStation(stationX, stationY, stationWidth, stationHeight, torpedoStartX, torpedoStartY, submarine, mainStage);
    }

    private void addShieldStation(MapProperties shieldStationProperties) {
        float stationX = (float) shieldStationProperties.get("x");
        float stationY = (float) shieldStationProperties.get("y");
        float stationWidth = (float) shieldStationProperties.get("width");
        float stationHeight = (float) shieldStationProperties.get("height");

        new ShieldStation(stationX, stationY, stationWidth, stationHeight, submarine, mainStage);
    }

    private void loadMap() {
        TileMapActor tileMapActor = new TileMapActor("map.tmx", mainStage);
        BaseActor map = new BaseActor(0, 0, mainStage);
        map.loadTexture("map.png");
        BaseActor.setWorldBounds(map);

        for (MapObject environmentObject : tileMapActor.getPolygonList("Environment")) {
            PolygonMapObject environmentPolygonObject = (PolygonMapObject) environmentObject;
            MapProperties environmentPolygonProperties = environmentPolygonObject.getProperties();

            float environmentX = (float) environmentPolygonProperties.get("x");
            float environmentY = (float) environmentPolygonProperties.get("y");
            Polygon environmentPolygon = environmentPolygonObject.getPolygon();

            new Environment(environmentX, environmentY, environmentPolygon, mainStage);

        }
        for (MapObject areaObject : tileMapActor.getRectangleList("Area")) {
            MapProperties areaProperties = areaObject.getProperties();

            float areaX = (float) areaProperties.get("x");
            float areaY = (float) areaProperties.get("y");
            float areaWidth = (float) areaProperties.get("width");
            float areaHeight = (float) areaProperties.get("height");
            float areaRotation = 0;
            AreaObjectType areaObjectType = AreaObjectType.valueOf(((String) areaProperties.get("objectType")).toUpperCase());
            int areaObjectCount = Integer.parseInt((String) areaProperties.get("objectCount"));
            try {
                areaRotation = -(float) areaProperties.get("rotation");
            } catch (Exception e) {
                // Area rotation not specified
            }

            new Area(areaObjectType, areaX, areaY, areaWidth, areaHeight, areaRotation, areaObjectCount, mainStage);
        }

        MapProperties submarineStartProperties = tileMapActor.getRectangleList("SubmarineStart").get(0).getProperties();
        submarineStartX = (float) submarineStartProperties.get("x");
        submarineStartY = (float) submarineStartProperties.get("y");
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            if (paused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
        return false;
    }

    @Override
    public void connected(Controller controller) {
        super.connected(controller);
    }

    @Override
    public void disconnected(Controller controller) {
        super.disconnected(controller);
    }

    public void pauseGame() {
        paused = true;
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(submarine);
        im.addProcessor(pauseMenu);
        pauseMenu.setVisible(true);
    }

    public void resumeGame() {
        paused = false;
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(pauseMenu);
        im.addProcessor(submarine);
        pauseMenu.setVisible(false);
    }

    @Override
    public void pause() {
        super.pause();
        pauseGame();
    }

    @Override
    public void hide() {
        super.hide();
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(pauseMenu);
        im.removeProcessor(submarine);
    }

    @Override
    public void show() {
        super.show();
        if (paused) {
            InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
            im.removeProcessor(pauseMenu);
            im.addProcessor(submarine);
        }
    }

    @Override
    public void resume() {
    }
}
