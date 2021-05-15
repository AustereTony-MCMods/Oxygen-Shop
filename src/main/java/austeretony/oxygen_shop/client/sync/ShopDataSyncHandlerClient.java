package austeretony.oxygen_shop.client.sync;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.gui.shop.ShopScreen;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.shop.ShopEntry;

import javax.annotation.Nullable;
import java.util.Map;

public class ShopDataSyncHandlerClient implements DataSyncHandlerClient<ShopEntry> {

    @Override
    public int getDataId() {
        return ShopMain.DATA_ID_SHOP;
    }

    @Override
    public Class<ShopEntry> getSynchronousEntryClass() {
        return ShopEntry.class;
    }

    @Nullable
    @Override
    public Map<Long, ShopEntry> getDataMap() {
        return ShopManagerClient.instance().getShopEntriesMap();
    }

    @Override
    public void clear() {
        ShopManagerClient.instance().reset();
    }

    @Override
    public void save() {
        ShopManagerClient.instance().markChanged();
    }

    @Nullable
    @Override
    public DataSyncListener getSyncListener() {
        return updated -> ShopScreen.dataSynchronized();
    }
}
