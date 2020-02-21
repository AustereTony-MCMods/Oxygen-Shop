package austeretony.oxygen_shop.common.main;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.settings.SettingsScreen;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_core.server.network.NetworkRequestsRegistryServer;
import austeretony.oxygen_core.server.timeout.TimeOutRegistryServer;
import austeretony.oxygen_shop.client.OffersSyncHandlerClient;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.ShopStatusMessagesHandler;
import austeretony.oxygen_shop.client.command.ShopArgumentClient;
import austeretony.oxygen_shop.client.event.ShopEventsClient;
import austeretony.oxygen_shop.client.gui.settings.TradeSettingsContainer;
import austeretony.oxygen_shop.client.gui.shop.ShopMenuScreen;
import austeretony.oxygen_shop.client.settings.EnumShopClientSetting;
import austeretony.oxygen_shop.client.settings.gui.EnumShopGUISetting;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.network.client.CPPurchaseSuccessful;
import austeretony.oxygen_shop.common.network.server.SPPurchaseItems;
import austeretony.oxygen_shop.server.OffersSyncHandlerServer;
import austeretony.oxygen_shop.server.ShopManagerServer;
import austeretony.oxygen_shop.server.command.ShopArgumentOperator;
import austeretony.oxygen_shop.server.event.ShopEventsServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = ShopMain.MODID, 
        name = ShopMain.NAME, 
        version = ShopMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.10.3,);after:oxygen_mail@[0.10.1,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = ShopMain.VERSIONS_FORGE_URL)
public class ShopMain {

    public static final String 
    MODID = "oxygen_shop",    
    NAME = "Oxygen: Shop",
    VERSION = "0.10.0",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Shop/info/mod_versions_forge.json";

    public static final int 
    SHOP_MOD_INDEX = 15,

    SHOP_OFFERS_DATA_ID = 150,

    SHOP_SCREEN_ID = 150,

    BUY_REQUEST_ID = 150,

    SHOP_MENU_TIMEOUT_ID = 150;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new ShopConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgument(new ShopArgumentClient());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        ShopManagerServer.create();
        CommonReference.registerEvent(new ShopEventsServer());
        OxygenHelperServer.registerDataSyncHandler(new OffersSyncHandlerServer());
        NetworkRequestsRegistryServer.registerRequest(BUY_REQUEST_ID, 1000);
        TimeOutRegistryServer.registerTimeOut(SHOP_MENU_TIMEOUT_ID, ShopConfig.SHOP_MENU_OPERATIONS_TIMEOUT_MILLIS.asInt());
        CommandOxygenOperator.registerArgument(new ShopArgumentOperator());
        EnumShopPrivilege.register();
        if (event.getSide() == Side.CLIENT) {
            ShopManagerClient.create();
            CommonReference.registerEvent(new ShopEventsClient());
            OxygenGUIHelper.registerOxygenMenuEntry(ShopMenuScreen.SHOP_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new ShopStatusMessagesHandler());
            OxygenHelperClient.registerDataSyncHandler(new OffersSyncHandlerClient());
            EnumShopClientSetting.register();
            EnumShopGUISetting.register();
            SettingsScreen.registerSettingsContainer(new TradeSettingsContainer());
        }
    }

    public static void addDefaultPrivileges() {
        if (PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).getPrivilege(EnumShopPrivilege.SHOP_ACCESS.id()) == null) {
            PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).addPrivileges(
                    PrivilegeUtils.getPrivilege(EnumShopPrivilege.SHOP_ACCESS.id(), true),
                    PrivilegeUtils.getPrivilege(EnumShopPrivilege.SHOP_MANAGEMENT.id(), true));
            OxygenMain.LOGGER.info("[Shop] Default Operator role privileges added.");
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPPurchaseSuccessful.class);

        OxygenMain.network().registerPacket(SPPurchaseItems.class);
    }
}
