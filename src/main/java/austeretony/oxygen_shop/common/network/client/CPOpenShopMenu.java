package austeretony.oxygen_shop.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_shop.client.ShopMenuManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPOpenShopMenu extends Packet {

    public CPOpenShopMenu() {}

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {}

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        OxygenHelperClient.addRoutineTask(ShopMenuManager::openShopMenuDelegated);
    }


}
