package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.graphics.g2d.Animation;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.Assets;

public class ArmTool extends BaseActor {
    private Animation drill;
    private Animation cutter;

    private float size;

    public ArmTool(Arm arm) {
        super(arm.getWidth(), 0, arm.getStage());
        size = arm.getHeight();
        cutter = loadTexture(Assets.instance.getTexture(Assets.SUBMARINE_CUTTER));
        drill = loadAnimationFromSheet(Assets.instance.getTexture(Assets.SUBMARINE_DRILL), 1, 4, 0.1f, true);
        setSize(size, size);
        setBoundaryRectangle();
        arm.addActor(this);
    }

    public void switchToDrill() {
        if (!animation.equals(drill)) {
            setAnimation(drill);
            setSize(size, size);
        }
    }

    public void switchToCutter() {
        if (!animation.equals(cutter)) {
            setAnimation(cutter);
            setSize(size, size);
        }
    }
}
