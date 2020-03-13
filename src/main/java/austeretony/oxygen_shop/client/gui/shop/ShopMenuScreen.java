package austeretony.oxygen_shop.client.gui.shop;

import java.util.Map;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.InventoryProviderClient;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_shop.client.gui.menu.ShopMenuEntry;
import austeretony.oxygen_shop.client.settings.gui.EnumShopGUISetting;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopMain;

public class ShopMenuScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry SHOP_MENU_ENTRY = new ShopMenuEntry();

    private CurrencyProperties currencyProperties;

    private final Map<ItemStackWrapper, Integer> inventoryContent;

    private ShopSection shopSection;

    public ShopMenuScreen() {
        OxygenHelperClient.syncData(ShopMain.SHOP_OFFERS_DATA_ID);

        this.currencyProperties = OxygenHelperClient.getCurrencyProperties(ShopConfig.SHOP_CURRENCY_INDEX.asInt());
        if (this.currencyProperties == null)
            this.currencyProperties = OxygenHelperClient.getCurrencyProperties(OxygenMain.COMMON_CURRENCY_INDEX);
        this.inventoryContent = InventoryProviderClient.getPlayerInventory().getInventoryContent(ClientReference.getClientPlayer());
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        EnumGUIAlignment alignment = EnumGUIAlignment.CENTER;
        switch (EnumShopGUISetting.SHOP_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            alignment = EnumGUIAlignment.LEFT;
            break;
        case 0:
            alignment = EnumGUIAlignment.CENTER;
            break;
        case 1:
            alignment = EnumGUIAlignment.RIGHT;
            break;    
        default:
            alignment = EnumGUIAlignment.CENTER;
            break;
        }
        return new GUIWorkspace(this, 323, 197).setAlignment(alignment, 0, 0);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.shopSection = new ShopSection(this));
    }

    @Override
    protected AbstractGUISection getDefaultSection() {
        return this.shopSection;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {}

    @Override
    protected boolean doesGUIPauseGame() {
        return false;
    }

    public void offersSynchronized() {
        this.shopSection.offersSynchronized();
    }

    public void purchaseSuccessful(long balance, long offerId) {
        this.shopSection.purchaseSuccessful(balance, offerId);
    }

    public CurrencyProperties getCurrencyProperties() {
        return this.currencyProperties;
    }

    public int getEqualStackAmount(ItemStackWrapper stackWrapper) {
        Integer amount = this.inventoryContent.get(stackWrapper);
        return amount == null ? 0 : amount.intValue();
    }

    public ShopSection getShopSection() {
        return this.shopSection;
    }
}
