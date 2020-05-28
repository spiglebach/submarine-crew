package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.Damageable;

public abstract class RandomizedAreaObject extends BaseActor implements Damageable {
    protected Area area;
    protected float health;

    public RandomizedAreaObject(Area area, float minimumSize, float maximumSize, int minimumHealth, int maximumHealth, Stage s) {
        super(0, 0, s);
        this.area = area;

        randomizeTexture();
        randomizeSize(minimumSize, maximumSize);
        randomizeHealth(minimumHealth, maximumHealth);
        randomizeRotation();
        randomizePositionInsideArea();
    }

    private void randomizeSize(float minimum, float maximum) {
        float randomSize = MathUtils.random(minimum, maximum);
        setSize(randomSize, randomSize);
        setBoundaryPolygon(8);
    }

    public void randomizeHealth(int minimum, int maximum) {
        int randomHealth = MathUtils.random(minimum, maximum);
        health = randomHealth;
    }

    private void randomizeRotation() {
        float randomRotation = MathUtils.random(360);
        float areaRotation = area.getRotation();
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        setRotation(areaRotation + randomRotation);
    }

    private void randomizePositionInsideArea() {
        float areaX = area.getX();
        float areaY = area.getY();
        float areaOriginX = area.getOriginX();
        float areaOriginY = area.getOriginY();

        float minimumX = 0;
        float minimumY = 0;
        float maximumX = area.getWidth() - getWidth();
        float maximumY = area.getHeight() - getHeight();

        float randomX = MathUtils.random(minimumX, maximumX);
        float randomY = MathUtils.random(minimumY, maximumY);

        Vector2 transformedPosition;
        Vector2 areaPosition = new Vector2(areaX, areaY);
        Vector2 areaOrigin = new Vector2(areaOriginX, areaOriginY);
        Vector2 randomPosition = new Vector2(randomX, randomY);

        Vector2 parentOriginToPosition = (randomPosition.cpy().sub(areaOrigin)).rotate(area.getRotation());

        transformedPosition = areaPosition.cpy();
        transformedPosition = transformedPosition.add(areaOrigin);
        transformedPosition = transformedPosition.add(parentOriginToPosition);

        setPosition(transformedPosition.x, transformedPosition.y);
    }

    @Override
    public boolean overlaps(BaseActor other) {
        if (!area.overlaps(other)) {
            return false;
        }
        return Intersector.overlapConvexPolygons(getBoundaryPolygon(), other.getBoundaryPolygon());
    }

    @Override
    public Vector2 preventOverlap(BaseActor other) {
        if (!area.overlaps(other)) {
            return null;
        }

        return super.preventOverlap(other);
    }

    protected abstract void randomizeTexture();

    @Override
    public void damage(float damage) {
        health -= damage;
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }
}
