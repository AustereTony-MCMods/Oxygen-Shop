package austeretony.oxygen_shop.client.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.network.operation.ShopOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShopNetworkOperationsHandlerClient implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return ShopMain.SHOP_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        ShopOperation operation = getEnum(ShopOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == ShopOperation.PURCHASED) {
            long balance = buffer.readLong();
            int amount = buffer.readByte();
            Map<Long, Integer> entriesMap = new LinkedHashMap<>(amount);
            for (int i = 0; i < amount; i++) {
                entriesMap.put(buffer.readLong(), (int) buffer.readShort());
            }

            ShopManagerClient.instance().itemsPurchased(balance, entriesMap);
        } else if (operation == ShopOperation.SOLD) {
            long balance = buffer.readLong();
            long entryId = buffer.readLong();
            int amount = buffer.readShort();

            ShopManagerClient.instance().itemSold(balance, entryId, amount);
        }
    }
}
