package austeretony.oxygen_shop.client.event;

import austeretony.oxygen_core.client.event.OxygenClientInitializedEvent;
import austeretony.oxygen_shop.client.ShopManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShopEventsClient {

    @SubscribeEvent
    public void onClientInitialized(OxygenClientInitializedEvent event) {
        ShopManagerClient.instance().clientInitialized();
    }
}
