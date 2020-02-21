package austeretony.oxygen_shop.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.server.ShopManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPPurchaseItems extends Packet {

    private long[] offerIds;

    private int[] amount;

    public SPPurchaseItems() {}

    public SPPurchaseItems(long[] offerIds, int[] amount) {
        this.offerIds = offerIds;
        this.amount = amount;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.offerIds.length);
        for (int i = 0; i < this.offerIds.length; i++) {
            buffer.writeLong(this.offerIds[i]);
            buffer.writeShort(this.amount[i]);
        }
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), ShopMain.BUY_REQUEST_ID)) {           
            final int size = buffer.readByte();
            final long[] offerIds = new long[size > ShopConfig.SHOP_CART_SIZE.asInt() ? ShopConfig.SHOP_CART_SIZE.asInt() : size];
            final int[] amount = new int[offerIds.length];
            for (int i = 0; i < offerIds.length; i++) {
                offerIds[i] = buffer.readLong();
                amount[i] = buffer.readShort();
            }
            OxygenHelperServer.addRoutineTask(()->ShopManagerServer.instance().getOffersManager().purchaseItems(playerMP, offerIds, amount));
        }
    }
}
