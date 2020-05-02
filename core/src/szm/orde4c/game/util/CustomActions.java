package szm.orde4c.game.util;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.Action;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Arm;

public class CustomActions {
    public static Action changeAnimation(BaseActor currentActor, Animation animation) {
        return new Action() {
            @Override
            public boolean act(float delta) {
                currentActor.setAnimation(animation);
                return true;
            }
        };
    }

    public static Action extendToExtensionPercent(Arm currentArmActor, float extensionPercent) {
        currentArmActor.clearActions();
        float direction;
        if (currentArmActor.getExtensionPercent() > extensionPercent) {
            direction = -1;
        } else {
            direction = 1;
        }
        return new Action() {
            @Override
            public boolean act(float delta) {
                float currentExtensionPercent = currentArmActor.getExtensionPercent();
                if (direction > 0) {
                    if (currentExtensionPercent >= currentArmActor.getMaxExtensionPercent()
                            || currentExtensionPercent >= extensionPercent) {
                        return true;
                    }
                } else {
                    if (currentExtensionPercent <= currentArmActor.getMinExtensionPercent()
                            || currentExtensionPercent <= extensionPercent) {
                        return true;
                    }
                }

                currentArmActor.extendInDirection(direction);
                return false;
            }
        };
    }
}
