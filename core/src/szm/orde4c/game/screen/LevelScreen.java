package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import szm.orde4c.game.base.*;
import szm.orde4c.game.entity.*;
import szm.orde4c.game.entity.stationary.Area;
import szm.orde4c.game.entity.stationary.Environment;
import szm.orde4c.game.entity.stationary.Geyser;
import szm.orde4c.game.entity.submarine.*;
import szm.orde4c.game.service.SaveGameService;
import szm.orde4c.game.ui.*;
import szm.orde4c.game.util.AreaObjectType;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.PlayerInfo;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;
import java.util.List;

public class LevelScreen extends BaseGamepadScreen {
    private boolean paused;

    private static final int MAXIMUM_ENEMY_COUNT = 4;
    private static final float ENEMY_SPAWN_COOLDOWN = 60;
    private float enemySpawnCooldown = 0;
    private List<Vector2> possibleEnemySpawns;

    private PlayerInfo[] playerInfos;
    private Submarine submarine;

    private float submarineStartX;
    private float submarineStartY;

    private int currentLevelIndex;
    private Save currentSave;
    private List<AttributeBar> attributeBars;

    private BaseActor goal;
    private TextDisplayScreen levelCompleteScreen;
    private TextDisplayScreen defeatScreen;

    private PauseMenu pauseMenu;

    public LevelScreen(int currentLevelIndex, Save save, PlayerInfo[] playerInfos) {
        super();
        this.currentLevelIndex = currentLevelIndex;
        this.playerInfos = playerInfos;
        this.currentSave = save;
        paused = false;
        initializeAfterConstructor();
    }

    @Override
    public void initialize() {
        levelCompleteScreen = new TextDisplayScreen("Szint teljesítve!", Color.FOREST, 0.5f, uiStage);
        defeatScreen = new TextDisplayScreen("Vereség!", Color.FIREBRICK, 1, uiStage);
    }

    private void initializeAfterConstructor() {
        loadLevel();
        submarine = new Submarine(submarineStartX, submarineStartY, mainStage);
        Controllers.addListener(submarine);
        submarine.initializePlayers(playerInfos);
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(submarine);
        initializeUI();
    }

    private void loadLevel() {
        String currentLevelName = String.format("level%d", currentLevelIndex);
        TileMapActor tileMapActor = new TileMapActor(String.format("level/%s/%s%s", currentLevelName, currentLevelName, ".tmx"));
        BaseActor mapBackground = new BaseActor(0, 0, mainStage);
        mapBackground.loadTexture(Assets.instance.getTexture(String.format("level/%s/%s%s", currentLevelName, currentLevelName, "background.jpg")));
        BaseActor.setWorldBounds(mapBackground);

        loadLevelEnvironments(tileMapActor);
        loadLevelAreas(tileMapActor);
        loadSubmarineStart(tileMapActor);
        loadLevelGoal(tileMapActor);
        loadLevelGeysers(tileMapActor);
        loadLevelEnemySpawnPositions(tileMapActor);

        BaseActor mapForeground = new BaseActor(0, 0, mainStage);
        mapForeground.loadTexture(Assets.instance.getTexture(String.format("level/%s/%s%s", currentLevelName, currentLevelName, "foreground.png")));
    }

    private void initializeUI() {
        attributeBars = new ArrayList<>();
        attributeBars.add(new AttributeBar(1, "Élet: ", Color.RED, Submarine::getHealthPercent, uiStage));
        attributeBars.add(new AttributeBar(3, "Pajzs: ", Color.CYAN, Submarine::getShieldPercent, uiStage));
        attributeBars.add(new AttributeBar(1, "Energia: ", Color.BLUE, Submarine::getEnergyPercent, uiStage));

        uiTable.pad(5);
        for (AttributeBar attributeBar : attributeBars.subList(0, 2)) {
            uiTable.add(attributeBar).expandX().left().top().pad(2);
            uiTable.row();
        }
        uiTable.add(attributeBars.get(2)).expand().top().left().pad(2);

        ArrayList<MenuLabel> menuOptions = new ArrayList<>();
        MenuLabel loadGame = new MenuLabel("Kilépés a menübe", BaseGame.labelStyle) {
            @Override
            public void execute() {
                submarine.levelFinished();
                BaseGame.setActiveScreen(new MainMenuScreen());
            }
        };
        menuOptions.add(loadGame);

        MenuLabel exit = new MenuLabel("Kilépés az asztalra", BaseGame.labelStyle) {
            @Override
            public void execute() {
                Gdx.app.exit();
            }
        };
        menuOptions.add(exit);

        pauseMenu = new PauseMenu(menuOptions, Assets.BLANK, 0.5f, uiStage);
        pauseMenu.setVisible(false);
    }

    private void updateUI() {
        for (AttributeBar attributeBar : attributeBars) {
            attributeBar.update(submarine);
        }
    }

    @Override
    public void render(float dt) {
        if (!paused) {
            mainStage.act(dt);
        }
        uiStage.act(dt);

        update(dt);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mainStage.getBatch().setBlendFunction(GL20.GL_SRC_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA);
        mainStage.draw();
        uiStage.draw();
    }

    @Override
    public void update(float dt) {

        if (paused || submarine.getStage() == null) {
            return;
        }

        for (BaseActor torpedoActor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Torpedo")) {
            if (((new Vector2(torpedoActor.getX(), torpedoActor.getY())).sub(submarine.getX() + submarine.getOriginX(), submarine.getY() + submarine.getOriginY())).len() > mainStage.getWidth() * 2) {
                torpedoActor.remove();
                continue;
            }
            List<BaseActor> actors = BaseActor.getList(mainStage, "szm.orde4c.game.entity.stationary.Environment");
            actors.addAll(BaseActor.getList(mainStage, "szm.orde4c.game.entity.Damageable"));
            for (BaseActor environmentActor : actors) {
                if (torpedoActor.overlaps(environmentActor)) {

                    BaseActor explosion = new BaseActor(0, 0, mainStage);
                    explosion.setSize(300, 300);
                    explosion.setBoundaryPolygon(10);
                    explosion.setVisible(false);
                    Vector2 rotatedTorpedoEnd = new Vector2(torpedoActor.getWidth(), torpedoActor.getHeight() / 2.0f).rotate(torpedoActor.getRotation());
                    explosion.centerAtPosition(torpedoActor.getX() + rotatedTorpedoEnd.x, torpedoActor.getY() + rotatedTorpedoEnd.y);
                    torpedoActor.remove();

                    for (BaseActor otherEnvironmentActor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Damageable")) {
                        if (explosion.overlaps(otherEnvironmentActor)) {
                            otherEnvironmentActor.remove();
                        }
                    }

                    if (submarine.overlaps(explosion)) {
                        submarine.damage(20);
                    }
                }
            }
        }
        submarine.boundToWorld();
        submarine.alignCamera();

        updateUI();

        for (BaseActor actor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Damageable")) {
            if (((Damageable) actor).isDestroyed()) {
                if (!(actor instanceof Submarine)) {
                    actor.remove();
                }
            }
        }

        if (submarine.overlaps(goal)) {
            levelComplete();
        }


        for (BaseActor projectileActor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Projectile")) {
            for (BaseActor armActor : BaseActor.getList(submarine, "szm.orde4c.game.entity.submarine.Arm")) {
                if (armActor.overlaps(projectileActor)) {
                    projectileActor.remove();
                    break;
                }
            }
            if (projectileActor.getStage() == null) {
                continue;
            }
            if (projectileActor.overlaps(submarine)) {
                submarine.damage(10);
                projectileActor.remove();
                continue;
            }
            List<BaseActor> collisionActors = BaseActor.getList(mainStage, "szm.orde4c.game.entity.stationary.Environment");
            collisionActors.addAll(BaseActor.getList(mainStage, "szm.orde4c.game.entity.stationary.Rock"));
            collisionActors.addAll(BaseActor.getList(mainStage, "szm.orde4c.game.entity.stationary.Vegetation"));
            for (BaseActor environmentActor : collisionActors) {
                if (environmentActor.overlaps(projectileActor)) {
                    projectileActor.remove();
                    break;
                }
            }
        }

        processEnemies(dt);

        if (submarine.isDestroyed() || submarine.getEnergy() <= 0) {
            levelLost();
        }
    }

    public void levelComplete() {
        currentSave.setCompletedLevels(currentLevelIndex);
        SaveGameService.save(currentSave);
        levelCompleteScreen.setVisible(true);
        submarine.addAction(Actions.delay(2f));
        submarine.addAction(Actions.after(new Action() {
            @Override
            public boolean act(float delta) {
                submarine.levelFinished();
                BaseGame.setActiveScreen(new LoadGameScreen());
                return true;
            }
        }));
    }

    private void levelLost() {
        defeatScreen.setVisible(true);
        submarine.addAction(Actions.delay(2f));
        submarine.addAction(Actions.after(new Action() {
            @Override
            public boolean act(float delta) {
                submarine.levelFinished();
                BaseGame.setActiveScreen(new LoadGameScreen());
                return true;
            }
        }));
    }

    private void processEnemies(float delta) {
        enemySpawnCooldown -= delta;
        if (enemySpawnCooldown <= 0 && BaseActor.count(mainStage, "szm.orde4c.game.entity.Enemy") < MAXIMUM_ENEMY_COUNT) {
            for (Vector2 position : possibleEnemySpawns) {
                Vector2 cameraPosition = new Vector2(mainStage.getCamera().position.x, mainStage.getCamera().position.y);
                Vector2 camerPositionToSpawnPosition = position.cpy().sub(cameraPosition);
                if (camerPositionToSpawnPosition.len() > mainStage.getCamera().viewportWidth / 2f + 200 && camerPositionToSpawnPosition.len() < mainStage.getWidth()) {
                    Enemy enemy = new Enemy(0, 0, mainStage);
                    enemy.centerAtPosition(position.x, position.y);
                    enemySpawnCooldown = ENEMY_SPAWN_COOLDOWN;
                    return;
                }
            }
        }
    }

    private void loadLevelEnvironments(TileMapActor tileMapActor) {
        for (MapObject environmentObject : tileMapActor.getPolygonList("Environment")) {
            PolygonMapObject environmentPolygonObject = (PolygonMapObject) environmentObject;
            MapProperties environmentPolygonProperties = environmentPolygonObject.getProperties();
            float environmentX = (float) environmentPolygonProperties.get("x");
            float environmentY = (float) environmentPolygonProperties.get("y");
            Polygon environmentPolygon = environmentPolygonObject.getPolygon();

            new Environment(environmentX, environmentY, environmentPolygon, mainStage);
        }
    }

    private void loadLevelAreas(TileMapActor tileMapActor) {
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
    }

    private void loadSubmarineStart(TileMapActor tileMapActor) {
        MapProperties submarineStartProperties = tileMapActor.getRectangleList("SubmarineStart").get(0).getProperties();
        submarineStartX = (float) submarineStartProperties.get("x");
        submarineStartY = (float) submarineStartProperties.get("y");
    }

    private void loadLevelGoal(TileMapActor tileMapActor) {
        MapProperties goalProperties = tileMapActor.getRectangleList("Goal").get(0).getProperties();
        float goalX = (float) goalProperties.get("x");
        float goalY = (float) goalProperties.get("y");
        float goalWidth = (float) goalProperties.get("width");
        float goalHeight = (float) goalProperties.get("height");
        goal = new BaseActor(goalX, goalY, mainStage);
        goal.setSize(goalWidth, goalHeight);
        goal.setBoundaryRectangle();
        goal.setVisible(false);
    }

    private void loadLevelGeysers(TileMapActor tileMapActor) {
        for (MapObject geyserObject : tileMapActor.getRectangleList("Geyser")) {
            MapProperties geyserProperties = geyserObject.getProperties();
            float geyserX = (float) geyserProperties.get("x");
            float geyserY = (float) geyserProperties.get("y");
            float geyserWidth = (float) geyserProperties.get("width");
            float geyserHeight = (float) geyserProperties.get("height");
            new Geyser(geyserX, geyserY, geyserWidth, geyserHeight, mainStage);
        }
    }

    private void loadLevelEnemySpawnPositions(TileMapActor tileMapActor) {
        possibleEnemySpawns = new ArrayList<>();
        for(MapObject enemySpawnObject : tileMapActor.getRectangleList("EnemySpawn")) {
            MapProperties enemySpawnProperties = enemySpawnObject.getProperties();
            float enemySpawnX = (float) enemySpawnProperties.get("x");
            float enemySpawnY = (float) enemySpawnProperties.get("y");
            float enemySpawnWidth = (float) enemySpawnProperties.get("width");
            float enemySpawnHeight = (float) enemySpawnProperties.get("height");
            float enemySpawnOriginX = enemySpawnX + enemySpawnWidth / 2f;
            float enemySpawnOriginY = enemySpawnY + enemySpawnHeight / 2f;
            possibleEnemySpawns.add(new Vector2(enemySpawnOriginX, enemySpawnOriginY));
        }
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
        if (!paused) {
            paused = true;
            InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
            im.removeProcessor(submarine);
            im.addProcessor(0, pauseMenu);
            pauseMenu.setVisible(true);
        }
    }

    public void resumeGame() {
        if (paused) {
            paused = false;
            InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
            im.removeProcessor(pauseMenu);
            im.addProcessor(submarine);
            pauseMenu.setVisible(false);
        }
    }

    @Override
    public void pause() {
        pauseGame();
    }

    @Override
    public void hide() {
        super.hide();
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(pauseMenu);
        im.removeProcessor(submarine);
        submarine.levelFinished();
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
