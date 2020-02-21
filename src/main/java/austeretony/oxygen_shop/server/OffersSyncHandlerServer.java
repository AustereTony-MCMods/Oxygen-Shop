package austeretony.oxygen_shop.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_shop.common.ShopOffer;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.EnumShopPrivilege;
import austeretony.oxygen_shop.common.main.ShopMain;

public class OffersSyncHandlerServer implements DataSyncHandlerServer<ShopOffer> {

    @Override
    public int getDataId() {
        return ShopMain.SHOP_OFFERS_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return (ShopConfig.ENABLE_SHOP_ACCESS_CLIENTSIDE.asBoolean() || OxygenHelperServer.checkTimeOut(playerUUID, ShopMain.SHOP_MENU_TIMEOUT_ID))
                && PrivilegesProviderServer.getAsBoolean(playerUUID, EnumShopPrivilege.SHOP_ACCESS.id(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean());
    }

    @Override
    public Set<Long> getIds(UUID playerUUID) {
        return ShopManagerServer.instance().getOffersContainer().getOffersIds();
    }

    @Override
    public ShopOffer getEntry(UUID playerUUID, long entryId) {
        return ShopManagerServer.instance().getOffersContainer().getOffer(entryId);
    }
}
