package austeretony.oxygen_shop.client.gui.shop.context;

import austeretony.oxygen_core.client.gui.base.context.ContextAction;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.special.callback.SelectQuantityCallback;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.gui.shop.ShopScreen;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;

public class ShopSellSelectItemQuantityContextAction implements ContextAction<ShopEntry> {

    @Nonnull
    @Override
    public String getName(ShopEntry entry) {
        return "oxygen_shop.gui.shop.selling.context.select_item_quantity";
    }

    @Override
    public boolean isValid(ShopEntry entry) {
        return getPlayerItemStock(entry.getStackWrapper()) >= entry.getQuantity();
    }

    @Override
    public void execute(ShopEntry entry) {
        Callback callback = new SelectQuantityCallback(
                "oxygen_shop.gui.shop.selling.callback.select_item_quantity",
                "oxygen_shop.gui.shop.selling.callback.select_item_quantity_message",
                1,
                getPlayerItemStock(entry.getStackWrapper()),
                1,
                selected -> ShopManagerClient.instance().sellItem(entry.getId(), selected.intValue()));
        Section.tryOpenCallback(callback);
    }

    private int getPlayerItemStock(ItemStackWrapper stackWrapper) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof ShopScreen) {
            return ((ShopScreen) screen).getPlayerItemStock(stackWrapper);
        }
        return 0;
    }
}
