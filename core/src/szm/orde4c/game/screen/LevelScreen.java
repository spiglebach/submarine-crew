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
import szm.orde4c.game.effect.GeyserBubblingEffect;
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
    private static final int MAXIMUM_ENEMY_COUNT = 4;
    private static final float ENEMY_SPAWN_COOLDOWN = 30;
    private float enemySpawnCooldown = 0;
    private List<Vector2> possibleEnemySpawns;

    private PlayerInfo[] playerInfos;
    private int currentLevelIndex;
    private Save save;

    private Submarine submarine;
    private float submarineStartX;
    private float submarineStartY;

    private List<AttributeBar> attributeBars;
    private BaseActor goalIndicator;
    private BaseActor goal;
    private boolean paused;
    private PauseMenu pauseMenu;

    public LevelScreen(int currentLevelIndex, Save save, PlayerInfo[] playerInfos) {
        super();
        this.currentLevelIndex = currentLevelIndex;
        this.playerInfos = playerInfos;
        this.save = save;
        paused = false;
        initializeAfterConstructor();
    }

    @Override
    public void initialize() {
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
        mapBackground.loadTexture(Assets.instance.getTexture(String.format("level/%s/%s%s", currentLevelName, currentLevelName, ".jpg")));
        BaseActor.setWorldBounds(mapBackground);

        loadLevelEnvironments(tileMapActor);
        loadLevelAreas(tileMapActor);
        loadLevelSubmarineStart(tileMapActor);
        loadLevelGoal(tileMapActor);
        loadLevelGeyserBubblingEffects(tileMapActor);
        loadLevelGeysers(tileMapActor);
        loadLevelEnemySpawnPositions(tileMapActor);
    }

    private void initializeUI() {
        initializeAttributeBars();
        initializeGoalIndicator();
        initializePauseMenu();
    }

    private void initializeAttributeBars() {
        attributeBars = new ArrayList<>();
        attributeBars.add(new AttributeBar(1, "Élet: ", Color.SALMON, Submarine::getHealthPercent, uiStage));
        attributeBars.add(new AttributeBar(3, "Pajzs: ", Color.CYAN, Submarine::getShieldPercent, uiStage));
        attributeBars.add(new AttributeBar(1, "Energia: ", Color.ROYAL, Submarine::getEnergyPercent, uiStage));

        uiTable.pad(20);
        for (AttributeBar attributeBar : attributeBars.subList(0, 2)) {
            uiTable.add(attributeBar).expandX().left().top().pad(10);
            uiTable.row();
        }
        uiTable.add(attributeBars.get(2)).expandX().left().pad(10);
    }

    private void initializeGoalIndicator() {
        BaseActor goalIndicatorGroup = new BaseActor(0, 0, uiStage);
        goalIndicatorGroup.setSize(100, 100);

        goalIndicator = new BaseActor(0, 0, uiStage);
        goalIndicator.loadTexture(Assets.instance.getTexture(Assets.LEVEL_GOAL_INDICATOR_ARROW));
        goalIndicator.setSize(100, 100);
        goalIndicator.setOrigin(goalIndicator.getWidth() / 2f, goalIndicator.getHeight() / 2f);
        goalIndicator.centerAtActor(goalIndicatorGroup);

        BaseActor goalIndicatorCenter = new BaseActor(0, 0, uiStage);
        goalIndicatorCenter.loadTexture(Assets.instance.getTexture(Assets.LEVEL_GOAL_INDICATOR));
        goalIndicatorCenter.setSize(100, 100);
        goalIndicatorCenter.centerAtActor(goalIndicatorGroup);

        goalIndicatorGroup.addActor(goalIndicator);
        goalIndicatorGroup.addActor(goalIndicatorCenter);
        uiTable.row();
        uiTable.add(goalIndicatorGroup).expand().top().left();
        updateGoalIndicator();
    }

    private void initializePauseMenu() {
        ArrayList<MenuLabel> menuOptions = new ArrayList<>();
        MenuLabel resumeGame = new MenuLabel("Játék folytatása", BaseGame.labelStyle) {
            @Override
            public void execute() {
                resumeGame();
            }
        };
        menuOptions.add(resumeGame);

        MenuLabel loadGame = new MenuLabel("Kilépés a főmenübe", BaseGame.labelStyle) {
            @Override
            public void execute() {
                levelFinished();
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

        pauseMenu = new PauseMenu(menuOptions, Assets.SUBMARINE_CONTROLS, 1f, uiStage);
        pauseMenu.setVisible(false);
    }

    @Override
    public void render(float dt) {
        if (!paused) {
            mainStage.act(dt);
            submarine.toFront();
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
        if (paused) {
            return;
        }

        spawnEnemies(dt);
        processProjectileCollision();

        checkLevelCompletion();
        checkLevelLoss();

        removeDestroyedActorsExceptSubmarine();

        updateUI();
    }

    private void updateUI() {
        updateAttributeBars();
        updateGoalIndicator();
    }

    private void updateAttributeBars() {
        for (AttributeBar attributeBar : attributeBars) {
            attributeBar.update(submarine);
        }
    }

    private void updateGoalIndicator() {
        Vector2 submarineOrigin = submarine.getPosition().cpy().add(submarine.getOriginX(), submarine.getOriginY());
        Vector2 goalOrigin = goal.getPosition().cpy().add(goal.getOriginX(), goal.getOriginY());
        goalIndicator.setRotation(goalOrigin.sub(submarineOrigin).angle());
    }

    private void levelComplete() {
        if (save.getCompletedLevels() < currentLevelIndex) {
            save.setCompletedLevels(currentLevelIndex);
            SaveGameService.save(save);
        }
        new TextDisplayScreen("Szint teljesítve!", Color.FOREST, 0.5f, uiStage).setVisible(true);
        levelFinished();
    }

    private void levelLost() {
        new TextDisplayScreen("Vereség!", Color.FIREBRICK, 1, uiStage).setVisible(true);
        levelFinished();
    }

    private void levelFinished() {
        submarine.addAction(Actions.delay(2f));
        submarine.addAction(Actions.after(new Action() {
            @Override
            public boolean act(float delta) {
                submarine.levelFinished();
                BaseGame.setActiveScreen(new LoadGameScreen());
                mainStage.addAction(Actions.removeActor());
                return true;
            }
        }));
    }

    private void checkLevelCompletion() {
        if (submarine.overlaps(goal)) {
            levelComplete();
        }
    }

    private void checkLevelLoss() {
        if (submarine.isDestroyed() || submarine.getEnergy() <= 0) {
            levelLost();
        }
    }

    private void removeDestroyedActorsExceptSubmarine() {
        for (BaseActor actor : BaseActor.getList(mainStage, "szm.orde4c.game.entity.Damageable")) {
            if (((Damageable) actor).isDestroyed()) {
                if (!(actor instanceof Submarine)) {
                    actor.addAction(Actions.removeActor());
                }
            }
        }
    }

    private void processProjectileCollision() {
        List<BaseActor> projectileList = BaseActor.getList(mainStage, "szm.orde4c.game.entity.Torpedo");
        projectileList.addAll(BaseActor.getList(mainStage, "szm.orde4c.game.entity.Projectile"));
        for (BaseActor projectileActor : projectileList) {
            if (((projectileActor.getPosition().cpy()).sub(submarine.getPosition().cpy())).len() > mainStage.getWidth() * 2) {
                projectileActor.addAction(Actions.removeActor());
                continue;
            }

            List<BaseActor> collisionActors = BaseActor.getList(mainStage, "szm.orde4c.game.entity.stationary.Environment");
            collisionActors.addAll(BaseActor.getList(mainStage, "szm.orde4c.game.entity.Damageable"));
            for (BaseActor collisionActor : collisionActors) {
                if (projectileActor.overlaps(collisionActor)) {
                    if (projectileActor instanceof Torpedo && !(collisionActor instanceof Submarine)) {
                        ((Torpedo) projectileActor).explode();
                        break;
                    } else if (projectileActor instanceof Projectile) {
                        if (collisionActor instanceof Damageable && !(collisionActor instanceof Enemy)) {
                            ((Damageable) collisionActor).damage(10);
                            projectileActor.addAction(Actions.removeActor());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void spawnEnemies(float delta) {
        enemySpawnCooldown -= delta;
        if (enemySpawnCooldown <= 0 && BaseActor.count(mainStage, "szm.orde4c.game.entity.Enemy") < MAXIMUM_ENEMY_COUNT) {
            for (Vector2 position : possibleEnemySpawns) {
                Vector2 cameraPosition = new Vector2(mainStage.getCamera().position.x, mainStage.getCamera().position.y);
                Vector2 camerPositionToSpawnPosition = position.cpy().sub(cameraPosition);
                if (camerPositionToSpawnPosition.len() > mainStage.getCamera().viewportWidth / 2f + 200 && camerPositionToSpawnPosition.len() < mainStage.getWidth() * 2f) {
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

    private void loadLevelSubmarineStart(TileMapActor tileMapActor) {
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

    private void loadLevelGeyserBubblingEffects(TileMapActor tileMapActor) {
        for (MapObject geyserEffectObject : tileMapActor.getRectangleList("GeyserEffect")) {
            MapProperties geyserEffectProperties = geyserEffectObject.getProperties();
            float geyserEffectX = (float) geyserEffectProperties.get("x");
            float geyserEffectY = (float) geyserEffectProperties.get("y");
            float geyserEffectWidth = (float) geyserEffectProperties.get("width");
            GeyserBubblingEffect effect = new GeyserBubblingEffect();
            effect.setPosition(geyserEffectX, geyserEffectY);
            mainStage.addActor(effect);
        }
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
        for (MapObject enemySpawnObject : tileMapActor.getRectangleList("EnemySpawn")) {
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
        if (buttonCode == XBoxGamepad.BUTTON_BACK) {
            if (paused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
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
