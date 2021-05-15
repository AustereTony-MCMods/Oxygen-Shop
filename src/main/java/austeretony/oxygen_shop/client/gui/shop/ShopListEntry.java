package austeretony.oxygen_shop.client.gui.shop;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class ShopListEntry extends ListEntry<ShopEntry> {

    private final boolean isAvailable;
    private final int playerStock;
    private final CurrencyProperties properties;

    public ShopListEntry(@Nonnull ShopEntry shopEntry, int playerStock, boolean isAvailable,
                         CurrencyProperties currencyProperties) {
        super("", shopEntry);
        this.isAvailable = isAvailable;
        this.playerStock = playerStock;
        properties = currencyProperties;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;

        GUIUtils.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), getColorFromState(fill));

        ItemStack itemStack = entry.getStackWrapper().getItemStackCached();
        GUIUtils.renderItemStack(itemStack, getX() + 2, getY(),
                CoreSettings.ENABLE_DURABILITY_BARS_GUI_DISPLAY.asBoolean());

        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        float textScale = text.getScale() - .08F;
        if (isMouseOver()) {
            GUIUtils.drawString(String.valueOf(playerStock), 16, 1, textScale, getColorFromState(text), true);
        }
        if (entry.getQuantity() > 1) {
            GUIUtils.drawString(String.valueOf(entry.getQuantity()), 16, 10, textScale, getColorFromState(text), true);
        }

        int nameColorHex = isAvailable ? getColorFromState(text) : CoreSettings.COLOR_TEXT_INACTIVE.asInt();
        float textY = (getHeight() - GUIUtils.getTextHeight(text.getScale() - .05F)) / 2F + .5F;
        String itemDisplayName = itemStack.getDisplayName();
        if (CoreSettings.ENABLE_RARITY_COLORS_GUI_DISPLAY.asBoolean()) {
            itemDisplayName = GUIUtils.getItemStackRarityColor(itemStack) + itemDisplayName;
        }
        GUIUtils.drawString(itemDisplayName, 30, textY, text.getScale() - .05F, nameColorHex, false);

        if (OxygenClient.isClientPlayerOperator()) {
            GUIUtils.drawString("id: " + entry.getId(), getWidth() - 54, 11, textScale - .05F,
                    CoreSettings.COLOR_TEXT_ADDITIONAL_ENABLED.asInt(), false);
        }

        int priceColorHex = entry.isDiscounted() ? CoreSettings.COLOR_TEXT_SPECIAL.asInt() : getColorFromState(text);
        if (!isAvailable) {
            priceColorHex = CoreSettings.COLOR_TEXT_INACTIVE.asInt();
        }
        textY = (getHeight() - GUIUtils.getTextHeight(textScale)) / 2F + .5F;
        String priceValueStr = CommonUtils.formatCurrencyValue(entry.getPrice());
        float priceValueStrWidth = GUIUtils.getTextWidth(priceValueStr, textScale);
        GUIUtils.drawString(priceValueStr, getWidth() - 12 - priceValueStrWidth, textY, textScale,
                priceColorHex, false);

        if (entry.isDiscounted()) {
            String basePriceValueStr = CommonUtils.formatCurrencyValue(entry.getBasePrice());
            float basePriceValueStrWidth = GUIUtils.getTextWidth(basePriceValueStr, textScale);
            GUIUtils.drawString(TextFormatting.STRIKETHROUGH + basePriceValueStr,
                    getWidth() - 12 - priceValueStrWidth - 4 - basePriceValueStrWidth, textY, textScale,
                    priceColorHex, false);

            String discountHint = (int) (entry.getDiscount() * 100) + "%";
            if (entry.getType() == ShopEntry.Type.BUY) {
                discountHint = "-" + discountHint;
            } else if (entry.getType() == ShopEntry.Type.SELLING) {
                discountHint = "+" + discountHint;
            }
            float discountHintWidth = GUIUtils.getTextWidth(discountHint, textScale);
            GUIUtils.drawString(discountHint, getWidth() - 56 - discountHintWidth,
                    textY, textScale, priceColorHex, false);
        }

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(getWidth() - 10 + properties.getIconXOffset(),
                (getHeight() - (properties.getIconHeight() + properties.getIconYOffset())) / 2F, properties.getIconWidth(), properties.getIconHeight(),
                properties.getIconTexture(), 0, 0, properties.getIconWidth(), properties.getIconHeight());

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        if (mouseX >= getX() + 2 && mouseY >= getY() && mouseX < getX() + 18 && mouseY < getY() + getHeight()) {
            int y = getY() + (int) ((getHeight() - Widget.TOOLTIP_HEIGHT) / 2F);
            drawToolTip(getX() + 30 - 3, y, entry.getStackWrapper().getItemStackCached());
        }
    }
}
