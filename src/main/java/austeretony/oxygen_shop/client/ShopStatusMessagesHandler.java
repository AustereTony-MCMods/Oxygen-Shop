package austeretony.oxygen_shop.client;

import austeretony.oxygen_core.common.chat.ChatMessagesHandler;
import austeretony.oxygen_shop.common.main.EnumShopStatusMessage;
import austeretony.oxygen_shop.common.main.ShopMain;

public class ShopStatusMessagesHandler implements ChatMessagesHandler {

    @Override
    public int getModIndex() {
        return ShopMain.SHOP_MOD_INDEX;
    }

    @Override
    public String getMessage(int messageIndex) {
        return EnumShopStatusMessage.values()[messageIndex].localizedName();
    }
}
