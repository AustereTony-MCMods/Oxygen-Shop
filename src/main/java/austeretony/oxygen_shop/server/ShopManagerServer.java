package austeretony.oxygen_shop.server;

import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.chat.StatusMessageType;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.SoundEffects;
import austeretony.oxygen_core.common.util.JsonUtils;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.operation.Operation;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.main.ShopPrivileges;
import austeretony.oxygen_shop.common.network.operation.ShopOperation;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class ShopManagerServer {

    private static ShopManagerServer instance;

    private final Map<Long, ShopEntry> shopEntriesMap = new HashMap<>();

    private ShopManagerServer() {}

    public static ShopManagerServer instance() {
        if (instance == null)
            instance = new ShopManagerServer();
        return instance;
    }

    public void serverStarting() {
        OxygenServer.addTask(this::loadShopEntries);
    }

    public void loadShopEntries() {
        String pathStr = OxygenCommon.getConfigFolder() + "/data/server/shop/shop_entries.json";
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            createDefaultEntriesFile();
        }

        shopEntriesMap.clear();
        try {
            JsonArray entriesArray = JsonUtils.getExternalJsonData(pathStr).getAsJsonArray();
            for (JsonElement entryElement : entriesArray) {
                ShopEntry shopEntry = ShopEntry.fromJson(entryElement.getAsJsonObject());
                shopEntriesMap.put(shopEntry.getId(), shopEntry);
            }
            OxygenMain.logInfo(1, "[Shop] Successfully loaded shop entries from file <{}>.", pathStr);
        } catch (IOException exception) {
            OxygenMain.logError(1, "[Shop] Shop entries file <" + pathStr + "> is damaged!", exception);
            exception.printStackTrace();
        }
    }

    private void createDefaultEntriesFile() {
        saveShopEntries(Collections.emptyMap());
    }

    private void saveShopEntries(Map<Long, ShopEntry> entriesMap) {
        String pathStr = OxygenCommon.getConfigFolder() + "/data/server/shop/shop_entries.json";
        Path path = Paths.get(pathStr);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
            }
            JsonArray entriesArray = new JsonArray();
            List<ShopEntry> sortedEntries = entriesMap.values()
                    .stream()
                    .sorted(Comparator.comparing(ShopEntry::getId))
                    .collect(Collectors.toList());
            for (ShopEntry shopEntry : sortedEntries) {
                entriesArray.add(shopEntry.toJson());
            }
            JsonUtils.createExternalJsonFile(pathStr, entriesArray);
        } catch (IOException exception) {
            OxygenMain.logError(1, "[Shop] Failed to create shop entries file! Path: {}", pathStr);
            exception.printStackTrace();
        }
    }

    public void saveShopEntries() {
        saveShopEntries(shopEntriesMap);
    }

    public Map<Long, ShopEntry> getShopEntriesMap() {
        return shopEntriesMap;
    }

    public void addShopEntry(ShopEntry shopEntry) {
        shopEntriesMap.put(shopEntry.getId(), shopEntry);
    }

    public ShopEntry removeShopEntry(long entryId) {
        return shopEntriesMap.remove(entryId);
    }

    public void openShopScreen(EntityPlayerMP playerMP) {
        OxygenServer.resetTimeout(ShopMain.TIMEOUT_SHOP_OPERATIONS, MinecraftCommon.getEntityUUID(playerMP));
        OxygenServer.openScreen(playerMP, ShopMain.SCREEN_ID_SHOP);
    }

    public void purchaseItems(EntityPlayerMP playerMP, Map<Long, Integer> entriesMap) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!ShopConfig.ENABLE_SHOP_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(ShopMain.TIMEOUT_SHOP_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, ShopMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        if (!PrivilegesServer.getBoolean(playerUUID, ShopPrivileges.SHOP_ACCESS.getId(),
                ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            OxygenServer.sendStatusMessage(playerMP, ShopMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.no_access");
            return;
        }

        int currencyIndex = ShopConfig.SHOP_CURRENCY_INDEX.asInt();
        Map<ItemStackWrapper, Integer> itemsMap = new HashMap<>();
        long totalPrice = 0L;

        for (Map.Entry<Long, Integer> entry : entriesMap.entrySet()) {
            ShopEntry shopEntry = shopEntriesMap.get(entry.getKey());
            if (shopEntry == null || shopEntry.getType() != ShopEntry.Type.BUY || entry.getValue() <= 0) continue;

            totalPrice += shopEntry.getPrice() * entry.getValue();
            int quantity = shopEntry.getQuantity() * entry.getValue();

            int contained = itemsMap.getOrDefault(shopEntry.getStackWrapper(), 0);
            itemsMap.put(shopEntry.getStackWrapper(), contained + quantity);
        }

        Operation.of(ShopMain.OPERATION_ITEM_BUY, playerMP)
                .withSuccessTask(() -> {
                    OxygenMain.logInfo(2, "[Shop] BUY: {}/{} bought: {}",
                            playerUUID, MinecraftCommon.getEntityName(playerMP), entriesMap);

                    OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
                    OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);

                    long balance = OxygenServer.getWatcherValue(playerUUID, currencyIndex, 0L);
                    OxygenServer.sendToClient(
                            playerMP,
                            ShopMain.SHOP_OPERATIONS_HANDLER_ID,
                            ShopOperation.PURCHASED.ordinal(),
                            buffer -> {
                                buffer.writeLong(balance);
                                buffer.writeByte(entriesMap.size());
                                for (Map.Entry<Long, Integer> entry : entriesMap.entrySet()) {
                                    buffer.writeLong(entry.getKey());
                                    buffer.writeShort(entry.getValue());
                                }
                            }
                    );
                })
                .withFailTask(reason -> OxygenServer.sendMessageOnOperationFail(playerMP, reason, ShopMain.MODULE_INDEX))
                .withCurrencyWithdraw(currencyIndex, totalPrice)
                .withItemsAdd(itemsMap)
                .process();
    }

    public void sellItem(EntityPlayerMP playerMP, long entryId, int amount) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!ShopConfig.ENABLE_SHOP_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(ShopMain.TIMEOUT_SHOP_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, ShopMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_shop.status_message.operations_timeout");
            return;
        }

        if (!PrivilegesServer.getBoolean(playerUUID, ShopPrivileges.SHOP_ACCESS.getId(),
                ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {
            OxygenServer.sendStatusMessage(playerMP, ShopMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.no_access");
            return;
        }

        int currencyIndex = ShopConfig.SHOP_CURRENCY_INDEX.asInt();
        ShopEntry shopEntry = shopEntriesMap.get(entryId);
        if (shopEntry == null || shopEntry.getType() != ShopEntry.Type.SELLING) {
            OxygenServer.sendStatusMessage(playerMP, ShopMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.invalid_operation");
            return;
        }

        int quantity = shopEntry.getQuantity() * amount;
        long income = shopEntry.getPrice() * amount;

        Operation.of(ShopMain.OPERATION_ITEM_SELL, playerMP)
                .withSuccessTask(() -> {
                    OxygenMain.logInfo(2, "[Shop] SELL: {}/{} sold [id: {}, amount: {}] and earned {} currency.",
                            playerUUID, MinecraftCommon.getEntityName(playerMP), entryId, amount, income);

                    OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
                    OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);

                    long balance = OxygenServer.getWatcherValue(playerUUID, currencyIndex, 0L);
                    OxygenServer.sendToClient(
                            playerMP,
                            ShopMain.SHOP_OPERATIONS_HANDLER_ID,
                            ShopOperation.SOLD.ordinal(),
                            buffer -> {
                                buffer.writeLong(balance);
                                buffer.writeLong(entryId);
                                buffer.writeShort(amount);
                            });
                })
                .withFailTask(reason -> OxygenServer.sendMessageOnOperationFail(playerMP, reason, ShopMain.MODULE_INDEX))
                .withItemWithdraw(shopEntry.getStackWrapper(), quantity)
                .withCurrencyGain(currencyIndex, income)
                .process();
    }
}
