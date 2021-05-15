package austeretony.oxygen_shop.client.gui.shop;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CartListEntry extends ListEntry<ShopEntry> {

    public static final Texture CHECK_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CHECK_ICONS)
            .size(ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE)
            .imageSize(ShopScreen.BTN_SIZE * 3, ShopScreen.BTN_SIZE)
            .build();
    public static final Texture MINUS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.MINUS_ICONS)
            .size(ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE)
            .imageSize(ShopScreen.BTN_SIZE * 3, ShopScreen.BTN_SIZE)
            .build();
    public static final Texture PLUS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.PLUS_ICONS)
            .size(ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE)
            .imageSize(ShopScreen.BTN_SIZE * 3, ShopScreen.BTN_SIZE)
            .build();

    private final int playerStock;
    private final CurrencyProperties properties;

    private NumberField numberField;
    private ImageButton purchaseButton;

    private boolean canAfford, removed;
    private int itemsAmount;
    private long totalPrice;

    public CartListEntry(@Nonnull ShopEntry shopEntry, int playerStock,
                         CurrencyProperties currencyProperties) {
        super("", shopEntry);
        this.playerStock = playerStock;
        properties = currencyProperties;
        itemsAmount = ShopManagerClient.instance().getCartItemAmount(shopEntry.getId());
    }

    public boolean isRemoved() {
        return removed;
    }

    @Override
    public void init() {
        addWidget(numberField = new NumberField(36, 8, 13, 1, 999)
                .setKeyPressListener((keyChar, keyCode) -> {
                    itemsAmount = (int) numberField.getTypedNumberAsLong();
                    if (itemsAmount == 0) {
                        itemsAmount = 1;
                    }
                    ShopManagerClient.instance().setCartItemAmount(entry.getId(), itemsAmount);
                    updateTotalPrice();
                }));
        numberField.setText(String.valueOf(itemsAmount));

        addWidget(new ImageButton(30, 10, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                MINUS_ICONS_TEXTURE, "")
                .setMouseClickListener((mouseX, mouseY, button) -> {
                    if (itemsAmount == 1) return;
                    itemsAmount = ShopManagerClient.instance().decrementCartItemAmount(entry.getId());
                    numberField.setText(String.valueOf(itemsAmount));
                    updateTotalPrice();
                }));
        addWidget(new ImageButton(50, 10, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                PLUS_ICONS_TEXTURE, "")
                .setMouseClickListener((mouseX, mouseY, button) -> {
                    if (itemsAmount == 999) return;
                    itemsAmount = ShopManagerClient.instance().incrementCartItemAmount(entry.getId());
                    numberField.setText(String.valueOf(itemsAmount));
                    updateTotalPrice();
                }));
        addWidget(new ImageButton(getWidth() - 7, 1, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                ShopScreen.CROSS_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.cart_entry.image_button.remove"))
                .setMouseClickListener((mouseX, mouseY, button) -> {
                    ShopManagerClient.instance().setCartItemAmount(entry.getId(), 0);
                    removed = true;
                }));
        addWidget(purchaseButton = new ImageButton(getWidth() - 14, 1, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                CHECK_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.cart_entry.image_button.purchase"))
                .setMouseClickListener((mouseX, mouseY, button) -> {
                    ShopManagerClient.instance().setCartItemAmount(entry.getId(), 0);
                    removed = true;
                    ShopManagerClient.instance().purchaseItems(Collections.singletonMap(entry.getId(), itemsAmount));
                }));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        totalPrice = entry.getPrice() * itemsAmount;
        canAfford = totalPrice <= OxygenClient.getWatcherValue(properties.getCurrencyIndex(), 0L);
        purchaseButton.setEnabled(canAfford);
        ((BuySection) screen.getWorkspace().getCurrentSection()).updateTotalCartPrice();
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

        float textScale = text.getScale() - .07F;
        if (isMouseOver()) {
            GUIUtils.drawString(String.valueOf(playerStock), 16, 1, textScale, getColorFromState(text), false);
        }
        if (entry.getQuantity() > 1) {
            GUIUtils.drawString(String.valueOf(entry.getQuantity()), 16, 10, textScale, getColorFromState(text), false);
        }

        int priceColorHex = entry.isDiscounted() ? CoreSettings.COLOR_TEXT_SPECIAL.asInt() : getColorFromState(text);
        if (!canAfford) {
            priceColorHex = CoreSettings.COLOR_TEXT_INACTIVE.asInt();
        }
        float textY = (properties.getIconHeight() - GUIUtils.getTextHeight(textScale)) / 2F + properties.getIconYOffset() + .5F;
        String priceValueStr = CommonUtils.formatCurrencyValue(entry.getPrice());
        float priceValueStrWidth = GUIUtils.getTextWidth(priceValueStr, textScale);
        GUIUtils.drawString(priceValueStr, 67 - priceValueStrWidth, textY, textScale,
                priceColorHex, false);

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(69 + properties.getIconXOffset(), 0, properties.getIconWidth(), properties.getIconHeight(),
                properties.getIconTexture(), 0, 0, properties.getIconWidth(), properties.getIconHeight());

        String totalPriceValueStr = CommonUtils.formatCurrencyValue(totalPrice);
        GUIUtils.drawString(totalPriceValueStr, 60, 11, textScale, priceColorHex, false);

        mouseX -= getX();
        mouseY -= getY();

        for (Widget widget : getWidgets()) {
            widget.draw(mouseX, mouseY, partialTicks);
        }

        GUIUtils.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        super.drawForeground(mouseX, mouseY, partialTicks);
        if (!isVisible()) return;
        if (mouseX >= getX() + 2 && mouseY >= getY() && mouseX < getX() + 18 && mouseY < getY() + getHeight()) {
            int offset = 16 + 8;
            int x = getX() + offset;
            int y = getY() + 2;

            ItemStack itemStack = entry.getStackWrapper().getItemStackCached();
            List<String> tooltipLines = GUIUtils.getItemStackToolTip(itemStack);
            float width = 0;
            for (String line : tooltipLines) {
                float lineWidth = GUIUtils.getTextWidth(line, CoreSettings.SCALE_TEXT_TOOLTIP.asFloat()) + 6F;
                if (lineWidth > width) {
                    width = lineWidth;
                }
            }
            int startX = getScreenX() + width + offset > getScreen().width ? (int) (x - width - offset) : x;

            drawToolTip(startX, y, tooltipLines);
        }
    }
}
