package szm.orde4c.game.base;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class DragAndDropActor extends BaseActor {

    private float grabOffsetX;
    private float grabOffsetY;

    private float startPositionX;
    private float startPositionY;

    private DropTargetActor dropTarget;
    private boolean draggable;

    public DragAndDropActor(float x, float y, Stage s) {
        super(x, y, s);
        draggable = true;

        startPositionX = x;
        startPositionY = y;

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!draggable) {
                    return false;
                }
                startPositionX = x;
                startPositionY = y;
                grabOffsetX = x;
                grabOffsetY = y;

                toFront();

                addAction(Actions.scaleTo(1.1f, 1.1f, 0.25f));
                onDragStart();
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!draggable) {
                    return;
                }
                dropTarget = null;
                float closestDistance = Float.MAX_VALUE;

                for (BaseActor targetActor : BaseActor.getList(getStage(), "com.mygdx.game.DropTargetActor")) {
                    DropTargetActor target = (DropTargetActor) targetActor;
                    if (target.isTargetable() && overlaps(target)) {
                        float currentDistance = Vector2.dst(getX(), getY(), target.getX(), target.getY());
                        if (currentDistance < closestDistance) {
                            dropTarget = target;
                            closestDistance = currentDistance;
                        }
                    }
                }
                addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f));
                onDrop();
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!draggable) {
                    return;
                }
                float deltaX = x - grabOffsetX;
                float deltaY = y - grabOffsetY;

                moveBy(deltaX, deltaY);
            }
        });
    }

    public void moveToActor(BaseActor other) {
        float x = other.getX() + (other.getWidth() - getWidth()) / 2;
        float y = other.getY() + (other.getHeight() - getHeight()) / 2;
        addAction(Actions.moveTo(x, y, 0.50f, Interpolation.pow3));
    }

    public void moveToStart() {
        addAction(Actions.moveTo(startPositionX, startPositionY, 0.50f, Interpolation.pow3));
    }

    public void onDragStart() {

    }

    public void onDrop() {

    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    public boolean hasDropTarget() {
        return dropTarget != null;
    }

    public DropTargetActor getDropTarget() {
        return dropTarget;
    }

    public void setDropTarget(DropTargetActor dropTarget) {
        this.dropTarget = dropTarget;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }
}
