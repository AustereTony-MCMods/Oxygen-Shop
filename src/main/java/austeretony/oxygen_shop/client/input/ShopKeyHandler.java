package austeretony.oxygen_shop.client.input;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_shop.client.ShopMenuManager;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.EnumShopPrivilege;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ShopKeyHandler {

    private KeyBinding shopMenuKeybinding;

    public ShopKeyHandler() {        
        if (ShopConfig.ENABLE_SHOP_MENU_KEY.asBoolean() && !OxygenGUIHelper.isOxygenMenuEnabled())
            ClientReference.registerKeyBinding(this.shopMenuKeybinding = new KeyBinding("key.oxygen_shop.shopMenu", ShopConfig.SHOP_MENU_KEY.asInt(), "Oxygen"));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {        
        if (this.shopMenuKeybinding != null && this.shopMenuKeybinding.isPressed())
            if (ShopConfig.ENABLE_SHOP_ACCESS_CLIENTSIDE.asBoolean() 
                    && PrivilegesProviderClient.getAsBoolean(EnumShopPrivilege.SHOP_ACCESS.id(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean()))
                ShopMenuManager.openShopMenu();
    }

    public KeyBinding getShopMenuKeybinding() {
        return this.shopMenuKeybinding;
    }
}
