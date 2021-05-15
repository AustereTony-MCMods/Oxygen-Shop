package austeretony.oxygen_shop.client.gui.menu;

import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_shop.client.settings.ShopSettings;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopMain;
import net.minecraft.util.ResourceLocation;

public class ShopScreenMenuEntry implements OxygenMenuEntry {

    private static final ResourceLocation ICON = new ResourceLocation(ShopMain.MOD_ID,
            "textures/gui/menu/shop.png");

    @Override
    public int getScreenId() {
        return ShopMain.SCREEN_ID_SHOP;
    }

    @Override
    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_shop.gui.shop.title");
    }

    @Override
    public int getPriority() {
        return 1800;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return ICON;
    }

    @Override
    public int getKeyCode() {
        return ShopConfig.SHOP_SCREEN_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return ShopSettings.ADD_SHOP_SCREEN_TO_OXYGEN_MENU.asBoolean() && ShopConfig.ENABLE_SHOP_ACCESS_CLIENT_SIDE.asBoolean();
    }
}
