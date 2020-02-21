package austeretony.oxygen_shop.client.gui.shop;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.OxygenGUIUtils;
import austeretony.oxygen_core.client.gui.elements.OxygenIndexedPanelEntry;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_shop.common.ShopOffer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class ShopOfferPanelEntry extends OxygenIndexedPanelEntry<ShopOffer> {

    private final String amountStr, priceStr, discountStr;

    private String playerStockStr;

    private final boolean singleItem, enableDurabilityBar, discount;

    private boolean available;

    private CurrencyProperties currencyProperties;

    public ShopOfferPanelEntry(ShopOffer offer, int playerStock, boolean available, CurrencyProperties properties) {
        super(offer);
        this.playerStockStr = String.valueOf(playerStock);
        this.amountStr = String.valueOf(offer.getAmount());
        this.priceStr = OxygenUtils.formatCurrencyValue(String.valueOf(offer.getPrice()));
        this.singleItem = offer.getAmount() == 1;

        this.available = available;
        this.currencyProperties = properties;

        this.discount = offer.getDiscount() > 0;
        this.discountStr = ClientReference.localize("oxygen_shop.gui.shop.offer.discount", offer.getDiscount() + "%");

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? (available ? offer.getStackWrapper().getCachedItemStack().getRarity().rarityColor : "") + offer.getStackWrapper().getCachedItemStack().getDisplayName() : offer.getStackWrapper().getCachedItemStack().getDisplayName());
        this.setDynamicBackgroundColor(EnumBaseGUISetting.ELEMENT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.ELEMENT_HOVERED_COLOR.get().asInt());
        this.setTextDynamicColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_DISABLED_COLOR.get().asInt(), EnumBaseGUISetting.TEXT_HOVERED_COLOR.get().asInt());
        this.setDebugColor(EnumBaseGUISetting.INACTIVE_ELEMENT_COLOR.get().asInt());
        this.setTooltipBackgroundColor(EnumBaseGUISetting.STATUS_TEXT_COLOR.get().asInt());
        this.requireDoubleClick();
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (this.isVisible()) {      
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

            int color = this.getEnabledBackgroundColor();                     
            if (!this.isEnabled())                  
                color = this.getDisabledBackgroundColor();
            else if (this.isHovered())                  
                color = this.getHoveredBackgroundColor();      

            int third = this.getWidth() / 3;
            OxygenGUIUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
            drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
            OxygenGUIUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

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
            GlStateManager.translate(this.getWidth() - 12.0F - this.textWidth(this.priceStr, this.getTextScale() - 0.05F), (this.getHeight() - this.textHeight(this.getTextScale() - 0.05F)) / 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F); 
            this.mc.fontRenderer.drawString(this.priceStr, 0, 0, this.available ? (this.discount ? this.getTooltipBackgroundColor() : color) : this.getDebugColor(), false);
            GlStateManager.popMatrix();    

            GlStateManager.pushMatrix();           
            GlStateManager.translate(28.0F, (this.getHeight() - this.textHeight(this.getTextScale() + 0.05F)) / 2.0F, 0.0F);            
            GlStateManager.scale(this.getTextScale() + 0.05F, this.getTextScale() + 0.05F, 0.0F);           
            this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, this.available ? color : this.getDebugColor(), false);
            GlStateManager.popMatrix();     

            if (this.discount) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(130.0F, (this.getHeight() - this.textHeight(this.getTextScale() - 0.05F)) / 2.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale() - 0.05F, this.getTextScale() - 0.05F, 0.0F);           
                this.mc.fontRenderer.drawString(this.discountStr, 0, 0, this.getTooltipBackgroundColor(), false);
                GlStateManager.popMatrix(); 
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            GlStateManager.enableBlend(); 
            this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
            GUIAdvancedElement.drawCustomSizedTexturedRect(this.getWidth() - 10 + this.currencyProperties.getXOffset(), (this.getHeight() - this.currencyProperties.getIconHeight()) / 2 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());            
            GlStateManager.disableBlend();

            GlStateManager.popMatrix();
        }     
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() + 2 && mouseY >= this.getY() && mouseX < this.getX() + 18 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.index.getStackWrapper().getCachedItemStack(), mouseX + 6, mouseY);
    }

    public void setPlayerStock(int value) {
        this.playerStockStr = String.valueOf(value);
    }

    public ShopOfferPanelEntry setAvailable(boolean flag) {
        this.available = flag;
        if (flag)
            this.setDisplayText(EnumBaseClientSetting.ENABLE_RARITY_COLORS.get().asBoolean() ? (flag ? this.index.getStackWrapper().getCachedItemStack().getRarity().rarityColor : "") + this.index.getStackWrapper().getCachedItemStack().getDisplayName() : this.index.getStackWrapper().getCachedItemStack().getDisplayName());
        else
            this.setDisplayText(this.index.getStackWrapper().getCachedItemStack().getDisplayName());
        return this;
    }

    public boolean isAvailable() {
        return this.available;
    }
}
