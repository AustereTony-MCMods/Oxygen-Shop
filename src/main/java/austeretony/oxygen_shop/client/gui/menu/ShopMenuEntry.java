package austeretony.oxygen_shop.client.gui.menu;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_shop.client.ShopMenuManager;
import austeretony.oxygen_shop.client.settings.EnumShopClientSetting;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.EnumShopPrivilege;
import austeretony.oxygen_shop.common.main.ShopMain;

public class ShopMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return ShopMain.SHOP_SCREEN_ID;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_shop.gui.shop.title");
    }

    @Override
    public int getKeyCode() {
        return ShopConfig.SHOP_MENU_KEY.asInt();
    }

    @Override
    public boolean isValid() {
        return ShopConfig.ENABLE_SHOP_ACCESS_CLIENTSIDE.asBoolean() 
                && PrivilegesProviderClient.getAsBoolean(EnumShopPrivilege.SHOP_ACCESS.id(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())
                && EnumShopClientSetting.ADD_SHOP_MENU.get().asBoolean();
    }

    @Override
    public void open() {
        ShopMenuManager.openShopMenu();
    }
}
