package austeretony.oxygen_shop.server.sync;

import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import austeretony.oxygen_shop.server.ShopManagerServer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class ShopDataSyncHandlerServer implements DataSyncHandlerServer<ShopEntry> {

    @Override
    public int getDataId() {
        return ShopMain.DATA_ID_SHOP;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return true;
    }

    @Nonnull
    @Override
    public Map<Long, ShopEntry> getDataMap(UUID playerUUID) {
        return ShopManagerServer.instance().getShopEntriesMap();
    }
}
