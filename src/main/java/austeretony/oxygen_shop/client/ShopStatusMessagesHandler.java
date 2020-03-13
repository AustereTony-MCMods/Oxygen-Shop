package austeretony.oxygen_shop.client;

import austeretony.oxygen_core.client.chat.MessageFormatter;
import austeretony.oxygen_core.common.chat.ChatMessagesHandler;
import austeretony.oxygen_shop.common.main.EnumShopStatusMessage;
import austeretony.oxygen_shop.common.main.ShopMain;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class ShopStatusMessagesHandler implements ChatMessagesHandler {

    private final MessageFormatter formatter = (messageIndex, args)->{
        if (messageIndex == EnumShopStatusMessage.PURCHASE_SUCCESSFUL_MAIL.ordinal()) {
            ITextComponent 
            message = new TextComponentTranslation("oxygen_shop.status.message.purchaseSuccessfulMail"),
            command = new TextComponentTranslation("oxygen_shop.message.clickHere");

            message.getStyle().setItalic(true);
            message.getStyle().setColor(TextFormatting.AQUA);
            command.getStyle().setItalic(true);
            command.getStyle().setUnderlined(true);
            command.getStyle().setColor(TextFormatting.WHITE);
            command.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/oxygenc mail"));

            return message.appendSibling(command);
        }
        return null;
    };

    @Override
    public int getModIndex() {
        return ShopMain.SHOP_MOD_INDEX;
    }

    @Override
    public String getMessage(int messageIndex) {
        return EnumShopStatusMessage.values()[messageIndex].localizedName();
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return this.formatter;
    }
}
