package austeretony.oxygen_shop.client.gui.shop;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.Fills;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.DropDownList;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.*;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.preset.ItemsSubCategory;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.gui.shop.context.ShopSellSelectItemQuantityContextAction;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopPrivileges;
import austeretony.oxygen_shop.common.shop.ShopEntry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SellingSection extends Section {

    private final ShopScreen screen;

    private ScrollableList<ShopEntry> shopEntriesList;
    private TextField textFiled;
    private Sorter itemNameSorter, priceSorter;
    private ImageButton resetFiltersButton, applyLatestFiltersButton;
    private DropDownList<String> itemCategoryDDList, itemSubCategoryDDList;
    private InventoryLoad inventoryLoad;
    private CurrencyValue balanceValue;

    private String currItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
    private String currItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;

    private static String cachedItemCategory = ItemCategoriesPresetClient.COMMON_CATEGORY_NAME;
    private static String cachedItemSubCategory = ItemCategoriesPresetClient.COMMON_SUB_CATEGORY_NAME;

    public SellingSection(@Nonnull ShopScreen screen) {
        super(screen, localize("oxygen_shop.gui.shop.section.selling"), true);
        this.screen = screen;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_shop.gui.shop.title")));
        addWidget(new SectionSwitcher(this));

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

        addWidget(shopEntriesList = new ScrollableList<>(6, 47, Fills.def(), 8, getWidth() - 6 * 2 - 3, 16, 1, true)
                .<ShopEntry>setEntryMouseClickListener((previous, current, x, y, button) -> {
                    if (!((ShopListEntry) current).isAvailable()) return;
                    sellItem(current.getEntry().getId(), 1);
                }));
        VerticalSlider slider = new VerticalSlider(6 + getWidth() - 6 * 2 - 3 + 1, 47, 2, 16 * 8 + 7);
        addWidget(slider);
        shopEntriesList.setSlider(slider);
        addWidget(new TextLabel(150, 24, Texts.additionalDark("oxygen_core.gui.label.text_search")));
        addWidget(textFiled = new TextField(150, 27, 67, 24)
                .setKeyPressListener((keyChar, keyCode) -> filterShopEntries()));
        shopEntriesList.createContextMenu(Collections.singletonList(new ShopSellSelectItemQuantityContextAction()));

        if (!PrivilegesClient.getBoolean(ShopPrivileges.SHOP_ACCESS.getId(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            addWidget(new TextLabel(6, 55, Texts.additionalDark("oxygen_shop.gui.shop.label.no_access")));
        }

        addWidget(inventoryLoad = new InventoryLoad(6, getHeight() - 10).updateLoad());
        int currencyIndex = ShopConfig.SHOP_CURRENCY_INDEX.asInt();
        addWidget(balanceValue = new CurrencyValue(getWidth() - 14, getHeight() - 10)
                .setCurrency(currencyIndex, OxygenClient.getWatcherValue(currencyIndex, 0L)));
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

    public void dataSynchronized() {
        filterShopEntries();
    }

    private boolean canPlayerSell(ShopEntry shopEntry) {
        return screen.getPlayerItemStock(shopEntry.getStackWrapper()) >= shopEntry.getQuantity();
    }

    private void filterShopEntries() {
        if (!PrivilegesClient.getBoolean(ShopPrivileges.SHOP_ACCESS.getId(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            return;
        }

        shopEntriesList.clear();
        List<ShopEntry> shopEntries = getShopEntries();
        for (ShopEntry shopEntry : shopEntries) {
            shopEntriesList.addElement(new ShopListEntry(shopEntry, screen.getPlayerItemStock(shopEntry.getStackWrapper()),
                    canPlayerSell(shopEntry), screen.getCurrencyProperties()));
        }
    }

    private List<ShopEntry> getShopEntries() {
        return ShopManagerClient.instance().getShopEntries(ShopEntry.Type.SELLING)
                .stream()
                .filter(this::isValidCategory)
                .filter(this::isValidName)
                .sorted(Comparator.comparingInt(e -> e.getStackWrapper().getItemId()))
                .sorted(getSortersComparator())
                .collect(Collectors.toList());
    }

    private boolean isValidCategory(ShopEntry shopEntry) {
        return screen.getItemCategoriesPreset().isValidForCategory(currItemCategory, currItemSubCategory,
                shopEntry.getStackWrapper().getItemStackCached());
    }

    private boolean isValidName(ShopEntry shopEntry) {
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

    private void sellItem(long entryId, int amount) {
        ShopManagerClient.instance().sellItem(entryId, amount);
    }

    public void itemSold(long balance, long entryId, int amount) {
        int position = shopEntriesList.getScrollPosition();
        filterShopEntries();
        shopEntriesList.setScrollPosition(position);

        balanceValue.setValue(balance);
        inventoryLoad.updateLoad(screen.getInventoryContentMap());
    }

    protected void itemsPurchased(long balance, Map<Long, Integer> purchasedEntries) {
        filterShopEntries();

        balanceValue.setValue(balance);
        inventoryLoad.updateLoad(screen.getInventoryContentMap());
    }
}
