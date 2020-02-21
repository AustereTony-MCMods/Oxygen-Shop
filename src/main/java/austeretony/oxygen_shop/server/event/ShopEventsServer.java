package austeretony.oxygen_shop.server.event;

import austeretony.oxygen_core.server.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen_core.server.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.server.ShopManagerServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShopEventsServer {

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        ShopMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {
        ShopManagerServer.instance().worldLoaded();
    }
}
