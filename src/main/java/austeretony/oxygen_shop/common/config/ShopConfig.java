package austeretony.oxygen_shop.common.config;

import austeretony.oxygen_core.common.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_shop.common.main.ShopMain;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ShopConfig extends AbstractConfig {

    public static final ConfigValue
            ENABLE_SHOP_SCREEN_KEY = ConfigValueUtils.getBoolean("client", "enable_shop_screen_key", true),
            SHOP_SCREEN_KEY = ConfigValueUtils.getInt("client", "shop_screen_key", Keyboard.KEY_PERIOD),

    SHOP_CURRENCY_INDEX = ConfigValueUtils.getInt("server", "shop_currency_index", OxygenMain.CURRENCY_COINS, true),
            SHOP_CART_SIZE = ConfigValueUtils.getInt("server", "shop_cart_size", 7, true),
            SHOP_MENU_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getInt("server", "shop_screen_operations_timeout_millis", 5 * 60 * 1000),
            ENABLE_SHOP_ACCESS_CLIENT_SIDE = ConfigValueUtils.getBoolean("server", "enable_shop_access_client_side", true, true),
            ENABLE_SHOP_ACCESS = ConfigValueUtils.getBoolean("server", "enable_shop_access", true, true),
            ENABLE_SHOP_MANAGEMENT_IN_GAME = ConfigValueUtils.getBoolean("server", "enable_shop_management_in_game", true);

    @Override
    public String getDomain() {
        return ShopMain.MOD_ID;
    }

    @Override
    public String getVersion() {
        return ShopMain.VERSION_CUSTOM;
    }

    @Override
    public String getFileName() {
        return "shop.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_SHOP_SCREEN_KEY);
        values.add(SHOP_SCREEN_KEY);

        values.add(SHOP_CURRENCY_INDEX);
        values.add(SHOP_CART_SIZE);
        values.add(SHOP_MENU_OPERATIONS_TIMEOUT_MILLIS);
        values.add(ENABLE_SHOP_ACCESS_CLIENT_SIDE);
        values.add(ENABLE_SHOP_ACCESS);
        values.add(ENABLE_SHOP_MANAGEMENT_IN_GAME);
    }
}
