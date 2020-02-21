package austeretony.oxygen_shop.client.gui.shop;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenIndexedPanelEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.common.ShopOffer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class CartPanelEntry extends OxygenIndexedPanelEntry<ShopOffer> {

    private final String amountStr, priceStr;

    private String playerStockStr, cartItemAmountStr, totalPriceStr;

    private final boolean singleItem, enableDurabilityBar;

    private boolean available, remove;

    private final CurrencyProperties currencyProperties;

    private OxygenTexturedButton decrementButton, incrementButton, removeButton, purchaseButton;

    //cache

    private long totalPrice;

    public CartPanelEntry(ShopOffer offer, int playerStock, boolean available, CurrencyProperties properties, int cartItemAmount) {
        super(offer);
        this.playerStockStr = String.valueOf(playerStock);
        this.amountStr = String.valueOf(offer.getAmount());
        this.priceStr = OxygenUtils.formatCurrencyValue(String.valueOf(offer.getPrice()));
        this.totalPrice = offer.getPrice() * cartItemAmount;
        this.totalPriceStr = OxygenUtils.formatCurrencyValue(String.valueOf(this.totalPrice));
        this.singleItem = offer.getAmount() == 1;
        this.available = available;
        this.currencyProperties = properties;
        this.cartItemAmountStr = String.valueOf(cartItemAmount);

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setDebugColor(EnumBaseGUISetting.INACTIVE_ELEMENT_COLOR.get().asInt());
    }

    @Override
    public void init() {
        this.decrementButton = new OxygenTexturedButton(25, 11, 4, 4, ShopMenuScreen.MINUS_ICONS, 4, 4, "").initScreen(this.getScreen());
        this.incrementButton = new OxygenTexturedButton(42, 11, 4, 4, ShopMenuScreen.PLUS_ICONS, 4, 4, "").initScreen(this.getScreen());
        this.removeButton = new OxygenTexturedButton(this.getWidth() - 8, 1, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, "").initScreen(this.getScreen());
        this.purchaseButton = new OxygenTexturedButton(this.getWidth() - 8, 10, 5, 5, OxygenGUITextures.CHECK_ICONS, 5, 5, "").initScreen(this.getScreen());

        this.purchaseButton.setEnabled(this.available);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {      
            ///

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            int color = this.getEnabledBackgroundColor();                     
            if (!this.isEnabled())                  
                color = this.getDisabledBackgroundColor();
            else if (this.isHovered())                  
                color = this.getHoveredBackgroundColor();      

            int third = this.getWidth() / 3;
            OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

            GlStateManager.popMatrix();

            ///

            RenderHelper.enableGUIStandardItemLighting();            
            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.index.getStackWrapper().getCachedItemStack(), this.getX() + 2, this.getY());  

            if (this.enableDurabilityBar) {
                FontRenderer font = this.index.getStackWrapper().getCachedItemStack().getItem().getFontRenderer(this.index.getStackWrapper().getCachedItemStack());
                if (font == null) 
                    font = this.mc.fontRenderer;
                this.itemRender.renderItemOverlayIntoGUI(font, this.index.getStackWrapper().getCachedItemStack(), this.getX() + 2, this.getY(), null);
            }

            GlStateManager.disableDepth();
            RenderHelper.disableStandardItemLighting();

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            color = this.getEnabledTextColor();
            if (!this.isEnabled())                  
                color = this.getDisabledTextColor();           
            else if (this.isHovered())                                          
                color = this.getHoveredTextColor();

            if (this.isHovered()) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 1.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F);   
                this.mc.fontRenderer.drawString(this.playerStockStr, 0, 0, color, true); 
                GlStateManager.popMatrix();
            }

            if (!this.singleItem) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F);   
                this.mc.fontRenderer.drawString(this.amountStr, 0, 0, color, true);           
                GlStateManager.popMatrix();      
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(46.0F - this.textWidth(this.priceStr, this.getTextScale() - 0.05F), 1 + ((this.currencyProperties.getIconWidth() - this.textHeight(this.getTextScale() - 0.05F)) / 2) + this.currencyProperties.getXOffset(), 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
            this.mc.fontRenderer.drawString(this.priceStr, 0, 0, color, false);
            GlStateManager.popMatrix();    

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.enableBlend(); 
            this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
            GUIAdvancedElement.drawCustomSizedTexturedRect(48 + this.currencyProperties.getXOffset(), 1 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());            
            GlStateManager.disableBlend();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.pushMatrix();           
            GlStateManager.translate(35.0F - this.textWidth(this.cartItemAmountStr, this.getTextScale() - 0.3F), 11.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
            this.mc.fontRenderer.drawString(this.cartItemAmountStr, 0, 0, this.available ? color : this.getDebugColor(), false);
            GlStateManager.popMatrix();   

            GlStateManager.pushMatrix();           
            GlStateManager.translate(50.0F, 11.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
            this.mc.fontRenderer.drawString(this.totalPriceStr, 0, 0, this.available ? color : this.getDebugColor(), false);
            GlStateManager.popMatrix();   

            this.decrementButton.draw(mouseX, mouseY);
            this.incrementButton.draw(mouseX, mouseY);
            this.removeButton.draw(mouseX, mouseY);
            this.purchaseButton.draw(mouseX, mouseY);

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.index.getStackWrapper().getCachedItemStack(), mouseX + 6, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {       
        if (this.decrementButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            int amount = ShopManagerClient.instance().getShoppingCartManager().decrementItemAmount(this.index.getId());
            this.cartItemAmountStr = String.valueOf(amount);

            this.totalPrice = this.index.getPrice() * amount;
            this.totalPriceStr = OxygenUtils.formatCurrencyValue(String.valueOf(this.totalPrice));
            this.available = this.totalPrice <= WatcherHelperClient.getLong(this.currencyProperties.getIndex());
            this.purchaseButton.setEnabled(this.available);

            ((ShopSection) this.screen.getWorkspace().getCurrentSection()).updateTotalCartPrice();
        } else if (this.incrementButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            int amount = ShopManagerClient.instance().getShoppingCartManager().incrementItemAmount(this.index.getId());
            this.cartItemAmountStr = String.valueOf(amount);

            this.totalPrice = this.index.getPrice() * amount;
            this.totalPriceStr = OxygenUtils.formatCurrencyValue(String.valueOf(this.totalPrice));
            this.available = this.totalPrice <= WatcherHelperClient.getLong(this.currencyProperties.getIndex());
            this.purchaseButton.setEnabled(this.available);

            ((ShopSection) this.screen.getWorkspace().getCurrentSection()).updateTotalCartPrice();
        } else if (this.removeButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            ShopManagerClient.instance().getShoppingCartManager().removeItemFromCart(this.index.getId());
            this.remove = true;
        } else if (this.purchaseButton.mouseClicked(mouseX, mouseY, mouseButton))
            ShopManagerClient.instance().getShoppingCartManager().purchaseItem(this.index.getId());
        return false;
    }

    @Override
    public void mouseOver(int mouseX, int mouseY) {
        this.decrementButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());
        this.incrementButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());
        this.removeButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());
        this.purchaseButton.mouseOver(mouseX - this.getX(), mouseY - this.getY());

        this.setHovered(this.isEnabled() && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight());   
    }

    public void setPlayerStock(int value) {
        this.playerStockStr = String.valueOf(value);
    }

    public void setAvailable(boolean flag) {
        this.available = flag;
        if (flag)
            this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? (flag ? this.index.getStackWrapper().getCachedItemStack().getRarity().rarityColor : "") + this.index.getStackWrapper().getCachedItemStack().getDisplayName() : this.index.getStackWrapper().getCachedItemStack().getDisplayName());
        else
            this.setDisplayText(this.index.getStackWrapper().getCachedItemStack().getDisplayName());
    }

    public boolean isAvailable() {
        return this.available;
    }

    public long getTotalPrice() {
        return this.totalPrice;
    }

    public void remove() {
        this.remove = true;
    }

    public boolean isRemoved() {
        return this.remove;
    }
}
