package austeretony.oxygen_shop.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_shop.client.ShopManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPPurchaseSuccessful extends Packet {

    private long balance, offerId;

    public CPPurchaseSuccessful() {}

    public CPPurchaseSuccessful(long balance, long offerId) {
        this.balance = balance;
        this.offerId = offerId;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.balance);
        buffer.writeLong(this.offerId);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long 
        balance = buffer.readLong(),
        offerId = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->ShopManagerClient.instance().getShoppingCartManager().purchaseSuccessful(balance, offerId));
    }
}
