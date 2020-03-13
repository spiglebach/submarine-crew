package szm.orde4c.game.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class BaseActor extends Group {
    protected Animation<TextureRegion> animation;
    protected float elapsedTime;
    private boolean animationPaused;

    protected Vector2 velocityVector;
    protected Vector2 accelerationVector;
    protected float acceleration;

    protected float maxSpeed;
    protected float deceleration;

    float rotationSpeed;
    float rotationLimit;
    float initialRotation;
    int rotationDirection;

    private Polygon boundaryPolygon;

    public static Rectangle worldBounds;

    public BaseActor(float x, float y, Stage s) {
        super();
        setPosition(x, y);
        s.addActor(this);

        animation = null;
        elapsedTime = 0;
        animationPaused = false;

        velocityVector = new Vector2(0, 0);
        accelerationVector = new Vector2(0, 0);
        acceleration = 0;

        maxSpeed = 1000;
        deceleration = 0;

        rotationSpeed = 0;
        rotationLimit = 0;
        initialRotation = 0;
        rotationDirection = 0;
    }

    public void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
        TextureRegion tr = animation.getKeyFrame(0);
        float w = tr.getRegionWidth();
        float h = tr.getRegionHeight();
        setSize(w, h);
        setOrigin(w / 2, h / 2);

        if (boundaryPolygon == null) {
            setBoundaryRectangle();
        }
    }


    public void setAnimationPaused(boolean animationPaused) {
        this.animationPaused = animationPaused;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!animationPaused) {
            elapsedTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a);

        if (animation != null && isVisible()) {
            batch.draw(animation.getKeyFrame(elapsedTime), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }

        super.draw(batch, parentAlpha);
    }

    public void applyPhysics(float dt) {
        velocityVector.add(accelerationVector.x * dt, accelerationVector.y * dt);

        float speed = getSpeed();

        if (accelerationVector.len() == 0) {
            speed -= deceleration * dt;
        }
        speed = MathUtils.clamp(speed, 0, maxSpeed);

        setSpeed(speed);

        moveBy(velocityVector.x * dt, velocityVector.y * dt);

        accelerationVector.set(0, 0);
    }

    public void applyRotation(float dt) {
        float rotation = getRotation();
        rotation += rotationSpeed * dt * rotationDirection;
        if (rotationLimit != 0) {
            rotation = MathUtils.clamp(rotation, initialRotation - rotationLimit, initialRotation + rotationLimit);
        }
        setRotation(rotation);
        rotationDirection = 0;
    }

    public void rotate(int direction) {
        rotationDirection = direction;
    }

    public void moveForward(float distance) {
        Vector2 directionVector = accelerationVector.nor();
        moveBy(directionVector.x * distance, directionVector.y * distance);

    }

    public void setBoundaryRectangle() {
        float w = getWidth();
        float h = getHeight();
        float[] vertices = {0, 0, w, 0, w, h, 0, h};
        boundaryPolygon = new Polygon(vertices);
    }

    public void setBoundaryPolygon(int numSides) {
        float w = getWidth();
        float h = getHeight();

        float[] vertices = new float[2 * numSides];
        for (int i = 0; i < numSides; i++) {
            float angle = i * MathUtils.PI2 / numSides;
            vertices[2 * i] = w / 2 * MathUtils.cos(angle) + w / 2;
            vertices[2 * i + 1] = h / 2 * MathUtils.sin(angle) + h / 2;
        }
        boundaryPolygon = new Polygon(vertices);
    }

    public void setBoundaryPolygon(Polygon polygon) {
        boundaryPolygon = polygon;
    }

    public void stopMovementY() {
        velocityVector.y = 0;
    }

    public void stopMovementX() {
        velocityVector.x = 0;
    }

    public Polygon getBoundaryPolygon() {
        boundaryPolygon.setPosition(getX(), getY());
        boundaryPolygon.setOrigin(getOriginX(), getOriginY());
        boundaryPolygon.setRotation(getRotation());
        boundaryPolygon.setScale(getScaleX(), getScaleY());
        return boundaryPolygon;
    }

    public boolean overlaps(BaseActor other) {
        Polygon poly1 = this.getBoundaryPolygon();
        Polygon poly2 = other.getBoundaryPolygon();

        if (!poly1.getBoundingRectangle().overlaps(poly2.getBoundingRectangle())) {
            return false;
        }

        return Intersector.overlapConvexPolygons(poly1, poly2);
    }

    public Vector2 preventOverlap(BaseActor other) {
        return preventOverlapWithScale(other, 1.0f);
    }

    public Vector2 preventOverlapWithScale(BaseActor other, float scale) {
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

        this.moveBy(mtv.normal.x * mtv.depth * scale, mtv.normal.y * mtv.depth * scale);
        return mtv.normal;
    }

    public void bounceOff(BaseActor other) {
        Vector2 v = preventOverlap(other);
        if (Math.abs(v.x) >= Math.abs(v.y)) {
            velocityVector.x *= -1;
        } else {
            velocityVector.y *= -1;
        }
    }

    public boolean isWithinDistance(float distance, BaseActor other) {
        Polygon poly1 = this.getBoundaryPolygon();
        float scaleX = (this.getWidth() + 2 * distance) / this.getWidth();
        float scaleY = (this.getHeight() + 2 * distance) / this.getHeight();
        poly1.setScale(scaleX, scaleY);

        Polygon poly2 = other.getBoundaryPolygon();

        if (!poly1.getBoundingRectangle().overlaps(poly2.getBoundingRectangle())) {
            return false;
        }

        return Intersector.overlapConvexPolygons(poly1, poly2);
    }

    public Animation<TextureRegion> loadAnimationFromFiles(String[] fileNames, float frameDuration, boolean loop) {
        int fileCount = fileNames.length;
        Array<TextureRegion> textureArray = new Array<>();

        for (int i = 0; i < fileCount; i++) {
            String fileName = fileNames[i];
            Texture texture = new Texture(Gdx.files.internal(fileName));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textureArray.add(new TextureRegion(texture));
        }

        Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, textureArray);

        if (loop) {
            animation.setPlayMode(Animation.PlayMode.LOOP);
        } else {
            animation.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (animation != null) {
            setAnimation(animation);
        }
        return animation;
    }

    public Animation<TextureRegion> loadAnimationFromSheet(String fileName, int rows, int cols, float frameDuration, boolean loop) {
        Texture texture = new Texture(Gdx.files.internal(fileName));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight() / rows;

        TextureRegion[][] temp = TextureRegion.split(texture, frameWidth, frameHeight);

        Array<TextureRegion> textureArray = new Array<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                textureArray.add(temp[r][c]);
            }
        }

        Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, textureArray);

        if (loop) {
            animation.setPlayMode(Animation.PlayMode.LOOP);
        } else {
            animation.setPlayMode(Animation.PlayMode.NORMAL);
        }
        if (animation != null) {
            setAnimation(animation);
        }
        return animation;
    }

    public Animation<TextureRegion> loadTexture(String fileName) {
        String[] fileNames = new String[1];
        fileNames[0] = fileName;
        return loadAnimationFromFiles(fileNames, 1, false);
    }

    public void resetAnimation() {
        elapsedTime = 0;
    }

    public boolean isAnimationFinished() {
        return animation.isAnimationFinished(elapsedTime);
    }

    public void setSpeed(float speed) {
        if (velocityVector.len() == 0) {
            velocityVector.set(speed, 0);
        } else {
            velocityVector.setLength(speed);
        }
    }

    public static ArrayList<BaseActor> getList(Stage stage, String className) {
        ArrayList<BaseActor> list = new ArrayList<>();

        Class theClass = null;
        try {
            theClass = Class.forName(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Actor a : stage.getActors()) {
            if (theClass.isInstance(a)) {
                list.add((BaseActor) a);
            }
        }
        return list;
    }

    public static ArrayList<BaseActor> getList(Group group, String className) {
        ArrayList<BaseActor> list = new ArrayList<>();

        Class theClass = null;
        try {
            theClass = Class.forName(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Actor a : group.getChildren()) {
            if (theClass.isInstance(a)) {
                list.add((BaseActor) a);
            }
        }
        return list;
    }

    public void boundToWorld() {
        if (getX() < 0) {
            setX(0);
        }
        if (getX() + getWidth() > worldBounds.width) {
            setX(worldBounds.width - getWidth());
        }
        if (getY() < 0) {
            setY(0);
        }
        if (getY() + getHeight() > worldBounds.height) {
            setY(worldBounds.height - getHeight());
        }
    }

    public void wrapAroundWorld() {
        if (getX() + getWidth() < 0) {
            setX(worldBounds.width);
        }
        if (getX() > worldBounds.width) {
            setX(-getWidth());
        }
        if (getY() + getHeight() < 0) {
            setY(worldBounds.height);
        }
        if (getY() > worldBounds.height) {
            setY(-getHeight());
        }
    }

    public void alignCamera() {
        Camera camera = this.getStage().getCamera();
        Viewport viewport = this.getStage().getViewport();

        camera.position.set(getX() + getOriginX(), getY() + getOriginY(), 0);
        camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth / 2, worldBounds.width - camera.viewportWidth / 2);
        camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight / 2, worldBounds.height - camera.viewportHeight / 2);

        camera.update();
    }

    public static int count(Stage s, String className) {
        return getList(s, className).size();
    }

    public float getSpeed() {
        return velocityVector.len();
    }

    public void setMotionAngle(float angle) {
        velocityVector.setAngle(angle);
    }

    public float getMotionAngle() {
        return velocityVector.angle();
    }

    public boolean isMoving() {
        return getSpeed() > 0;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void accelerateAtAngle(float angle) {
        accelerationVector.add(new Vector2(acceleration, 0).setAngle(angle));
    }

    public void accelerateForward() {
        accelerateAtAngle(getRotation());
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setDeceleration(float deceleration) {
        this.deceleration = deceleration;
    }

    public void centerAtPosition(float x, float y) {
        setPosition(x - getWidth() / 2, y - getHeight() / 2);
    }

    public void centerAtActor(Actor other) {
        setPosition(other.getX() + other.getWidth() / 2 - getWidth() / 2, other.getY() + other.getHeight() / 2 - getHeight() / 2);
    }

    public void setOpacity(float opacity) {
        this.getColor().a = opacity;
    }

    public static void setWorldBounds(float width, float height) {
        worldBounds = new Rectangle(0, 0, width, height);
    }

    public static Rectangle getWorldBounds() {
        return worldBounds;
    }

    public void setVelocityVector(Vector2 vec) {
        this.velocityVector = vec;
    }

    public Vector2 getVelocityVector() {
        return velocityVector;
    }

    public static void setWorldBounds(BaseActor ba) {
        setWorldBounds(ba.getWidth(), ba.getHeight());
    }

    public Vector2 getPosition() {
        return new Vector2(getX(), getY());
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setRotationLimit(float rotationLimit) {
        this.rotationLimit = rotationLimit;
    }

    public void setInitialRotation(float initialRotation) {
        this.initialRotation = initialRotation;
    }
}
