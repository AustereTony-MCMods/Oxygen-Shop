package austeretony.oxygen_shop.server;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.EnumOxygenStatusMessage;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.Mailbox;
import austeretony.oxygen_mail.server.api.MailHelperServer;
import austeretony.oxygen_shop.common.ShopOffer;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.EnumShopPrivilege;
import austeretony.oxygen_shop.common.main.EnumShopStatusMessage;
import austeretony.oxygen_shop.common.main.ShopMain;
import austeretony.oxygen_shop.common.network.client.CPPurchaseSuccessful;
import net.minecraft.entity.player.EntityPlayerMP;

public class OffersManagerServer {

    private final ShopManagerServer manager;

    public OffersManagerServer(ShopManagerServer manager) {
        this.manager = manager;
    }

    public void purchaseItems(EntityPlayerMP playerMP, long[] offerIds, int[] amount) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (ShopConfig.ENABLE_SHOP_ACCESS_CLIENTSIDE.asBoolean() || OxygenHelperServer.checkTimeOut(playerUUID, ShopMain.SHOP_MENU_TIMEOUT_ID)) {
            if (PrivilegesProviderServer.getAsBoolean(playerUUID, EnumShopPrivilege.SHOP_ACCESS.id(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean())) {

                if (ShopConfig.SHOP_ITEMS_RECEIVING_MODE.asInt() == 0) {//mail
                    Mailbox mailbox = MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID);
                    int emptySlots = mailbox.getMaxCapacity() - mailbox.getMessagesAmount();
                    if (emptySlots < offerIds.length) {
                        this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.MAILBOX_FULL);
                        return;
                    }
                } else {//inventory
                    int emptySlots = InventoryProviderServer.getPlayerInventory().getEmptySlotsAmount(playerMP);
                    if (emptySlots < offerIds.length) {
                        this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.INVENTORY_FULL);
                        return;
                    }
                }

                ShopOffer[] offers = new ShopOffer[offerIds.length];
                ShopOffer offer;
                int index = 0;
                for (long offerId : offerIds) {
                    offer = this.manager.getOffersContainer().getOffer(offerId);
                    if (offer == null || amount[index] <= 0) return;
                    offers[index++] = offer;
                }

                index = 0;
                long totalPrice = 0L;
                for (ShopOffer shopOffer : offers)
                    totalPrice += shopOffer.getPrice() * amount[index++];

                if (CurrencyHelperServer.enoughCurrency(playerUUID, totalPrice, ShopConfig.SHOP_CURRENCY_INDEX.asInt())) {
                    CurrencyHelperServer.removeCurrency(playerUUID, totalPrice, ShopConfig.SHOP_CURRENCY_INDEX.asInt());

                    index = 0;
                    for (ShopOffer shopOffer : offers) {
                        final int itemAmount = shopOffer.getAmount() * amount[index++];

                        if (ShopConfig.SHOP_ITEMS_RECEIVING_MODE.asInt() == 0)//mail
                            MailHelperServer.sendSystemPackage(
                                    playerUUID, 
                                    "mail.sender.sys", 
                                    "shop.purchased", 
                                    "shop.purchasedItemMessage", 
                                    Parcel.create(shopOffer.getStackWrapper(), itemAmount), 
                                    true);
                        else//inventory
                            InventoryProviderServer.getPlayerInventory().addItem(playerMP, shopOffer.getStackWrapper(), itemAmount);

                        if (ShopConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Shop] Player purchased an item. Player: {}/{}, offer id: {}, item: {}, amount: {}, total price: {}.",
                                    CommonReference.getName(playerMP),
                                    CommonReference.getPersistentUUID(playerMP),
                                    shopOffer.getId(),
                                    shopOffer.getStackWrapper().registryName,
                                    itemAmount,
                                    itemAmount * shopOffer.getPrice());
                    }

                    SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.SELL.id);

                    OxygenMain.network().sendTo(
                            new CPPurchaseSuccessful(
                                    CurrencyHelperServer.getCurrency(playerUUID, ShopConfig.SHOP_CURRENCY_INDEX.asInt()),
                                    offerIds.length == 1 ? offerIds[0] : 0L),
                            playerMP);
                    this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.PURCHASE_SUCCESSFUL);
                }
            } else
                this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.PURCHASE_FAILED);
        } else
            OxygenHelperServer.sendStatusMessage(playerMP, OxygenMain.OXYGEN_CORE_MOD_INDEX, EnumOxygenStatusMessage.ACTION_TIMEOUT.ordinal()); 
    }

    //management

    public void reloadOffers(@Nullable EntityPlayerMP playerMP) {
        if (playerMP == null || (ShopConfig.ENABLE_SHOP_MANAGEMENT.asBoolean() 
                && PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumShopPrivilege.SHOP_MANAGEMENT.id(), false)))
            OxygenHelperServer.addRoutineTask(()->this.reload(playerMP));
    }

    private void reload(@Nullable EntityPlayerMP playerMP) {
        OxygenMain.LOGGER.info("[Shop] Reloading shop offers...");        
        Future future = this.manager.getOffersContainer().loadAsync();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException exception) {
            exception.printStackTrace();
        }
        if (playerMP != null) {
            OxygenManagerServer.instance().getDataSyncManager().syncData(playerMP, ShopMain.SHOP_OFFERS_DATA_ID);
            this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.OFFERS_RELOADED);
        }
        OxygenMain.LOGGER.info("[Shop] Shop offers reloaded.");
    }

    public void saveOffers(@Nullable EntityPlayerMP playerMP) {
        if (playerMP == null || (ShopConfig.ENABLE_SHOP_MANAGEMENT.asBoolean() 
                && PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumShopPrivilege.SHOP_MANAGEMENT.id(), false))) {
            this.manager.getOffersContainer().saveAsync();
            if (playerMP != null)
                this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.OFFERS_SAVED);
            OxygenMain.LOGGER.info("[Shop] Shop offers saved.");
        }
    }

    public void createOffer(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper, int amount, long price, int discount) {
        if (ShopConfig.ENABLE_SHOP_MANAGEMENT.asBoolean() 
                && PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumShopPrivilege.SHOP_MANAGEMENT.id(), false)) {
            ShopOffer offer = new ShopOffer(
                    TimeHelperServer.getCurrentMillis(),
                    stackWrapper,
                    amount,
                    price,
                    discount);
            this.manager.getOffersContainer().addOffer(offer);

            this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.OFFER_CREATED);
            OxygenMain.LOGGER.info("[Shop] Created offer <{}>. Player: {}/{}, item: {}, amount: {}, price: {}, discount: {}%.",
                    offer.getId(),
                    CommonReference.getName(playerMP),
                    CommonReference.getPersistentUUID(playerMP),
                    stackWrapper.registryName,
                    amount,
                    price,
                    discount);
        }
    }

    public void removeOffer(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper) {
        if (ShopConfig.ENABLE_SHOP_MANAGEMENT.asBoolean() 
                && PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumShopPrivilege.SHOP_MANAGEMENT.id(), false)) {
            Iterator<ShopOffer> iterator = this.manager.getOffersContainer().getOffers().iterator();
            ShopOffer offer = null;
            while (iterator.hasNext()) {
                offer = iterator.next();
                if (offer.getStackWrapper().equals(stackWrapper))
                    iterator.remove();
            }

            if (offer != null) {
                this.manager.sendStatusMessage(playerMP, EnumShopStatusMessage.OFFER_REMOVED);
                OxygenMain.LOGGER.info("[Shop] Removed offer <{}>. Player: {}/{}, item: {}.",
                        offer.getId(),
                        CommonReference.getName(playerMP),
                        CommonReference.getPersistentUUID(playerMP),
                        stackWrapper.registryName);
            }
        }
    }
}
