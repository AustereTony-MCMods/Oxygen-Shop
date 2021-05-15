package austeretony.oxygen_shop.client.gui.shop;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.*;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.gui.base.list.DropDownList;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.*;
import austeretony.oxygen_core.client.gui.base.special.callback.YesNoCallback;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemsSubCategory;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopPrivileges;
import austeretony.oxygen_shop.common.shop.ShopEntry;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuySection extends Section {

    private final ShopScreen screen;

    private ScrollableList<ShopEntry> shopEntriesList, cartEntriesList;
    private TextField textFiled;
    private Sorter itemNameSorter, priceSorter;
    private ImageButton resetFiltersButton, applyLatestFiltersButton, clearCartButton;
    private DropDownList<String> itemCategoryDDList, itemSubCategoryDDList;
    private InventoryLoad inventoryLoad;
    private CurrencyValue balanceValue, totalPriceValue;
    private KeyButton purchaseCartButton;

    private String currItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
    private String currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;

    private static String cachedItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
    private static String cachedItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;

    public BuySection(@Nonnull ShopScreen screen) {
        super(screen, localize("oxygen_shop.gui.shop.section.buy"), true);
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottomButtons(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_shop.gui.shop.title")));
        if (!ShopManagerClient.instance().getShopEntries(ShopEntry.Type.SELLING).isEmpty()) {
            addWidget(new SectionSwitcher(this));
        }

        addWidget(applyLatestFiltersButton = new ImageButton(205, 20, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                ShopScreen.CLOCK_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.image_button.apply_latest_filters"))
                .setMouseClickListener((mouseX, mouseY, button) -> applyLatestFilters()));
        addWidget(resetFiltersButton = new ImageButton(212, 20, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                ShopScreen.CROSS_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.image_button.reset_filters"))
                .setMouseClickListener((mouseX, mouseY, button) -> resetFilters()));

        addWidget(new TextLabel(6, 24, Texts.additionalDark("oxygen_core.gui.label.item_category")));
        String commonItemsCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
        addWidget(itemCategoryDDList = new DropDownList<>(6, 26, 67, commonItemsCategory)
                .<String>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    cachedItemCategory = currItemCategory = current.getEntry();
                    updateItemSubCategoriesFilter(current.getEntry());
                    filterShopEntries();
                }));
        itemCategoryDDList.addElement(ListEntry.of(commonItemsCategory, commonItemsCategory));
        for (String categoryName : screen.getItemCategoriesPreset().getSortedCategories()) {
            itemCategoryDDList.addElement(ListEntry.of(localize(categoryName), categoryName));
        }

        addWidget(itemSubCategoryDDList = new DropDownList<>(75, 26, 67, ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME)
                .<String>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    cachedItemSubCategory = currItemSubCategory = current.getEntry();
                    filterShopEntries();
                }));
        updateItemSubCategoriesFilter(commonItemsCategory);

        addWidget(priceSorter = new Sorter(6, 38, Sorter.State.DOWN, localize("oxygen_core.gui.sorter.by_price"))
                .setStateChangeListener((previous, current) -> {
                    itemNameSorter.setState(Sorter.State.INACTIVE);
                    filterShopEntries();
                }));
        addWidget(itemNameSorter = new Sorter(12, 38, Sorter.State.INACTIVE, localize("oxygen_core.gui.sorter.by_item_name"))
                .setStateChangeListener((previous, current) -> {
                    priceSorter.setState(Sorter.State.INACTIVE);
                    filterShopEntries();
                }));

        addWidget(shopEntriesList = new ScrollableList<>(6, 47, Fills.def(), 8, getWidth() - 6 * 2 - 110, 16, 1, true)
                .<ShopEntry>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (!((ShopListEntry) current).isAvailable()) return;
                    addToCart(current.getEntry());
                }));
        VerticalSlider slider = new VerticalSlider(6 + getWidth() - 6 * 2 - 110 + 1, 47, 2, 16 * 8 + 7);
        addWidget(slider);
        shopEntriesList.setSlider(slider);
        addWidget(new TextLabel(150, 24, Texts.additionalDark("oxygen_core.gui.label.text_search")));
        addWidget(textFiled = new TextField(150, 27, 67, 24)
                .setKeyPressListener((keyChar, keyCode) -> filterShopEntries()));

        if (!PrivilegesClient.getBoolean(ShopPrivileges.SHOP_ACCESS.getId(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            addWidget(new TextLabel(6, 55, Texts.additionalDark("oxygen_shop.gui.shop.label.no_access")));
        }

        // cart - start

        addWidget(new TextLabel(getWidth() - 110, 45, Texts.additional("oxygen_shop.gui.shop.label.cart")));
        addWidget(clearCartButton = new ImageButton(getWidth() - 16, 39, ShopScreen.BTN_SIZE, ShopScreen.BTN_SIZE,
                ShopScreen.CROSS_ICONS_TEXTURE, localize("oxygen_shop.gui.shop.image_button.clear_cart"))
                .setMouseClickListener((mouseX, mouseY, button) -> clearCart()));

        addWidget(cartEntriesList = new ScrollableList<>(getWidth() - 110, 47, 7, 100, 16));
        VerticalSlider cartSlider = new VerticalSlider(cartEntriesList.getX() + cartEntriesList.getWidth() + 1, 47, 2, 16 * 7 + 6);
        addWidget(cartSlider);
        cartEntriesList.setSlider(cartSlider);

        addWidget(new TextLabel(getWidth() - 110, 181, Texts.additional("oxygen_shop.gui.shop.label.total_price")));
        int currencyIndex = ShopConfig.SHOP_CURRENCY_INDEX.asInt();
        addWidget(totalPriceValue = new CurrencyValue(getWidth() - 14, 174).setCurrency(currencyIndex, 0L));

        // cart - end

        addWidget(inventoryLoad = new InventoryLoad(6, getHeight() - 10).updateLoad());
        addWidget(balanceValue = new CurrencyValue(getWidth() - 14, getHeight() - 10)
                .setCurrency(currencyIndex, OxygenClient.getWatcherValue(currencyIndex, 0L)));

        String keyButtonText = localize("oxygen_shop.gui.shop.buy.button.purchase_cart");
        addWidget(purchaseCartButton = new KeyButton(0, 0, Keys.ACTION_KEY, keyButtonText)
                .setLayer(Layer.FRONT)
                .setPressListener(() -> {
                    if (!textFiled.isFocused()) {
                        Callback callback = new YesNoCallback(
                                "oxygen_shop.gui.shop.buy.callback.purchase_cart",
                                "oxygen_shop.gui.shop.buy.callback.purchase_cart_message",
                                this::purchaseCartItems);
                        openCallback(callback);
                    }
                })
                .setEnabled(false));
        OxygenGUIUtils.calculateBottomCenteredOffscreenButtonPosition(purchaseCartButton, 1, 1);
    }

    private void updateItemSubCategoriesFilter(String categoryName) {
        itemSubCategoryDDList.clear();

        String commonSubCategoryName = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
        itemSubCategoryDDList.getText().setText(commonSubCategoryName);
        itemSubCategoryDDList.addElement(ListEntry.of(commonSubCategoryName, commonSubCategoryName));
        if (categoryName.equals(ItemCategoriesPresetClient.COMMON_CATEGORY_NAME)) return;

        List<ItemsSubCategory> subCategoriesList = screen.getItemCategoriesPreset()
                .getSortedSubCategories(categoryName);
        for (ItemsSubCategory subCategory : subCategoriesList) {
            itemSubCategoryDDList.addElement(ListEntry.of(subCategory.getLocalizedName(), subCategory.getName()));
        }
        cachedItemSubCategory = currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!textFiled.isFocused()) {
            OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void update() {
        super.update();
        if (MinecraftClient.getPlayer().ticksExisted % 10 == 0) {
            boolean reloadCart = false;
            for (Widget widget : cartEntriesList.getWidgets()) {
                CartListEntry cartEntry = (CartListEntry) widget;
                if (cartEntry.isRemoved()) {
                    reloadCart = true;
                }
            }

            if (reloadCart) {
                loadCart();
            }
        }
    }

    public void dataSynchronized() {
        filterShopEntries();
        loadCart();
    }

    private boolean canPlayerAfford(ShopEntry shopEntry) {
        return shopEntry.getPrice() <= balanceValue.getValue();
    }

    private void filterShopEntries() {
        if (!PrivilegesClient.getBoolean(ShopPrivileges.SHOP_ACCESS.getId(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            return;
        }

        shopEntriesList.clear();
        List<ShopEntry> shopEntries = getShopEntries();
        for (ShopEntry shopEntry : shopEntries) {
            shopEntriesList.addElement(new ShopListEntry(shopEntry, screen.getPlayerItemStock(shopEntry.getStackWrapper()),
                    canPlayerAfford(shopEntry), screen.getCurrencyProperties()));
        }
    }

    private List<ShopEntry> getShopEntries() {
        return ShopManagerClient.instance().getShopEntries(ShopEntry.Type.BUY)
                .stream()
                .filter(this::isValidCategory)
                .filter(this::isValidItemName)
                .sorted(Comparator.comparingInt(e -> e.getStackWrapper().getItemId()))
                .sorted(getSortersComparator())
                .collect(Collectors.toList());
    }

    private boolean isValidCategory(ShopEntry shopEntry) {
        return screen.getItemCategoriesPreset().isValidForCategory(currItemCategory, currItemSubCategory,
                shopEntry.getStackWrapper().getItemStackCached());
    }

    private boolean isValidItemName(ShopEntry shopEntry) {
        String typedText = textFiled.getTypedText();
        return typedText.isEmpty() || getItemDisplayName(shopEntry).contains(typedText);
    }

    private Comparator<ShopEntry> getSortersComparator() {
        if (itemNameSorter.getState() != Sorter.State.INACTIVE) {
            if (itemNameSorter.getState() == Sorter.State.DOWN) {
                return Comparator.comparing(BuySection::getItemDisplayName);
            } else {
                return Comparator.comparing(BuySection::getItemDisplayName).reversed();
            }
        } else {
            if (priceSorter.getState() == Sorter.State.DOWN) {
                return Comparator.comparingLong(ShopEntry::getPrice);
            } else {
                return Comparator.comparingLong(ShopEntry::getPrice).reversed();
            }
        }
    }

    public static String getItemDisplayName(ShopEntry shopEntry) {
        return shopEntry.getStackWrapper().getItemStackCached().getDisplayName();
    }

    private void applyLatestFilters() {
        currItemCategory = cachedItemCategory;
        itemCategoryDDList.getText().setText(localize(currItemCategory));

        currItemSubCategory = cachedItemSubCategory;
        updateItemSubCategoriesFilter(currItemCategory);
        filterShopEntries();
    }

    private void resetFilters() {
        currItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
        itemCategoryDDList.getText().setText(localize(currItemCategory));

        currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;
        updateItemSubCategoriesFilter(currItemCategory);

        textFiled.setText("");

        filterShopEntries();
    }

    private void loadCart() {
        if (!PrivilegesClient.getBoolean(ShopPrivileges.SHOP_ACCESS.getId(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            return;
        }

        cartEntriesList.clear();
        ShopManagerClient manager = ShopManagerClient.instance();
        for (Map.Entry<Long, Integer> entry : manager.getCartEntriesMap().entrySet()) {
            ShopEntry shopEntry = manager.getShopEntry(entry.getKey());
            if (shopEntry == null) {
                manager.setCartItemAmount(entry.getKey(), 0);
                continue;
            }

            cartEntriesList.addElement(new CartListEntry(shopEntry, screen.getPlayerItemStock(shopEntry.getStackWrapper()),
                    screen.getCurrencyProperties()));
        }
        updateTotalCartPrice();
    }

    protected void updateTotalCartPrice() {
        ShopManagerClient manager = ShopManagerClient.instance();
        totalPriceValue.setValue(manager.calculateTotalCartPrice());
        boolean canAfford = balanceValue.getValue() >= totalPriceValue.getValue();
        totalPriceValue.setState(canAfford ? SpecialState.NORMAL : SpecialState.INACTIVE);
        purchaseCartButton.setEnabled(!manager.isCartEmpty() && canAfford);
    }

    private void addToCart(ShopEntry shopEntry) {
        ShopManagerClient manager = ShopManagerClient.instance();
        if (!manager.canItemBeAddedToCart(shopEntry.getId())) return;
        manager.incrementCartItemAmount(shopEntry.getId());
        cartEntriesList.addElement(new CartListEntry(shopEntry, screen.getPlayerItemStock(shopEntry.getStackWrapper()),
                screen.getCurrencyProperties()));
    }

    private void clearCart() {
        cartEntriesList.clear();
        ShopManagerClient.instance().clearCart();
        updateTotalCartPrice();
    }

    private void purchaseCartItems() {
        ShopManagerClient.instance().purchaseCartItems();
    }

    protected void itemsPurchased(long balance, Map<Long, Integer> purchasedEntries) {
        if (purchasedEntries.isEmpty()) return;
        balanceValue.setValue(balance);

        for (Map.Entry<Long, Integer> entry : purchasedEntries.entrySet()) {
            ShopEntry shopEntry = ShopManagerClient.instance().getShopEntry(entry.getKey());
            if (shopEntry == null) continue;
            screen.incrementPlayerStock(shopEntry.getStackWrapper(), shopEntry.getQuantity() * entry.getValue());
        }
        inventoryLoad.updateLoad(screen.getInventoryContentMap());

        loadCart();
        int position = shopEntriesList.getScrollPosition();
        filterShopEntries();
        shopEntriesList.setScrollPosition(position);
    }

    public void itemSold(long balance, long entryId, int amount) {
        balanceValue.setValue(balance);
        ShopEntry shopEntry = ShopManagerClient.instance().getShopEntry(entryId);
        if (shopEntry == null) return;
        screen.incrementPlayerStock(shopEntry.getStackWrapper(), -shopEntry.getQuantity() * amount);
        inventoryLoad.updateLoad(screen.getInventoryContentMap());

        filterShopEntries();
    }
}
