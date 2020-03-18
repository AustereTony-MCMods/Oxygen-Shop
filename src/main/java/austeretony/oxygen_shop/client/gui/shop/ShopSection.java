package austeretony.oxygen_shop.client.gui.shop;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.OxygenGUITextures;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenDefaultBackgroundWithButtonsUnderlinedFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList;
import austeretony.oxygen_core.client.gui.elements.OxygenDropDownList.OxygenDropDownListWrapperEntry;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenTexturedButton;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemCategory;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient.ItemSubCategory;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.gui.shop.callback.ConfirmPurchaseCallback;
import austeretony.oxygen_shop.common.ShopOffer;
import austeretony.oxygen_shop.common.config.ShopConfig;
import net.minecraft.client.gui.ScaledResolution;

public class ShopSection extends AbstractGUISection {

    private final ShopMenuScreen screen;

    private OxygenKeyButton purchaseButton;

    private OxygenTexturedButton applyLatestFiltersButton, resetFiltersButton, resetCartButton;

    private OxygenSorter nameSorter, priceSorter;

    private OxygenScrollablePanel offersPanel, cartPanel;

    private OxygenDropDownList categoriesList, subCategoriesList;

    private OxygenTextField textField;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue, totalPriceValue;

    private AbstractGUICallback confirmPurchaseCallback;

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
        this.confirmPurchaseCallback = new ConfirmPurchaseCallback(this.screen, this, 140, 36).enableDefaultBackground();

        this.addElement(new OxygenDefaultBackgroundWithButtonsUnderlinedFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, this.getDisplayText(), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.applyLatestFiltersButton = new OxygenTexturedButton(205, 20, 5, 5, OxygenGUITextures.CLOCK_ICONS, 5, 5, ClientReference.localize("oxygen_shop.gui.shop.tooltip.latestFilters")));         
        this.addElement(this.resetFiltersButton = new OxygenTexturedButton(212, 20, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_shop.gui.shop.tooltip.resetFilters")));         

        this.addElement(this.priceSorter = new OxygenSorter(6, 38, EnumSorting.DOWN, ClientReference.localize("oxygen_core.gui.price")));   
        this.priceSorter.setSortingListener((sorting)->{
            this.nameSorter.reset();
            this.filterOffers();
        });

        this.addElement(this.nameSorter = new OxygenSorter(12, 38, EnumSorting.INACTIVE, ClientReference.localize("oxygen_core.gui.name")));  
        this.nameSorter.setSortingListener((sorting)->{
            this.priceSorter.reset();
            this.filterOffers();
        });

        //offers panel
        this.addElement(this.offersPanel = new OxygenScrollablePanel(this.screen, 6, 47, this.getWidth() - 115, 16, 1, 80, 8, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.offersPanel.<ShopOfferPanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (mouseButton == 0)
                this.addToCart(clicked.getWrapped());
        });   

        //text field
        this.addElement(new OxygenTextLabel(150, 24, ClientReference.localize("oxygen_shop.gui.shop.label.search"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.textField = new OxygenTextField(150, 26, 67, 20, ""));

        //text field listener
        this.textField.setInputListener((keyChar, keyCode)->this.filterOffers());

        this.addElement(new OxygenTextLabel(6, 23, ClientReference.localize("oxygen_shop.gui.shop.label.category"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        //client data
        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.updateLoad();
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(this.screen.getCurrencyProperties().getIndex(), WatcherHelperClient.getLong(this.screen.getCurrencyProperties().getIndex()));

        //sub categories filter
        this.subCategoriesList = new OxygenDropDownList(75, 25, 67, "");
        this.addElement(this.subCategoriesList);

        //sub categories listener
        this.subCategoriesList.<OxygenDropDownListWrapperEntry<ItemSubCategory>>setElementClickListener((element)->{
            subCategoryCached = this.currentSubCategory = element.getWrapped();
            this.filterOffers();
        });

        this.loadSubCategories(ItemCategoriesPresetClient.COMMON_CATEGORY);

        //categories filter
        this.categoriesList = new OxygenDropDownList(6, 25, 67, this.currentCategory.localizedName());
        for (ItemCategory category : OxygenManagerClient.instance().getItemCategoriesPreset().getCategories())
            this.categoriesList.addElement(new OxygenDropDownListWrapperEntry<ItemCategory>(category, category.localizedName()));
        this.addElement(this.categoriesList);

        //categories listener
        this.categoriesList.<OxygenDropDownListWrapperEntry<ItemCategory>>setElementClickListener((element)->{
            this.loadSubCategories(categoryCached = this.currentCategory = element.getWrapped());
            this.filterOffers();
        });

        // *
        // * shopping cart
        // *

        this.addElement(new OxygenTextLabel(this.getWidth() - 100, 45, ClientReference.localize("oxygen_shop.gui.shop.label.cart"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));
        this.addElement(this.resetCartButton = new OxygenTexturedButton(this.getWidth() - 16, 39, 5, 5, OxygenGUITextures.CROSS_ICONS, 5, 5, ClientReference.localize("oxygen_shop.gui.shop.tooltip.resetCart")));         

        this.addElement(this.cartPanel = new OxygenScrollablePanel(this.screen, this.getWidth() - 100, 47, 91, 16, 1, MathUtils.greaterOfTwo(7, ShopConfig.SHOP_CART_SIZE.asInt()), 7, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.addElement(new OxygenTextLabel(this.getWidth() - 100, 181, ClientReference.localize("oxygen_shop.gui.shop.label.totalPrice"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.totalPriceValue = new OxygenCurrencyValue(this.getWidth() - 14, 174));   
        this.totalPriceValue.setValue(this.screen.getCurrencyProperties().getIndex(), 0L);

        this.addElement(this.purchaseButton = new OxygenKeyButton(0, this.getY() + this.getHeight() + this.screen.guiTop - 8, ClientReference.localize("oxygen_shop.gui.shop.button.purchase"), Keyboard.KEY_E, this::purchaseCartItems).disable());
    }

    private void purchaseCartItems() {
        if (!this.textField.isDragged())
            this.confirmPurchaseCallback.open();
    }

    private void calculateButtonsHorizontalPosition() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.purchaseButton.setX((sr.getScaledWidth() - (12 + this.textWidth(this.purchaseButton.getDisplayText(), this.purchaseButton.getTextScale()))) / 2 - this.screen.guiLeft);
    }

    private void loadSubCategories(ItemCategory category) {
        this.currentSubCategory = category.getSubCategories().get(0);
        this.subCategoriesList.reset();
        this.subCategoriesList.setDisplayText(this.currentSubCategory.localizedName());
        for (ItemSubCategory subCategory : category.getSubCategories())
            this.subCategoriesList.addElement(new OxygenDropDownListWrapperEntry<ItemSubCategory>(subCategory, subCategory.localizedName()));
    }

    private void filterOffers() {
        List<ShopOffer> offers = this.getOffers();

        this.offersPanel.reset();
        for (ShopOffer offer : offers) {
            this.offersPanel.addEntry(new ShopOfferPanelEntry(
                    offer, 
                    this.screen.getEqualStackAmount(offer.getStackWrapper()), 
                    offer.getPrice() <= this.balanceValue.getValue(),
                    this.screen.getCurrencyProperties()));
        }

        this.offersPanel.getScroller().reset();
        this.offersPanel.getScroller().updateRowsAmount(MathUtils.clamp(offers.size(), 8, 800));
    }

    private List<ShopOffer> getOffers() {
        return ShopManagerClient.instance().getOffersContainer().getOffers()
                .stream()
                .filter(this::filterByCategory)
                .filter(this::filterByName)
                .sorted((o1, o2)->(o1.getStackWrapper().getItemId() - o2.getStackWrapper().getItemId()))
                .sorted(this.getSortingComparator())
                .collect(Collectors.toList());
    }

    private boolean filterByCategory(ShopOffer offer) {
        return this.currentCategory.isValid(this.currentSubCategory, offer.getStackWrapper().getCachedItemStack().getItem().getRegistryName());
    }

    private boolean filterByName(ShopOffer offer) {
        return this.textField.getTypedText().isEmpty() || offer.getStackWrapper().getCachedItemStack().getDisplayName().contains(textSearchCached = this.textField.getTypedText());
    }

    private Comparator<ShopOffer> getSortingComparator() {
        if (this.nameSorter.getCurrentSorting() != EnumSorting.INACTIVE) {
            if (this.nameSorter.getCurrentSorting() == EnumSorting.DOWN)
                return (o1, o2)->getItemDisplayName(o1).compareTo(getItemDisplayName(o2));
                else
                    return (o1, o2)->getItemDisplayName(o2).compareTo(getItemDisplayName(o1));
        } else {
            if (this.priceSorter.getCurrentSorting() == EnumSorting.DOWN)
                return (o1, o2)->o1.getPrice() < o2.getPrice() ? - 1 : o1.getPrice() > o2.getPrice() ? 1 : 0;
                else
                    return (o1, o2)->o2.getPrice() < o1.getPrice() ? - 1 : o2.getPrice() > o1.getPrice() ? 1 : 0;
        }
    }

    public static String getItemDisplayName(ShopOffer offer) {
        return offer.getStackWrapper().getCachedItemStack().getDisplayName();
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
                        this.screen.getCurrencyProperties(),
                        entry.getValue()));
            else
                ShopManagerClient.instance().getShoppingCartManager().removeItemFromCart(entry.getKey());
        }

        this.cartPanel.getScroller().reset();
        this.cartPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.cartPanel.buttonsBuffer.size(), 7, MathUtils.greaterOfTwo(7, ShopConfig.SHOP_CART_SIZE.asInt())));

        this.updateTotalCartPrice();
    }

    private void addToCart(ShopOffer offer) {
        if (ShopManagerClient.instance().getShoppingCartManager().canItemBeAdded(offer.getId())) {
            ShopManagerClient.instance().getShoppingCartManager().addItemToCart(offer.getId());

            this.cartPanel.addEntry(new CartPanelEntry(
                    offer, 
                    this.screen.getEqualStackAmount(offer.getStackWrapper()), 
                    offer.getPrice() <= this.balanceValue.getValue(),
                    this.screen.getCurrencyProperties(),
                    1));

            this.cartPanel.getScroller().reset();
            this.cartPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.cartPanel.buttonsBuffer.size(), 7, ShopConfig.SHOP_CART_SIZE.asInt()));

            this.updateTotalCartPrice();
        }
    }

    public void updateTotalCartPrice() {
        this.totalPriceValue.setValue(this.screen.getCurrencyProperties().getIndex(), this.calculateTotalCartPrice());
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
            } else if (element == this.resetCartButton) {
                ShopManagerClient.instance().getShoppingCartManager().reset();
                this.loadCart();
            } else if (element == this.purchaseButton)
                this.purchaseCartItems();
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

        this.calculateButtonsHorizontalPosition();
    }

    public void purchaseSuccessful(long balance, long offerId) {
        this.balanceValue.updateValue(balance);

        //offers
        ShopOfferPanelEntry entry;
        for (GUIButton button : this.offersPanel.buttonsBuffer) {
            entry = (ShopOfferPanelEntry) button;
            if (entry.getWrapped().getPrice() > balance)
                entry.setAvailable(false);
        }

        //cart
        if (offerId != 0L) {
            CartPanelEntry cartEntry;
            for (GUIButton button : this.cartPanel.buttonsBuffer) {
                cartEntry = (CartPanelEntry) button;
                if (cartEntry.getWrapped().getPrice() > balance)
                    cartEntry.setAvailable(false);
                if (cartEntry.getWrapped().getId() == offerId)
                    cartEntry.remove();
            }
        } else
            this.cartPanel.reset();

        this.updateTotalCartPrice();
    }
}
