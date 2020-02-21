package austeretony.oxygen_shop.client.settings.gui;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.settings.SettingValue;
import austeretony.oxygen_core.common.settings.SettingValueUtils;

public enum EnumShopGUISetting {

    //Alignment

    SHOP_MENU_ALIGNMENT("alignment_shop_menu", EnumValueType.INT, String.valueOf(0));

    private final String key, baseValue;

    private final EnumValueType type;

    private SettingValue value;

    EnumShopGUISetting(String key, EnumValueType type, String baseValue) {
        this.key = key;
        this.type = type;
        this.baseValue = baseValue;
    }

    public SettingValue get() {
        if (this.value == null)
            this.value = OxygenManagerClient.instance().getClientSettingManager().getSettingValue(this.key);
        return this.value;
    }

    public static void register() {
        for (EnumShopGUISetting setting : values())
            OxygenManagerClient.instance().getClientSettingManager().register(SettingValueUtils.getValue(setting.type, setting.key, setting.baseValue));
    }
}
