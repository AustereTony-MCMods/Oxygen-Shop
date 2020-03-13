package austeretony.oxygen_shop.common.main;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumShopStatusMessage {

    PURCHASE_SUCCESSFUL("purchaseSuccessful"),
    PURCHASE_SUCCESSFUL_MAIL("purchaseSuccessfulMail"),
    PURCHASE_FAILED("purchaseFailed"),

    OFFERS_RELOADED("offersReloaded"),
    OFFERS_SAVED("offersSaved"),
    OFFER_CREATED("offerCreated"),
    OFFER_CREATION_FAILED("offerCreationFailed"),
    OFFER_REMOVED("offerRemoved");

    private final String status;

    EnumShopStatusMessage(String status) {
        this.status = "oxygen_shop.status.message." + status;
    }

    public String localizedName() {
        return ClientReference.localize(this.status);
    }
}
