package austeretony.oxygen_shop.server;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_shop.common.main.EnumShopStatusMessage;
import austeretony.oxygen_shop.common.main.ShopMain;
import net.minecraft.entity.player.EntityPlayerMP;

public class ShopManagerServer {

    private static ShopManagerServer instance;

    private final OffersContainerServer offersContainer = new OffersContainerServer();

    private final OffersManagerServer offersManager;

    private ShopManagerServer() {
        this.offersManager = new OffersManagerServer(this);
    }

    public static void create() {
        if (instance == null)
            instance = new ShopManagerServer();
    }

    public static ShopManagerServer instance() {
        return instance;
    }

    public OffersContainerServer getOffersContainer() {
        return this.offersContainer;
    }

    public OffersManagerServer getOffersManager() {
        return this.offersManager;
    }

    public void worldLoaded() {
        this.offersContainer.loadAsync();
    }

    public void sendStatusMessage(EntityPlayerMP playerMP, EnumShopStatusMessage status) {
        OxygenHelperServer.sendStatusMessage(playerMP, ShopMain.SHOP_MOD_INDEX, status.ordinal());
    }
}
