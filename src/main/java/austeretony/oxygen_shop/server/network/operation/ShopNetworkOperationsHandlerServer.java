package austeretony.oxygen_shop.server.network.operation;

import austeretony.oxygen_core.common.network.operation.NetworkOperationsHandler;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.network.operation.ShopOperation;
import austeretony.oxygen_shop.server.ShopManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShopNetworkOperationsHandlerServer implements NetworkOperationsHandler {

    @Override
    public int getId() {
        return ShopMain.SHOP_OPERATIONS_HANDLER_ID;
    }

    @Override
    public void process(EntityPlayer player, int operationIndex, ByteBuf buffer) {
        ShopOperation operation = getEnum(ShopOperation.values(), operationIndex);
        if (operation == null) return;

        if (operation == ShopOperation.PURCHASE) {
            int amount = buffer.readByte();
            Map<Long, Integer> entriesMap = new LinkedHashMap<>(amount);
            for (int i = 0; i < amount; i++) {
                entriesMap.put(buffer.readLong(), (int) buffer.readShort());
            }

            ShopManagerServer.instance().purchaseItems((EntityPlayerMP) player, entriesMap);
        } else if (operation == ShopOperation.SELL) {
            long entryId = buffer.readLong();
            final int amount = buffer.readShort();

            ShopManagerServer.instance().sellItem((EntityPlayerMP) player, entryId, amount);
        }
    }
}
