package austeretony.oxygen_shop.client;

import java.util.LinkedHashMap;
import java.util.Map;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.network.server.SPPurchaseItems;

public class ShoppingCartManagerClient {

    private final ShopManagerClient manager;

    private final Map<Long, Integer> cart = new LinkedHashMap<>();

    public ShoppingCartManagerClient(ShopManagerClient manager) {
        this.manager = manager;
    }

    public Map<Long, Integer> getCart() {
        return this.cart;
    }

    public boolean canItemBeAdded(long offerId) {
        return this.cart.size() < ShopConfig.SHOP_CART_SIZE.asInt() && !this.cart.containsKey(offerId);
    }

    public void addItemToCart(long offerId) {
        this.cart.put(offerId, 1);
    }

    public void removeItemFromCart(long offerId) {
        this.cart.remove(offerId);
    }

    public int decrementItemAmount(long offerId) {
        Integer amount = this.cart.get(offerId);
        if (amount == 1) 
            return amount;
        this.cart.put(offerId, --amount);
        return amount;
    }

    public int incrementItemAmount(long offerId) {
        Integer amount = this.cart.get(offerId);
        this.cart.put(offerId, ++amount);
        return amount;
    }

    public void purchaseItem(long offerId) {
        if (this.cart.containsKey(offerId))
            OxygenMain.network().sendToServer(new SPPurchaseItems(new long[]{offerId}, new int[]{this.cart.get(offerId)}));
    }

    public void purchaseItems() {
        if (!this.cart.isEmpty()) {
            long[] offerIds = new long[this.cart.size()];
            int[] amount = new int[offerIds.length];
            int index = 0;
            for (Map.Entry<Long, Integer> cartEntry : this.cart.entrySet()) {
                offerIds[index] = cartEntry.getKey();
                amount[index++] = cartEntry.getValue();
            }

            OxygenMain.network().sendToServer(new SPPurchaseItems(offerIds, amount));
        }
    }

    public void purchaseSuccessful(long balance, long offerId) {
        if (offerId == 0L)
            this.cart.clear();
        else
            this.cart.remove(offerId);

        this.manager.getMenuManager().purchaseSuccessful(balance, offerId);
    }

    public void reset() {
        this.cart.clear();
    }
}
