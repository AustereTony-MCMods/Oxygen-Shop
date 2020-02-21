package austeretony.oxygen_shop.client.event;

import austeretony.oxygen_core.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_shop.client.ShopManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShopEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        ShopManagerClient.instance().worldLoaded();
    }
}
