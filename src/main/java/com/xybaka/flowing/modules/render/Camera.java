package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public final class Camera extends Module {
    private final BooleanSetting cameraNoClip = bool("CameraNoClip", true);
    private final BooleanSetting noHurtCam = bool("NoHurtCam", false);
    private final BooleanSetting lowFireOverlay = bool("LowFireOverlay", true);
    private final BooleanSetting noBlindness = bool("NoBlindness", true);
    private final BooleanSetting noNausea = bool("NoNausea", true);
    private final BooleanSetting noPumpkinOverlay = bool("NoPumpkinOverlay", true);

    public Camera() {
        super("Camera", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    public boolean cameraNoClip() {
        return isEnabled() && cameraNoClip.getValue();
    }

    public boolean noHurtCam() {
        return isEnabled() && noHurtCam.getValue();
    }

    public boolean lowFireOverlay() {
        return isEnabled() && lowFireOverlay.getValue();
    }

    public boolean noBlindness() {
        return isEnabled() && noBlindness.getValue();
    }

    public boolean noNausea() {
        return isEnabled() && noNausea.getValue();
    }

    public boolean noPumpkinOverlay() {
        return isEnabled() && noPumpkinOverlay.getValue();
    }
}
