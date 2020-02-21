package austeretony.oxygen_shop.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_shop.client.gui.shop.ShopMenuScreen;

public class ShopMenuManager {

    private final ShopManagerClient manager;

    protected ShopMenuManager(ShopManagerClient manager) {
        this.manager = manager;
    }

    public void openShopMenu() {
        ClientReference.displayGuiScreen(new ShopMenuScreen());
    }

    public void offersSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isShopMenuOpened())
                ((ShopMenuScreen) ClientReference.getCurrentScreen()).offersSynchronized();
        });
    }

    public void purchaseSuccessful(long balance, long offerId) {
        ClientReference.delegateToClientThread(()->{
            if (isShopMenuOpened())
                ((ShopMenuScreen) ClientReference.getCurrentScreen()).purchaseSuccessful(balance, offerId);
        });
    }

    public static boolean isShopMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof ShopMenuScreen;
    }
}
