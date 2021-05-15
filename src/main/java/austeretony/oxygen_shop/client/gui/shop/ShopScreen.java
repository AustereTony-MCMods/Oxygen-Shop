package austeretony.oxygen_shop.client.gui.shop;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.player.inventory.InventoryHelperClient;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.preset.ItemCategoriesPresetClient;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_shop.client.gui.menu.ShopScreenMenuEntry;
import austeretony.oxygen_shop.client.settings.ShopSettings;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopMain;
import net.minecraft.client.gui.GuiScreen;

import java.util.Map;

public class ShopScreen extends OxygenScreen {

    public static final OxygenMenuEntry SHOP_SCREEN_MENU_ENTRY = new ShopScreenMenuEntry();

    public static final int BTN_SIZE = 5;
    public static final Texture CLOCK_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CLOCK_ICONS)
            .size(BTN_SIZE, BTN_SIZE)
            .imageSize(BTN_SIZE * 3, BTN_SIZE)
            .build();
    public static final Texture CROSS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CROSS_ICONS)
            .size(BTN_SIZE, BTN_SIZE)
            .imageSize(BTN_SIZE * 3, BTN_SIZE)
            .build();

    private final ItemCategoriesPresetClient itemCategoriesPreset;
    private final CurrencyProperties currencyProperties;
    private final Map<ItemStackWrapper, Integer> inventoryContentMap;

    private BuySection buySection;
    private SellingSection sellingSection;

    private ShopScreen() {
        itemCategoriesPreset = (ItemCategoriesPresetClient) OxygenClient.getPreset(OxygenMain.PRESET_ITEM_CATEGORIES);
        currencyProperties = OxygenClient.getCurrencyProperties(ShopConfig.SHOP_CURRENCY_INDEX.asInt());
        inventoryContentMap = InventoryHelperClient.getInventoryContent();
    }

    @Override
    public void initGui() {
        super.initGui();
        OxygenClient.requestDataSync(ShopMain.DATA_ID_SHOP);
    }

    @Override
    public int getScreenId() {
        return ShopMain.SCREEN_ID_SHOP;
    }

    @Override
    public Workspace createWorkspace() {
        Workspace workspace = new Workspace(this, 333, 197);
        workspace.setAlignment(Alignment.valueOf(ShopSettings.SHOP_SCREEN_ALIGNMENT.asString()), 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(buySection = new BuySection(this));
        getWorkspace().addSection(sellingSection = new SellingSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return buySection;
    }

    public ItemCategoriesPresetClient getItemCategoriesPreset() {
        return itemCategoriesPreset;
    }

    public CurrencyProperties getCurrencyProperties() {
        return currencyProperties;
    }

    public Map<ItemStackWrapper, Integer> getInventoryContentMap() {
        return inventoryContentMap;
    }

    public int getPlayerItemStock(ItemStackWrapper itemStackWrapper) {
        return inventoryContentMap.getOrDefault(itemStackWrapper, 0);
    }

    public void incrementPlayerStock(ItemStackWrapper itemStackWrapper, int amount) {
        int stock = inventoryContentMap.getOrDefault(itemStackWrapper, 0);
        stock += amount;
        if (stock <= 0) {
            inventoryContentMap.remove(itemStackWrapper);
            return;
        }
        inventoryContentMap.put(itemStackWrapper, stock);
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new ShopScreen());
    }

    public static void dataSynchronized() {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof ShopScreen) {
            ((ShopScreen) screen).buySection.dataSynchronized();
            ((ShopScreen) screen).sellingSection.dataSynchronized();
        }
    }

    public static void itemsPurchased(long balance, Map<Long, Integer> purchasedEntries) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof ShopScreen) {
            ((ShopScreen) screen).buySection.itemsPurchased(balance, purchasedEntries);
            ((ShopScreen) screen).sellingSection.itemsPurchased(balance, purchasedEntries);
        }
    }

    public static void itemSold(long balance, long entryId, int amount) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof ShopScreen) {
            ((ShopScreen) screen).buySection.itemSold(balance, entryId, amount);
            ((ShopScreen) screen).sellingSection.itemSold(balance, entryId, amount);
        }
    }
}
