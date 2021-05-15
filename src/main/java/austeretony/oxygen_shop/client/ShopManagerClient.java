package austeretony.oxygen_shop.client;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_shop.client.gui.shop.ShopScreen;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.network.operation.ShopOperation;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ShopManagerClient extends AbstractPersistentData {

    private static ShopManagerClient instance;

    private final Map<Long, ShopEntry> shopEntriesMap = new HashMap<>();
    private final Map<Long, Integer> cartEntriesMap = new LinkedHashMap<>();

    private ShopManagerClient() {
        OxygenClient.registerPersistentData(this);
    }

    public static ShopManagerClient instance() {
        if (instance == null)
            instance = new ShopManagerClient();
        return instance;
    }

    public void clientInitialized() {
        OxygenClient.loadPersistentDataAsync(this);
        cartEntriesMap.clear();
    }

    public Map<Long, ShopEntry> getShopEntriesMap() {
        return shopEntriesMap;
    }

    public ShopEntry getShopEntry(long id) {
        return shopEntriesMap.get(id);
    }

    public Map<Long, Integer> getCartEntriesMap() {
        return cartEntriesMap;
    }

    public boolean isCartEmpty() {
        return cartEntriesMap.isEmpty();
    }

    public void clearCart() {
        cartEntriesMap.clear();
    }

    public boolean canItemBeAddedToCart(long entryId) {
        return !cartEntriesMap.containsKey(entryId) && cartEntriesMap.size() < ShopConfig.SHOP_CART_SIZE.asInt();
    }

    public int getCartItemAmount(long entryId) {
        return cartEntriesMap.getOrDefault(entryId, 0);
    }

    public void setCartItemAmount(long entryId, int amount) {
        if (amount == 0) {
            cartEntriesMap.remove(entryId);
            return;
        }
        cartEntriesMap.put(entryId, amount);
    }

    public int incrementCartItemAmount(long entryId) {
        int amount = cartEntriesMap.getOrDefault(entryId, 0);
        amount++;
        cartEntriesMap.put(entryId, amount);
        return amount;
    }

    public int decrementCartItemAmount(long entryId) {
        int amount = cartEntriesMap.getOrDefault(entryId, 0);
        if (amount == 0) return 0;
        if (amount == 1) {
            cartEntriesMap.remove(entryId);
            return 0;
        }
        amount--;
        cartEntriesMap.put(entryId, amount);
        return amount;
    }

    public long calculateTotalCartPrice() {
        long totalPrice = 0;
        for (Map.Entry<Long, Integer> entry : cartEntriesMap.entrySet()) {
            ShopEntry shopEntry = getShopEntry(entry.getKey());
            if (shopEntry == null) continue;
            totalPrice += shopEntry.getPrice() * entry.getValue();
        }
        return totalPrice;
    }

    public void purchaseItems(Map<Long, Integer> entriesMap) {
        OxygenClient.sendToServer(
                ShopMain.SHOP_OPERATIONS_HANDLER_ID,
                ShopOperation.PURCHASE.ordinal(),
                buffer -> {
                    buffer.writeByte(entriesMap.size());
                    for (Map.Entry<Long, Integer> entry : entriesMap.entrySet()) {
                        buffer.writeLong(entry.getKey());
                        buffer.writeShort(entry.getValue());
                    }
                });
    }

    public void purchaseCartItems() {
        purchaseItems(cartEntriesMap);
    }

    public void itemsPurchased(long balance, Map<Long, Integer> purchasedEntries) {
        for (Map.Entry<Long, Integer> entry : purchasedEntries.entrySet()) {
            int amount = cartEntriesMap.getOrDefault(entry.getKey(), 0);
            if (amount == 0) continue;
            amount -= entry.getKey();
            if (amount <= 0) {
                cartEntriesMap.remove(entry.getKey());
                continue;
            }
            cartEntriesMap.put(entry.getKey(), amount);
        }
        ShopScreen.itemsPurchased(balance, purchasedEntries);
    }

    public List<ShopEntry> getShopEntries(ShopEntry.Type type) {
        return shopEntriesMap.values()
                .stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    public void sellItem(long entryId, int amount) {
        OxygenClient.sendToServer(
                ShopMain.SHOP_OPERATIONS_HANDLER_ID,
                ShopOperation.SELL.ordinal(),
                buffer -> {
                    buffer.writeLong(entryId);
                    buffer.writeShort(amount);
                });
    }

    public void itemSold(long balance, long entryId, int amount) {
        ShopScreen.itemSold(balance, entryId, amount);
    }

    @Override
    public String getName() {
        return "shop:data_client";
    }

    @Override
    public String getPath() {
        return OxygenClient.getDataFolder() + "/client/shop/shop.dat";
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = new NBTTagList();
        for (ShopEntry shopEntry : shopEntriesMap.values()) {
            tagList.appendTag(shopEntry.writeToNBT());
        }
        tagCompound.setTag("shop_entries_list", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = tagCompound.getTagList("shop_entries_list", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            ShopEntry shopEntry = ShopEntry.readFromNBT(tagList.getCompoundTagAt(i));
            shopEntriesMap.put(shopEntry.getId(), shopEntry);
        }
    }

    @Override
    public void reset() {
        shopEntriesMap.clear();
    }
}
