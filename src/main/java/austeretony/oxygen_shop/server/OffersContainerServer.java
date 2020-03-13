package austeretony.oxygen_shop.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.JsonUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_shop.common.ShopOffer;

public class OffersContainerServer {

    private final Map<Long, ShopOffer> offers = new ConcurrentHashMap<>();

    public Set<Long> getOffersIds() {
        return this.offers.keySet();
    }

    public Collection<ShopOffer> getOffers() {
        return this.offers.values();
    }

    @Nullable
    public ShopOffer getOffer(long offerId) {
        return this.offers.get(offerId);
    }

    public void addOffer(ShopOffer offer) {
        this.offers.put(offer.getId(), offer);
    }

    public void removeOffer(long offerId) {
        this.offers.remove(offerId);
    }

    public Future<?> loadAsync() {
        return OxygenHelperServer.addIOTask(this::load);
    }

    private void load() {
        this.offers.clear();
        String pathStr = OxygenHelperCommon.getConfigFolder() + "data/server/shop/shop_offers.json";
        Path path = Paths.get(pathStr);
        if (Files.exists(path)) {
            try {    
                JsonArray objectsArray = JsonUtils.getExternalJsonData(pathStr).getAsJsonArray();
                ShopOffer offer;
                for (JsonElement jsonElement : objectsArray) {
                    offer = ShopOffer.fromJson(jsonElement.getAsJsonObject());
                    this.offers.put(offer.getId(), offer);
                }
            } catch (IOException exception) {
                OxygenMain.LOGGER.error("[Shop] Failed to load shop offers.");
                exception.printStackTrace();
            }
            OxygenMain.LOGGER.info("[Shop] Loaded {} shop offers.", this.offers.size());
        }
    }

    public Future<?> saveAsync() {
        return OxygenHelperServer.addIOTask(this::save);
    }

    private void save() {
        String pathStr = OxygenHelperCommon.getConfigFolder() + "data/server/shop/shop_offers.json";
        Path path = Paths.get(pathStr);
        try {    
            if (!Files.exists(path))
                Files.createDirectory(path.getParent());

            JsonArray offersArray = new JsonArray();
            this.offers.values().stream()
            .sorted((o1, o2)->o1.getStackWrapper().getRegistryName().compareTo(o2.getStackWrapper().getRegistryName()))
            .forEach((offer)->offersArray.add(offer.toJson()));

            JsonUtils.createExternalJsonFile(pathStr, offersArray);
        } catch (IOException exception) {
            OxygenMain.LOGGER.error("[Shop] Failed to save shop offers.");
            exception.printStackTrace();
        }
        OxygenMain.LOGGER.info("[Shop] Shop offers saved.");
    }
}
