package austeretony.oxygen_shop.client.settings;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.settings.SettingType;
import austeretony.oxygen_core.client.settings.SettingValue;
import austeretony.oxygen_core.client.settings.gui.SettingWidgets;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_shop.common.main.ShopMain;

public final class ShopSettings {

    public static final SettingValue
    SHOP_SCREEN_ALIGNMENT = OxygenClient.registerSetting(ShopMain.MOD_ID, SettingType.INTERFACE, "Shop", "alignment",
            ValueType.STRING, "shop_screen_alignment", Alignment.CENTER.toString(), SettingWidgets.screenAlignmentList()),

    ADD_SHOP_SCREEN_TO_OXYGEN_MENU = OxygenClient.registerSetting(ShopMain.MOD_ID, SettingType.COMMON, "Shop", "oxygen_menu",
            ValueType.BOOLEAN, "add_shop_screen", true, SettingWidgets.checkBox());

    private ShopSettings() {}

    public static void register() {}
}
