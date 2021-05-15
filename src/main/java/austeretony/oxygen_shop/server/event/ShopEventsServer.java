package austeretony.oxygen_shop.server.event;

import austeretony.oxygen_core.server.event.OxygenServerEvent;
import austeretony.oxygen_shop.server.ShopManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShopEventsServer {

    @SubscribeEvent
    public void onServerStarting(OxygenServerEvent.Starting event) {
        ShopManagerServer.instance().serverStarting();
    }
}
