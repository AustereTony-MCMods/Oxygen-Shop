package austeretony.oxygen_shop.client;

import java.util.Set;

import austeretony.oxygen_core.client.sync.DataSyncHandlerClient;
import austeretony.oxygen_core.client.sync.DataSyncListener;
import austeretony.oxygen_shop.common.ShopOffer;
import austeretony.oxygen_shop.common.main.ShopMain;

public class OffersSyncHandlerClient implements DataSyncHandlerClient<ShopOffer> {

    @Override
    public int getDataId() {
        return ShopMain.SHOP_OFFERS_DATA_ID;
    }

    @Override
    public Class<ShopOffer> getDataContainerClass() {
        return ShopOffer.class;
    }

    @Override
    public Set<Long> getIds() {
        return ShopManagerClient.instance().getOffersContainer().getOfferIds();
    }

    @Override
    public void clearData() {
        ShopManagerClient.instance().getOffersContainer().reset();
    }

    @Override
    public ShopOffer getEntry(long entryId) {
        return ShopManagerClient.instance().getOffersContainer().getOffer(entryId);
    }

    @Override
    public void addEntry(ShopOffer entry) {
        ShopManagerClient.instance().getOffersContainer().addOffer(entry);
    }

    @Override
    public void save() {
        ShopManagerClient.instance().getOffersContainer().setChanged(true);
    }

    @Override
    public DataSyncListener getSyncListener() {
        return (updated)->ShopManagerClient.instance().getMenuManager().offersSynchronized();
    }
}
