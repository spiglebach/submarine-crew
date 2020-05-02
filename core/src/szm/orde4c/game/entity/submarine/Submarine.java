package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.entity.Damageable;
import szm.orde4c.game.util.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class Submarine extends BaseActor implements InputProcessor, ControllerListener, Damageable {
    // Players
    private ArrayList<Player> players;
    private ArrayList<Vector2> playerStartPositions;

    private static final float MAX_HEALTH = 100;
    private static final float MAX_ENERGY = 100;
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

    float delay = 0.5f;
    private float boundaryPolygonOffsetX;
    private float boundaryPolygonOffsetY;

    public Submarine(float x, float y, Polygon boundaryPolygon, Stage s) {
        super(x, y, s);
        loadTexture("submarine/submarine.png");
        setSize(1440, 960);
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        setBoundaryPolygon(boundaryPolygon);

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
    }

    @Override
    public void accelerateForward() {
        Vector2 acceleration = new Vector2(horizontalAcceleration, 0).setAngle(getRotation());
        accelerationVector.add(acceleration);
    }

    public void accelerateBackward() {
        Vector2 acceleration = new Vector2(horizontalAcceleration, 0).setAngle(getRotation() + 180);
        accelerationVector.add(acceleration);
    }

    public void ascend() {
        Vector2 acceleration = new Vector2(0, verticalAcceleration).setAngle(90);
        accelerationVector.add(acceleration);
    }

    public void descend() {
        Vector2 acceleration = new Vector2(0, verticalAcceleration).setAngle(270);
        accelerationVector.add(acceleration);
    }

    public void liftNose() {
        rotate(1);
    }

    public void lowerNose() {
        rotate(-1);
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
            e.printStackTrace();
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
            delay -= delta;
            for (Player player : players) {
                player.setPosition(playerStartPositions.get(0).x, playerStartPositions.get(0).y);
                player.setSpeed(0);
            }
            return;
        }

        super.act(delta);

        for (BaseActor solid : BaseActor.getList(this, "szm.orde4c.game.entity.stationary.Solid")) {
            for (Player player : players) {
                if (!player.isClimbing()) {
                    Vector2 offset = player.preventOverlap(solid);
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
        for (Player player : players) {
            if (!player.isClimbing()) {
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


        {
            // TODO refactor to damageable objects, do not remove, but damage and return afterwards if below 0 hp
            List<BaseActor> submarineCollisionActors = new ArrayList<>();
            submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Rock"));
            submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Environment"));
            submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Vegetation"));
            submarineCollisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.Fish"));

            for (BaseActor environmentActor : submarineCollisionActors) {
                Vector2 bodyCollisionOffset = preventOverlap(environmentActor);
                if (bodyCollisionOffset != null) {
                    bump(bodyCollisionOffset);
                }
                for (BaseActor armActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.Arm")) {
                    Arm arm = (Arm) armActor;
                    Vector2 armCollisionOffset = arm.preventOverlap(environmentActor);
                    if (armCollisionOffset != null) {
                        if (Math.abs(armCollisionOffset.x) > Math.abs(armCollisionOffset.y)) {
                            stopMovementX();
                        } else {
                            stopMovementY();
                        }
                    }
                    if (environmentActor instanceof Damageable) {
                        if (arm.overlaps(environmentActor)) {

                        }
                    }
                }
            }
        }
        this.applyPhysics(delta);
        this.applyRotation(delta);
        alignCamera();
    }

    public void bump(Vector2 offset) {
        float speed = getSpeed();
        float bumpSpeed = speed * 0.5f;
        if (Math.abs(offset.x) > Math.abs(offset.y)) {
            stopMovementX();
        } else {
            stopMovementY();
        }

        setSpeed(bumpSpeed);
        setMotionAngle(offset.angle());

        if (speed > (verticalMaxSpeed + horizontalMaxSpeed) * 0.5f * 0.2f) {
            float healthPunishment = MAX_HEALTH * 0.1f;
            float speedScale = speed / ((verticalMaxSpeed + horizontalMaxSpeed) * 0.5f);
            healthPunishment *= speedScale;

            damage(MathUtils.floor(healthPunishment));
        }

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

    public void initializePlayers(PlayerInfo[] playerInfos) {
        players = new ArrayList<>();
        for (PlayerInfo info : playerInfos) {
            int startPositionIndex = MathUtils.random(playerStartPositions.size() - 1);
            Player player = new Player(playerStartPositions.get(startPositionIndex).x, playerStartPositions.get(startPositionIndex).y, info, getStage());
            players.add(player);
            addActor(player);
        }
    }

    public void addPlayerStartPosition(Vector2 position) {
        playerStartPositions.add(position);
    }

    private boolean operateStation(Player player) {
        for (BaseActor stationActor : BaseActor.getList(this, "szm.orde4c.game.entity.submarine.Station")) {
            Station station = (Station) stationActor;
            if (player.overlaps(station) && !station.isOperated()) {
                player.setStation(station);
                station.setOperatingPlayer(player);
                player.centerAtActor(station);
                player.stopMovementX();
                player.stopMovementY();
                return true;
            }
        }
        return false;
    }

    private boolean useElevatorUp(Player player) {
        return useElevator(player, 1);
    }

    private boolean useElevatorDown(Player player) {
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

    public void increaseHealth(int amount) {
        health = MathUtils.clamp(health + amount, 0, MAX_HEALTH);
    }

    public void increaseEnergy(int amount) {
        energy = MathUtils.clamp(energy + amount, 0, MAX_ENERGY);
    }

    public void increaseShield(int amount) {
        shield = MathUtils.clamp(shield + amount, 0, MAX_SHIELD);
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

    @Override
    public boolean keyDown(int keycode) {
        for (Player player : players) {
            if (player.isKeyboardPlayer()) {
                if (!player.isOperatingStation() && keycode == Input.Keys.E) {
                    if (operateStation(player)) {
                        return true;
                    }
                }
                if (keycode == Input.Keys.S) {
                    if (useElevatorDown(player)) {
                        return true;
                    }
                }
                if (keycode == Input.Keys.W) {
                    if(useElevatorUp(player)) {
                        return true;
                    }
                }
                player.keyDown(keycode);
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
                player.buttonPressed(buttonCode);
                return true;
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
        for (Player player : players) {
            if (!player.isKeyboardPlayer() && player.getController().equals(controller)) {
                if (player.isOperatingStation()) {
                    player.axisMoved(axisCode, value);
                } else if ((axisCode == XBoxGamepad.AXIS_LEFT_Y) && Math.abs(value) > 0.6) {
                    boolean elevatorUsed = false;
                    if (value > 0) {
                        elevatorUsed = useElevatorUp(player);
                    } else {
                        elevatorUsed = useElevatorDown(player);
                    }
                    return elevatorUsed;
                }
            }
        }
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int id, PovDirection povDirection) {
        for (Player player : players) {
            if (!player.isKeyboardPlayer() && player.getController().equals(controller)) {
                boolean elevatorUsed = false;
                if (povDirection.equals(XBoxGamepad.DPAD_DOWN)) {
                    elevatorUsed = useElevatorDown(player);
                }
                if (povDirection.equals(XBoxGamepad.DPAD_UP)) {
                    elevatorUsed = useElevatorUp(player);
                }
                return elevatorUsed;
            }
        }
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

    public void setBoundaryPolygonOffsetX(float boundaryPolygonOffsetX) {
        this.boundaryPolygonOffsetX = boundaryPolygonOffsetX;
    }

    public void setBoundaryPolygonOffsetY(float boundaryPolygonOffsetY) {
        this.boundaryPolygonOffsetY = boundaryPolygonOffsetY;
    }

    @Override
    public void damage(int damage) {
        if (shield >= 33) {
            shield -= 33;
            shield = MathUtils.clamp(shield, 0, MAX_SHIELD);
        } else {
            health -= damage;
            health = MathUtils.clamp(health, 0, MAX_HEALTH);
        }
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }
}
