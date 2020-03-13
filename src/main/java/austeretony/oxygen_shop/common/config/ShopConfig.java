package austeretony.oxygen_shop.common.config;

import java.util.List;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_shop.common.main.ShopMain;

public class ShopConfig extends AbstractConfig {

    public static final ConfigValue
    ENABLE_SHOP_MENU_KEY = ConfigValueUtils.getValue("client", "enable_shop_menu_key", true),
    SHOP_MENU_KEY = ConfigValueUtils.getValue("client", "shop_menu_key", 52),

    SHOP_CURRENCY_INDEX = ConfigValueUtils.getValue("server", "shop_currency_index", 0, true),
    SHOP_CART_SIZE = ConfigValueUtils.getValue("server", "shop_cart_size", 7, true),
    SHOP_MENU_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getValue("server", "shop_menu_operations_timeout_millis", 240000),
    ENABLE_SHOP_ACCESS_CLIENTSIDE = ConfigValueUtils.getValue("server", "enable_shop_access_clientside", true, true),
    ENABLE_SHOP_ACCESS = ConfigValueUtils.getValue("server", "enable_shop_access", true, true),
    ENABLE_SHOP_MANAGEMENT_INGAME = ConfigValueUtils.getValue("server", "enable_shop_management_ingame", true),
    SHOP_ITEMS_RECEIVING_MODE = ConfigValueUtils.getValue("server", "shop_item_receiving_mode", 0),
    ADVANCED_LOGGING = ConfigValueUtils.getValue("server", "advanced_logging", false);

    @Override
    public String getDomain() {
        return ShopMain.MODID;
    }

    @Override
    public String getVersion() {
        return ShopMain.VERSION_CUSTOM;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/shop.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_SHOP_MENU_KEY);
        values.add(SHOP_MENU_KEY);

        values.add(SHOP_CURRENCY_INDEX);
        values.add(SHOP_CART_SIZE);
        values.add(SHOP_MENU_OPERATIONS_TIMEOUT_MILLIS);
        values.add(ENABLE_SHOP_ACCESS_CLIENTSIDE);
        values.add(ENABLE_SHOP_ACCESS);
        values.add(ENABLE_SHOP_MANAGEMENT_INGAME);
        values.add(SHOP_ITEMS_RECEIVING_MODE);
        values.add(ADVANCED_LOGGING);
    }
}
