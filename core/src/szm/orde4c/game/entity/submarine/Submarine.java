package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.TileMapActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.entity.Damageable;
import szm.orde4c.game.entity.Enemy;
import szm.orde4c.game.entity.stationary.Solid;
import szm.orde4c.game.util.Direction;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.PlayerInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Submarine extends BaseActor implements InputProcessor, ControllerListener, Damageable {
    private ArrayList<Player> players;
    private ArrayList<Vector2> playerStartPositions;

    private static final float MAX_HEALTH = 100;
    private static final float MAX_ENERGY = 200;
    private static final float MAX_SHIELD = 100;
    private float health;
    private float shield;
    private float energy;

    private float verticalMaxSpeed;
    private float verticalAcceleration;
    private float verticalDeceleration;

    private float horizontalMaxSpeed;
    private float horizontalAcceleration;
    private float horizontalDeceleration;

    private float delay = 0.5f;
    private float boundaryPolygonOffsetX;
    private float boundaryPolygonOffsetY;

    private BaseActor reflectorActor;

    public Submarine(float x, float y, Stage s) {
        super(x, y, s);
        loadTexture(Assets.instance.getTexture(Assets.SUBMARINE_IMAGE));
        setSize(360, 240);
        setOrigin(getWidth() / 2f, getHeight() / 2f);

        playerStartPositions = new ArrayList<>();

        health = MAX_HEALTH;
        shield = MAX_SHIELD;
        energy = MAX_ENERGY;

        verticalMaxSpeed = 300;
        verticalAcceleration = 150;
        verticalDeceleration = 200;

        horizontalMaxSpeed = 400;
        horizontalAcceleration = 200;
        horizontalDeceleration = 200;

        setRotationLimit(10);
        setRotationSpeed(10);

        reflectorActor = new BaseActor(0, 0, s);
        loadSubmarineFromTiledMap();
    }

    @Override
    public void applyPhysics(float delta) {
        velocityVector.add(accelerationVector.x * delta, accelerationVector.y * delta);
        float horizontalDirection = 1;
        float verticalDirection = 1;
        try {
            horizontalDirection = velocityVector.x / Math.abs(velocityVector.x);
            verticalDirection = velocityVector.y / Math.abs(velocityVector.y);
        } catch (Exception e) {
            // Zero division
        }
        float horizontalSpeed = velocityVector.x;
        float verticalSpeed = velocityVector.y;

        if (accelerationVector.len() == 0) {
            setAnimationPaused(true);
        } else {
            setAnimationPaused(false);
        }

        if (accelerationVector.x == 0 && horizontalSpeed != 0) {
            horizontalSpeed -= horizontalDirection * delta * horizontalDeceleration;
        }
        if (accelerationVector.y == 0 && verticalSpeed != 0) {
            verticalSpeed -= verticalDirection * delta * verticalDeceleration;
        }

        if (horizontalDirection > 0) {
            horizontalSpeed = MathUtils.clamp(horizontalSpeed, 0, horizontalMaxSpeed);
        } else {
            horizontalSpeed = MathUtils.clamp(horizontalSpeed, -horizontalMaxSpeed, 0);
        }

        if (verticalDirection > 0) {
            verticalSpeed = MathUtils.clamp(verticalSpeed, 0, verticalMaxSpeed);
        } else {
            verticalSpeed = MathUtils.clamp(verticalSpeed, -verticalMaxSpeed, 0);
        }

        velocityVector.set(horizontalSpeed, verticalSpeed);

        moveBy(velocityVector.x * delta, velocityVector.y * delta);

        accelerationVector.set(0, 0);
    }

    @Override
    public void act(float delta) {
        if (delay > 0) {
            delayBeforeLevel(delta);
            return;
        }
        super.act(delta);

        processPlayerCollisions();
        processSubmarineCollisions(delta);
        ifPlayerIsAloneThenOperateArmStations();
        ifSubmarineOverlapsGeyserThenRechargeEnergy(delta);
        applyStationContiniousEnergyConsumption(delta);

        applyPhysics(delta);
        applyRotation(delta);
        alignCamera();
    }

    private void delayBeforeLevel(float delta) {
        delay -= delta;
        for (Player player : players) {
            player.setPosition(playerStartPositions.get(0).x, playerStartPositions.get(0).y);
            player.setSpeed(0);
        }
    }

    private void processPlayerCollisions() {
        for (Player player : players) {
            if (!player.isClimbing()) {
                for (BaseActor solid : BaseActor.getList(this, "szm.orde4c.game.entity.stationary.Solid")) {
                    Vector2 offset = player.preventOverlap(solid);
                    if (offset != null) {
                        if (Math.abs(offset.x) > Math.abs(offset.y)) {
                            player.stopMovementX();
                        } else {
                            player.stopMovementY();
                        }
                    }
                }
                for (BaseActor ladderActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.Ladder")) {
                    if (player.belowOverlaps(ladderActor) && player.getY() >= ladderActor.getY() + ladderActor.getHeight() - 4) {
                        Vector2 offset = player.preventOverlap(ladderActor);
                        if (offset != null) {
                            if (Math.abs(offset.x) > Math.abs(offset.y)) {
                                player.stopMovementX();
                            } else {
                                player.stopMovementY();
                            }
                        }
                    }
                }
            }
        }
    }

    private void processSubmarineCollisions(float delta) {
        List<BaseActor> submarineCollisionActors = new ArrayList<>();
        submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Rock"));
        submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Environment"));
        submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Vegetation"));
        submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.Enemy"));

        for (BaseActor collisionActor : submarineCollisionActors) {
            Vector2 bodyCollisionOffset = preventOverlap(collisionActor);
            if (bodyCollisionOffset != null) {
                bump(bodyCollisionOffset);
                if (collisionActor instanceof Enemy) {
                    Enemy enemyActor = (Enemy) collisionActor;
                    enemyActor.damage(20);
                }
            }
            for (BaseActor armActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.Arm")) {
                Arm arm = (Arm) armActor;
                Vector2 armCollisionOffset = arm.preventOverlap(collisionActor);
                arm.ifArmIsNotOperatedThenProcessEnvironmentObjectWithSensorPolygon(collisionActor);
                if (armCollisionOffset != null) {
                    if (Math.abs(armCollisionOffset.x) > Math.abs(armCollisionOffset.y)) {
                        stopMovementX();
                    } else {
                        stopMovementY();
                    }
                }
                if (collisionActor instanceof Damageable) {
                    if (arm.overlaps(collisionActor)) {
                        Damageable damageableActor = (Damageable) collisionActor;
                        damageableActor.damage(20);
                    }
                }
            }
        }
    }

    private void ifPlayerIsAloneThenOperateArmStations() {
        if (players.size() < 2) {
            for (BaseActor armStationActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.ArmStation")) {
                ArmStation armStation = (ArmStation) armStationActor;
                if (!armStation.isOperated()) {
                    armStation.operate();
                }
            }
        }
    }

    private void ifSubmarineOverlapsGeyserThenRechargeEnergy(float delta) {
        for (BaseActor geyserActor : BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Geyser")) {
            if (this.overlaps(geyserActor)) {
                rechargeEnergy(delta);
            }
        }
    }

    private void applyStationContiniousEnergyConsumption(float delta) {
        for (BaseActor stationActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.Station")) {
            Station station = (Station) stationActor;
            station.continiousEnergyConsumption(delta);
        }
    }

    private void rechargeEnergy(float delta) {
        increaseEnergy(5 * delta);
    }

    private void bump(Vector2 offset) {
        float speed = getSpeed();
        float bumpSpeed = speed * 0.5f;
        if (Math.abs(offset.x) > Math.abs(offset.y)) {
            stopMovementX();
        } else {
            stopMovementY();
        }

        setSpeed(bumpSpeed);
        setMotionAngle(offset.angle());

        if (speed > (verticalMaxSpeed + horizontalMaxSpeed) * 0.1f) {
            float healthPunishment = MAX_HEALTH * 0.1f;
            float speedScale = speed / ((verticalMaxSpeed + horizontalMaxSpeed) * 0.5f);
            healthPunishment *= speedScale;

            damage(MathUtils.floor(healthPunishment));
        }
    }

    public void initializePlayers(PlayerInfo[] playerInfos) {
        players = new ArrayList<>();
        for (PlayerInfo info : playerInfos) {
            int startPositionIndex = MathUtils.random(playerStartPositions.size() - 1);
            Player player = new Player(playerStartPositions.get(startPositionIndex).x, playerStartPositions.get(startPositionIndex).y, info, this, getStage());
            players.add(player);
            if (player.isKeyboardPlayer()) {
                InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
                im.addProcessor(player);
            } else if (player.getController() != null) {
                player.getController().addListener(player);
            }
            addActor(player);
        }
    }

    private boolean operateStation(Player player) {
        for (BaseActor stationActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.Station")) {
            Station station = (Station) stationActor;
            if (player.overlaps(station) && !station.isOperated()) {
                player.setStation(station);
                station.setOperatingPlayer(player);
                player.setPosition(station.getX() + station.getWidth() - player.getWidth(), station.getY());
                player.stopMovementX();
                player.stopMovementY();
                return true;
            }
        }
        return false;
    }

    boolean useElevatorUp(Player player) {
        return useElevator(player, 1);
    }

    boolean useElevatorDown(Player player) {
        return useElevator(player, -1);
    }

    private boolean useElevator(Player player, int direction) {
        for (BaseActor elevatorGroupActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.ElevatorGroup")) {
            ElevatorGroup elevatorGroup = (ElevatorGroup) elevatorGroupActor;
            List<Elevator> elevators = elevatorGroup.getElevators();
            for (int i = 0; i < elevators.size(); i++) {
                if (player.overlaps(elevators.get(i))) {
                    if ((direction > 0 && i < elevators.size() - 1) || (direction < 0 && i > 0)) {
                        player.centerAtActor(elevators.get(i + direction));
                        player.stopMovementX();
                        player.stopMovementY();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Vector2 preventOverlap(BaseActor other) {
        Polygon poly1 = this.getBoundaryPolygon();
        Polygon poly2 = other.getBoundaryPolygon();

        if (!poly1.getBoundingRectangle().overlaps(poly2.getBoundingRectangle())) {
            return null;
        }

        Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
        boolean polygonOverlap = Intersector.overlapConvexPolygons(poly1, poly2, mtv);

        if (!polygonOverlap) {
            return null;
        }

        if (rotationDirection != 0) {
            rotationDirection = 0;
        } else {
            this.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);
        }
        return mtv.normal;
    }

    @Override
    public Polygon getBoundaryPolygon() {
        Polygon modifiedBoundaryPolygon = super.getBoundaryPolygon();
        modifiedBoundaryPolygon.setPosition(getX() + boundaryPolygonOffsetX, getY() + boundaryPolygonOffsetY);
        modifiedBoundaryPolygon.setOrigin(getOriginX() + boundaryPolygonOffsetY, getOriginY() + boundaryPolygonOffsetY);
        modifiedBoundaryPolygon.setRotation(getRotation());
        modifiedBoundaryPolygon.setScale(getScaleX(), getScaleY());
        return modifiedBoundaryPolygon;
    }

    private void setBoundaryPolygonOffset(float boundaryPolygonOffsetX, float boundaryPolygonOffsetY) {
        this.boundaryPolygonOffsetX = boundaryPolygonOffsetX;
        this.boundaryPolygonOffsetY = boundaryPolygonOffsetY;
    }

    @Override
    public void damage(float damage) {
        if (shield >= 33) {
            shield -= 33;
            shield = MathUtils.clamp(shield, 0, MAX_SHIELD);
        } else {
            health -= damage;
            health = MathUtils.clamp(health, 0, MAX_HEALTH);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        reflectorActor.setVisible(true);
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR);
        reflectorActor.draw(batch, 1);
        batch.end();
        reflectorActor.setVisible(false);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a);

        if (animation != null && isVisible()) {
            batch.draw(animation.getKeyFrame(elapsedTime), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }

        super.draw(batch, parentAlpha);
    }

    public void levelFinished() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        for (Player player : players) {
            im.removeProcessor(player);
            if (player.getController() != null) {
                player.getController().removeListener(player);
            }
        }
    }

    private void loadSubmarineFromTiledMap() {
        TileMapActor tileMapActor = new TileMapActor("submarine/submarine.tmx");

        loadSubmarineBoundaryPolygon(tileMapActor);
        loadSubmarineSolids(tileMapActor);
        loadSubmarineLadders(tileMapActor);
        loadSubmarineElevators(tileMapActor);

        for (MapObject stationObject : tileMapActor.getRectangleList("Station")) {
            MapProperties stationProperties = stationObject.getProperties();
            String stationType = (String) stationProperties.get("type");
            try {
                switch (stationType) {
                    case "Engine":
                        loadEngineStation(stationProperties);
                        break;
                    case "Arm":
                        loadArmStation(stationProperties, tileMapActor);
                        break;
                    case "Torpedo":
                        loadTorpedoStation(stationProperties, tileMapActor);
                        break;
                    case "Shield":
                        loadShieldStation(stationProperties);
                        break;
                    case "Reflector":
                        loadReflectorStation(stationProperties);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        loadSubmarinePlayerStartingPositions(tileMapActor);
    }

    private void loadSubmarineBoundaryPolygon(TileMapActor tileMapActor) {
        Polygon boundaryPolygon = ((PolygonMapObject) tileMapActor.getPolygonList("BoundaryPolygon").get(0)).getPolygon();
        setBoundaryPolygon(boundaryPolygon);
        setBoundaryPolygonOffset(boundaryPolygon.getX(), boundaryPolygon.getY());
    }

    private void loadSubmarineSolids(TileMapActor tileMapActor) {
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
                // Rotation not specified!
            }
            Solid solid = new Solid(solidObjectX, solidObjectY, solidObjectWidth, solidObjectHeight, solidObjectRotation, getStage());
            addActor(solid);
        }

        for (MapObject solidObject : tileMapActor.getPolygonList("Solid")) {
            PolygonMapObject solidPolygonObject = (PolygonMapObject) solidObject;
            MapProperties solidPolygonProperties = solidObject.getProperties();
            float solidObjectX = (float) solidPolygonProperties.get("x");
            float solidObjectY = (float) solidPolygonProperties.get("y");
            Polygon solidObjectBoundaryPolygon = solidPolygonObject.getPolygon();

            Solid solid = new Solid(solidObjectX, solidObjectY, solidObjectBoundaryPolygon, getStage());
            addActor(solid);
        }
    }

    private void loadSubmarineLadders(TileMapActor tileMapActor) {
        for (MapObject ladderObject : tileMapActor.getRectangleList("Ladder")) {
            MapProperties ladderObjectProperties = ladderObject.getProperties();
            float ladderObjectX = (float) ladderObjectProperties.get("x");
            float ladderObjectY = (float) ladderObjectProperties.get("y");
            float ladderObjectWidth = (float) ladderObjectProperties.get("width");
            float ladderObjectHeight = (float) ladderObjectProperties.get("height");

            Ladder ladder = new Ladder(ladderObjectX, ladderObjectY, ladderObjectWidth, ladderObjectHeight, getStage());
            addActor(ladder);
        }
    }

    private void loadSubmarineElevators(TileMapActor tileMapActor) {
        for (MapObject object : tileMapActor.getRectangleList("Elevator")) {
            MapProperties elevatorProps = object.getProperties();
            float elevatorX = (float) elevatorProps.get("x");
            float elevatorY = (float) elevatorProps.get("y");
            float elevatorWidth = (float) elevatorProps.get("width");
            float elevatorHeight = (float) elevatorProps.get("height");
            String elevatorGroup = (String) elevatorProps.get("group");
            int elevatorId = Integer.parseInt((String) elevatorProps.get("id"));
            Elevator elevator = new Elevator(elevatorX, elevatorY, elevatorWidth, elevatorHeight, getStage());

            boolean existingGroup = false;
            for (BaseActor elevatorGroupActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.ElevatorGroup")) {
                ElevatorGroup group = (ElevatorGroup) elevatorGroupActor;
                if (group.getGroup().equals(elevatorGroup)) {
                    existingGroup = true;
                    group.addElevator(elevatorId, elevator);
                }
            }
            if (!existingGroup) {
                ElevatorGroup newElevatorGroup = new ElevatorGroup((String) elevatorProps.get("group"), getStage());
                newElevatorGroup.addElevator(elevatorId, elevator);
                addActor(newElevatorGroup);
            }
            addActor(elevator);
        }
    }

    private void loadSubmarinePlayerStartingPositions(TileMapActor tileMapActor) {
        for (MapObject object : tileMapActor.getRectangleList("Start")) {
            MapProperties props = object.getProperties();
            addPlayerStartPosition(new Vector2((float) props.get("x"), (float) props.get("y")));
        }
    }

    private void loadEngineStation(MapProperties engineStationProperties) {
        float stationX = (float) engineStationProperties.get("x");
        float stationY = (float) engineStationProperties.get("y");
        float stationWidth = (float) engineStationProperties.get("width");
        float stationHeight = (float) engineStationProperties.get("height");
        new EngineStation(stationX, stationY, stationWidth, stationHeight, this, getStage());
    }

    private void loadArmStation(MapProperties armStationProperties, TileMapActor tileMapActor) {
        String armId = (String) armStationProperties.get("id");
        float stationX = (float) armStationProperties.get("x");
        float stationY = (float) armStationProperties.get("y");
        float stationWidth = (float) armStationProperties.get("width");
        float stationHeight = (float) armStationProperties.get("height");

        MapProperties armProperties = tileMapActor.getRectangleList(armId).get(0).getProperties();
        float armX = (float) armProperties.get("x");
        float armY = (float) armProperties.get("y");
        Direction armDirection = Direction.valueOf((String) armProperties.get("direction"));

        Arm arm = new Arm(armX, armY, armDirection, getStage());
        new ArmStation(stationX, stationY, stationWidth, stationHeight, arm, this, getStage());
    }

    private void loadTorpedoStation(MapProperties torpedoStationProperties, TileMapActor tileMapActor) {
        String torpedoStartId = (String) torpedoStationProperties.get("id");
        float stationX = (float) torpedoStationProperties.get("x");
        float stationY = (float) torpedoStationProperties.get("y");
        float stationWidth = (float) torpedoStationProperties.get("width");
        float stationHeight = (float) torpedoStationProperties.get("height");

        MapProperties torpedoStartProps = tileMapActor.getRectangleList(torpedoStartId).get(0).getProperties();
        float torpedoStartX = (float) torpedoStartProps.get("x");
        float torpedoStartY = (float) torpedoStartProps.get("y");
        ArrayDeque<BaseActor> torpedoCountIndicators = new ArrayDeque<>();
        for (MapObject torpedoCountIndicatorObject : tileMapActor.getRectangleList(String.format("%sCountIndicator", torpedoStartId))) {
            MapProperties torpedoCountIndicatorProperties = torpedoCountIndicatorObject.getProperties();
            float indicatorX = (float) torpedoCountIndicatorProperties.get("x");
            float indicatorY = (float) torpedoCountIndicatorProperties.get("y");
            float indicatorWidth = (float) torpedoCountIndicatorProperties.get("width");
            float indicatorHeight = (float) torpedoCountIndicatorProperties.get("height");
            BaseActor indicator = new BaseActor(indicatorX, indicatorY, getStage());
            indicator.loadTexture(Assets.instance.getTexture(Assets.BLANK));
            indicator.setSize(indicatorWidth, indicatorHeight);
            indicator.setColor(Color.GREEN);
            torpedoCountIndicators.add(indicator);
            addActor(indicator);
        }
        new TorpedoStation(stationX, stationY, stationWidth, stationHeight, torpedoStartX, torpedoStartY, torpedoCountIndicators, this, getStage());
    }

    private void loadShieldStation(MapProperties shieldStationProperties) {
        float stationX = (float) shieldStationProperties.get("x");
        float stationY = (float) shieldStationProperties.get("y");
        float stationWidth = (float) shieldStationProperties.get("width");
        float stationHeight = (float) shieldStationProperties.get("height");

        new ShieldStation(stationX, stationY, stationWidth, stationHeight, this, getStage());
    }

    private void loadReflectorStation(MapProperties reflectorStationProperties) {
        float stationX = (float) reflectorStationProperties.get("x");
        float stationY = (float) reflectorStationProperties.get("y");
        float stationWidth = (float) reflectorStationProperties.get("width");
        float stationHeight = (float) reflectorStationProperties.get("height");

        new ReflectorStation(stationX, stationY, stationWidth, stationHeight, this, getStage());
    }

    @Override
    public void accelerateForward() {
        Vector2 acceleration = new Vector2(horizontalAcceleration, 0).setAngle(getRotation());
        accelerationVector.add(acceleration);
    }

    void accelerateBackward() {
        Vector2 acceleration = new Vector2(horizontalAcceleration, 0).setAngle(getRotation() + 180);
        accelerationVector.add(acceleration);
    }

    void ascend() {
        Vector2 acceleration = new Vector2(0, verticalAcceleration).setAngle(90);
        accelerationVector.add(acceleration);
    }

    void descend() {
        Vector2 acceleration = new Vector2(0, verticalAcceleration).setAngle(270);
        accelerationVector.add(acceleration);
    }

    void liftNose() {
        rotate(1);
    }

    void lowerNose() {
        rotate(-1);
    }

    void increaseHealth(float amount) {
        health = MathUtils.clamp(health + amount, 0, MAX_HEALTH);
    }

    private void increaseEnergy(float amount) {
        energy = MathUtils.clamp(energy + amount, 0, MAX_ENERGY);
    }

    void increaseShield(float amount) {
        shield = MathUtils.clamp(shield + amount, 0, MAX_SHIELD);
    }

    void decreaseEnergy(float amount) {
        energy = MathUtils.clamp(energy - amount, 0, MAX_ENERGY);
    }


    private void addPlayerStartPosition(Vector2 position) {
        playerStartPositions.add(position);
    }

    BaseActor getReflectorActor() {
        return reflectorActor;
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }

    public float getEnergy() {
        return energy;
    }

    public float getHealthPercent() {
        return health / MAX_HEALTH;
    }

    public float getEnergyPercent() {
        return energy / MAX_ENERGY;
    }

    public float getShieldPercent() {
        return shield / MAX_SHIELD;
    }

    @Override
    public boolean keyDown(int keycode) {
        for (Player player : players) {
            if (player.isKeyboardPlayer()) {
                if (!player.isOperatingStation() && keycode == Input.Keys.E) {
                    if (operateStation(player)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        for (Player player : players) {
            if (!player.isKeyboardPlayer() && player.getController().equals(controller)) {
                if (!player.isOperatingStation() && buttonCode == XBoxGamepad.BUTTON_X) {
                    if (operateStation(player)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonUp(Controller controller, int i) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int id, PovDirection povDirection) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int i, boolean b) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int i, boolean b) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int i, Vector3 vector3) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
