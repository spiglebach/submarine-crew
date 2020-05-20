package szm.orde4c.game.entity.submarine;

import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Arm;
import szm.orde4c.game.util.Assets;

public class ArmTool extends BaseActor {
    public ArmTool(Arm arm) {
        super(arm.getWidth(), 0, arm.getStage());
        loadTexture(Assets.instance.getTexture(Assets.BLANK));
        setSize(arm.getHeight(), arm.getHeight());
        setBoundaryRectangle();
        arm.addActor(this);
    }
}
