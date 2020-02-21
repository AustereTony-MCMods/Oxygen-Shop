package austeretony.oxygen_shop.client.gui.shop;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemCategory;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemSubCategory;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.common.ShopOffer;
import austeretony.oxygen_shop.common.config.ShopConfig;

public class ShopSection extends AbstractGUISection {

    private final ShopMenuScreen screen;

    private OxygenButton purchaseButton;

    private OxygenTexturedButton applyLatestFiltersButton, resetFiltersButton;

    private OxygenScrollablePanel offersPanel, cartPanel;

    private OxygenDropDownList categoriesList, subCategoriesList;

    private OxygenTextField textField;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue, totalPriceValue;

    //filters

    private ItemCategory currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;

    private ItemSubCategory currentSubCategory;

    //cache

    private static ItemCategory categoryCached = ItemCategoriesPresetClient.COMMON_CATEGORY;

    private static ItemSubCategory subCategoryCached;

    private static String textSearchCached = "";

    public ShopSection(ShopMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_shop.gui.shop.title"));
    }

    @Override
    public void init() {
        this.addElement(new ShopBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, this.getDisplayText(), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.applyLatestFiltersButton = new OxygenTexturedButton(205, 20, 5, 5, OxygenGUITextures.CLOCK_ICONS, 5, 5, ClientReference.localize("oxygen_shop.gui.shop.tooltip.latestFilters")));         
        this.addElement(this.resetFiltersButton = new OxygenTexturedButton(212, 20, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_shop.gui.shop.tooltip.resetFilters")));         

        //offers panel
        this.addElement(this.offersPanel = new OxygenScrollablePanel(this.screen, 6, 37, this.getWidth() - 105, 16, 1, 80, 8, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.offersPanel.<ShopOfferPanelEntry>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (mouseButton == 0)
                this.addToCart(clicked.index);
        });   

        //text field
        this.addElement(new OxygenTextLabel(150, 24, ClientReference.localize("oxygen_shop.gui.shop.search"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.textField = new OxygenTextField(150, 26, 67, 20, ""));

        //text field listener
        this.textField.setInputListener((keyChar, keyCode)->this.filterOffers());

        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_shop.gui.shop.category"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        //client data
        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.updateLoad();
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(this.screen.currencyProperties.getIndex(), WatcherHelperClient.getLong(this.screen.currencyProperties.getIndex()));

        //sub categories filter
        this.subCategoriesList = new OxygenDropDownList(75, 25, 67, "");
        this.addElement(this.subCategoriesList);

        //sub categories listener
        this.subCategoriesList.<OxygenDropDownListEntry<ItemSubCategory>>setClickListener((element)->{
            subCategoryCached = this.currentSubCategory = element.index;
            this.filterOffers();
        });

        this.loadSubCategories(ItemCategoriesPresetClient.COMMON_CATEGORY);

        //categories filter
        this.categoriesList = new OxygenDropDownList(6, 25, 67, this.currentCategory.localizedName());
        for (ItemCategory category : OxygenManagerClient.instance().getItemCategoriesPreset().getCategories())
            this.categoriesList.addElement(new OxygenDropDownListEntry<ItemCategory>(category, category.localizedName()));
        this.addElement(this.categoriesList);

        //categories listener
        this.categoriesList.<OxygenDropDownListEntry<ItemCategory>>setClickListener((element)->{
            this.loadSubCategories(categoryCached = this.currentCategory = element.index);
            this.filterOffers();
        });

        // *
        // * shopping cart
        // *

        this.addElement(new OxygenTextLabel(this.getWidth() - 90, 35, ClientReference.localize("oxygen_shop.gui.shop.cart"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.cartPanel = new OxygenScrollablePanel(this.screen, this.getWidth() - 90, 37, 81, 16, 1, ShopConfig.SHOP_CART_SIZE.asInt(), 7, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.addElement(this.totalPriceValue = new OxygenCurrencyValue(this.getWidth() - 14, 164));   
        this.totalPriceValue.setValue(this.screen.currencyProperties.getIndex(), 0L);

        this.addElement(this.purchaseButton = new OxygenButton(this.getWidth() - 90, 163, 40, 10, ClientReference.localize("oxygen_shop.gui.shop.purchaseButton")).disable());
        this.purchaseButton.setKeyPressListener(Keyboard.KEY_E, ShopManagerClient.instance().getShoppingCartManager()::purchaseItems);
    }

    private void loadSubCategories(ItemCategory category) {
        this.currentSubCategory = category.getSubCategories().get(0);
        this.subCategoriesList.reset();
        this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        for (ItemSubCategory subCategory : category.getSubCategories())
            this.subCategoriesList.addElement(new OxygenDropDownListEntry<ItemSubCategory>(subCategory, subCategory.localizedName()));
    }

    private void filterOffers() {
        List<ShopOffer> offers = this.getOffers();

        this.offersPanel.reset();
        for (ShopOffer offer : offers) {
            this.offersPanel.addEntry(new ShopOfferPanelEntry(
                    offer, 
                    this.screen.getEqualStackAmount(offer.getStackWrapper()), 
                    offer.getPrice() <= this.balanceValue.getValue(),
                    this.screen.currencyProperties));
        }

        this.offersPanel.getScroller().reset();
        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(offers.size(), 8, 800));
    }

    private List<ShopOffer> getOffers() {
        return ShopManagerClient.instance().getOffersContainer().getOffers()
                .stream()
                .filter((offer)->(this.filterByCategory(offer)))
                .filter((offer)->(this.filterByName(offer)))
                .sorted((o1, o2)->(o1.getStackWrapper().itemId - o2.getStackWrapper().itemId))
                .sorted((o1, o2)->o1.getPrice() < o2.getPrice() ? - 1 : o1.getPrice() > o2.getPrice() ? 1 : 0)
                .collect(Collectors.toList());
    }

    private boolean filterByCategory(ShopOffer offer) {
        return this.currentCategory.isValid(this.currentSubCategory, offer.getStackWrapper().getCachedItemStack().getItem().getRegistryName());
    }

    private boolean filterByName(ShopOffer offer) {
        return this.textField.getTypedText().isEmpty() || offer.getStackWrapper().getCachedItemStack().getDisplayName().contains(textSearchCached = this.textField.getTypedText());
    }

    public void loadCart() {
        this.cartPanel.reset();
        ShopOffer offer;
        for (Map.Entry<Long, Integer> entry : ShopManagerClient.instance().getShoppingCartManager().getCart().entrySet()) {
            offer = ShopManagerClient.instance().getOffersContainer().getOffer(entry.getKey());
            if (offer != null)
                this.cartPanel.addEntry(new CartPanelEntry(
                        offer, 
                        this.screen.getEqualStackAmount(offer.getStackWrapper()), 
                        offer.getPrice() <= this.balanceValue.getValue(),
                        this.screen.currencyProperties,
                        entry.getValue()));
            else
                ShopManagerClient.instance().getShoppingCartManager().removeItemFromCart(entry.getKey());
        }

        this.cartPanel.getScroller().reset();
        this.cartPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.cartPanel.buttonsBuffer.size(), 7, ShopConfig.SHOP_CART_SIZE.asInt()));

        this.updateTotalCartPrice();
    }

    private void addToCart(ShopOffer offer) {
        if (ShopManagerClient.instance().getShoppingCartManager().canItemBeAdded(offer.getId())) {
            ShopManagerClient.instance().getShoppingCartManager().addItemToCart(offer.getId());

            this.cartPanel.addEntry(new CartPanelEntry(
                    offer, 
                    this.screen.getEqualStackAmount(offer.getStackWrapper()), 
                    offer.getPrice() <= this.balanceValue.getValue(),
                    this.screen.currencyProperties,
                    1));

            this.cartPanel.getScroller().reset();
            this.cartPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.cartPanel.buttonsBuffer.size(), 7, ShopConfig.SHOP_CART_SIZE.asInt()));

            this.updateTotalCartPrice();
        }
    }

    public void updateTotalCartPrice() {
        this.totalPriceValue.setValue(this.screen.currencyProperties.getIndex(), this.calculateTotalCartPrice());
        this.totalPriceValue.setRed(this.totalPriceValue.getValue() > this.balanceValue.getValue());

        this.purchaseButton.setEnabled(!this.cartPanel.buttonsBuffer.isEmpty() && this.balanceValue.getValue() >= this.totalPriceValue.getValue());
    }

    private long calculateTotalCartPrice() {  
        long totalPrice = 0L;
        for (GUIButton button : this.cartPanel.buttonsBuffer)
            totalPrice += ((CartPanelEntry) button).getTotalPrice();
        return totalPrice;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.textField.isDragged() 
                && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == ShopMenuScreen.SHOP_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (ShopConfig.ENABLE_SHOP_MENU_KEY.asBoolean() 
                    && keyCode == ShopManagerClient.instance().getKeyHandler().getShopMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.applyLatestFiltersButton) {
                this.applyLatestFilters();
                this.filterOffers();
            } else if (element == this.resetFiltersButton) {
                this.resetFilters();
                this.filterOffers();
            } else if (element == this.purchaseButton)
                ShopManagerClient.instance().getShoppingCartManager().purchaseItems();
        }
    }

    @Override
    public void update() {
        if (ClientReference.getClientPlayer().ticksExisted % 20 == 0) {
            boolean reloadCart = false;
            CartPanelEntry cartEntry;
            for (GUIButton button : this.cartPanel.buttonsBuffer) {
                cartEntry = (CartPanelEntry) button;
                if (cartEntry.isRemoved())
                    reloadCart = true;
            }
            if (reloadCart)
                this.loadCart();
        }
    }

    private void applyLatestFilters() {
        this.currentCategory = categoryCached;
        this.categoriesList.setDisplayText(this.currentCategory.localizedName());
        this.loadSubCategories(this.currentCategory);
        this.currentSubCategory = subCategoryCached;
        if (subCategoryCached != null) {
            this.currentSubCategory = subCategoryCached;
            this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        }

        this.textField.setText(textSearchCached);
    }

    private void resetFilters() {
        this.currentCategory = ItemCategoriesPresetClient.COMMON_CATEGORY;
        this.categoriesList.setDisplayText(this.currentCategory.localizedName());
        this.loadSubCategories(this.currentCategory);

        this.textField.reset();
    }

    public void offersSynchronized() {
        this.filterOffers();
        this.loadCart();
    }

    public void purchaseSuccessful(long balance, long offerId) {
        this.balanceValue.updateValue(balance);

        //offers
        ShopOfferPanelEntry entry;
        for (GUIButton button : this.offersPanel.buttonsBuffer) {
            entry = (ShopOfferPanelEntry) button;
            if (entry.index.getPrice() > balance)
                entry.setAvailable(false);
        }

        //cart
        if (offerId != 0L) {
            CartPanelEntry cartEntry;
            for (GUIButton button : this.cartPanel.buttonsBuffer) {
                cartEntry = (CartPanelEntry) button;
                if (cartEntry.index.getPrice() > balance)
                    cartEntry.setAvailable(false);
                if (cartEntry.index.getId() == offerId)
                    cartEntry.remove();
            }
        } else
            this.cartPanel.reset();

        this.updateTotalCartPrice();
    }
}
