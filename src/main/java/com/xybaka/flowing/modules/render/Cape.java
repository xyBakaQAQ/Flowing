package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class Cape extends Module {
    private final ModeSetting mode = mode("Mode", "FDP", "FDP", "Hanabi", "AzureWare", "Envy", "ETB", "PowerX");
    private final ModeSetting fdpMode = mode("FDP Mode", "Aurora", "Aurora", "Forest", "Hot", "Indigo", "Lava", "Lime", "Night")
            .visibleWhen(() -> mode.is("FDP"));
    private final ModeSetting hanabiMode = mode("Hanabi Mode", "Dark", "Dark", "Light")
            .visibleWhen(() -> mode.is("Hanabi"));
    private final ModeSetting azureWareMode = mode("AzureWare Mode", "Dark", "Dark", "Light")
            .visibleWhen(() -> mode.is("AzureWare"));
    private final BooleanSetting overrideCape = bool("Override Cape", true);

    public Cape() {
        super("Cape", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean shouldUseCape() {
        return isEnabled();
    }

    public boolean shouldOverrideCape() {
        return overrideCape.getValue();
    }

    public Identifier getCapeTexture() {
        return switch (mode.getValue()) {
            case "FDP" -> getFdpCapeTexture();
            case "Hanabi" -> getHanabiCapeTexture();
            case "AzureWare" -> getAzureWareCapeTexture();
            case "ETB" -> Identifier.of("flowing", "capes/etb.png");
            case "PowerX" -> Identifier.of("flowing", "capes/powerx.png");
            default -> Identifier.of("flowing", "capes/envy.png");
        };
    }

    private Identifier getFdpCapeTexture() {
        return switch (fdpMode.getValue()) {
            case "Forest" -> Identifier.of("flowing", "capes/fdp/forest.png");
            case "Hot" -> Identifier.of("flowing", "capes/fdp/hot.png");
            case "Indigo" -> Identifier.of("flowing", "capes/fdp/indigo.png");
            case "Lava" -> Identifier.of("flowing", "capes/fdp/lava.png");
            case "Lime" -> Identifier.of("flowing", "capes/fdp/lime.png");
            case "Night" -> Identifier.of("flowing", "capes/fdp/night.png");
            default -> Identifier.of("flowing", "capes/fdp/aurora.png");
        };
    }

    private Identifier getHanabiCapeTexture() {
        return switch (hanabiMode.getValue()) {
            case "Light" -> Identifier.of("flowing", "capes/hanabi/lighthanabi.png");
            default -> Identifier.of("flowing", "capes/hanabi/darkhanabi.png");
        };
    }

    private Identifier getAzureWareCapeTexture() {
        return switch (azureWareMode.getValue()) {
            case "Light" -> Identifier.of("flowing", "capes/azureware/lightazureware.png");
            default -> Identifier.of("flowing", "capes/azureware/darkazureware.png");
        };
    }
}
