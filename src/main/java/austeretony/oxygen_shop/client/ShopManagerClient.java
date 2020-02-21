package austeretony.oxygen_shop.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_shop.client.input.ShopKeyHandler;

public class ShopManagerClient {

    private static ShopManagerClient instance;

    private final OffersContainerClient offersContainer = new OffersContainerClient();

    private final ShoppingCartManagerClient shoppingCartManager;

    private final ShopMenuManager menuManager;

    private final ShopKeyHandler keyHandler = new ShopKeyHandler();

    private ShopManagerClient() {
        this.shoppingCartManager = new ShoppingCartManagerClient(this);
        this.menuManager = new ShopMenuManager(this);
        CommonReference.registerEvent(this.keyHandler);
    }

    private void registerPersistentData() {
        OxygenHelperClient.registerPersistentData(this.offersContainer);
    }

    public static void create() {
        if (instance == null) {
            instance = new ShopManagerClient();
            instance.registerPersistentData();
        }
    }

    public static ShopManagerClient instance() {
        return instance;
    }

    public OffersContainerClient getOffersContainer() {
        return this.offersContainer;
    }

    public ShoppingCartManagerClient getShoppingCartManager() {
        return this.shoppingCartManager;
    }

    public ShopMenuManager getMenuManager() {
        return this.menuManager;
    }

    public ShopKeyHandler getKeyHandler() {
        return this.keyHandler;
    }

    public void worldLoaded() {
        OxygenHelperClient.loadPersistentDataAsync(this.offersContainer);
        this.shoppingCartManager.reset();
    }
}
