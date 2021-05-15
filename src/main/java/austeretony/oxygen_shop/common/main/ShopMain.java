package austeretony.oxygen_shop.common.main;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.command.ShopArgumentClient;
import austeretony.oxygen_shop.client.event.ShopEventsClient;
import austeretony.oxygen_shop.client.gui.shop.ShopScreen;
import austeretony.oxygen_shop.client.network.operation.ShopNetworkOperationsHandlerClient;
import austeretony.oxygen_shop.client.settings.ShopSettings;
import austeretony.oxygen_shop.client.sync.ShopDataSyncHandlerClient;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.server.command.ShopArgumentOperator;
import austeretony.oxygen_shop.server.event.ShopEventsServer;
import austeretony.oxygen_shop.server.network.operation.ShopNetworkOperationsHandlerServer;
import austeretony.oxygen_shop.server.sync.ShopDataSyncHandlerServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = ShopMain.MOD_ID,
        name = ShopMain.NAME,
        version = ShopMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.12.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = ShopMain.VERSIONS_FORGE_URL)
public class ShopMain {

    public static final String
            MOD_ID = "oxygen_shop",
            NAME = "Oxygen: Shop",
            VERSION = "0.12.0",
            VERSION_CUSTOM = VERSION + ":beta:0",
            VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Shop/info/versions.json";

    //oxygen module index
    public static final int MODULE_INDEX = 9;

    //screen id
    public static final int SCREEN_ID_SHOP = 90;

    //timeout ids
    public static final int TIMEOUT_SHOP_OPERATIONS = 90;

    //data id
    public static final int DATA_ID_SHOP = 90;

    //key binding id
    public static final int KEYBINDING_ID_OPEN_SHOP_SCREEN = 90;

    //operations handler id
    public static final int SHOP_OPERATIONS_HANDLER_ID = 90;

    //operations
    public static String
            OPERATION_ITEM_BUY = "shop:item_buy",
            OPERATION_ITEM_SELL = "shop:item_sell";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenCommon.registerConfig(new ShopConfig());
        if (event.getSide() == Side.CLIENT) {
            CommandOxygenClient.registerArgument(new ShopArgumentClient());
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_OPEN_SHOP_SCREEN,
                    "key.oxygen_shop.open_shop_screen",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    ShopConfig.SHOP_SCREEN_KEY::asInt,
                    ShopConfig.ENABLE_SHOP_SCREEN_KEY::asBoolean,
                    true,
                    () -> OxygenClient.openScreen(SCREEN_ID_SHOP));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftCommon.registerEventHandler(new ShopEventsServer());
        OxygenServer.registerTimeout(TIMEOUT_SHOP_OPERATIONS, ShopConfig.SHOP_MENU_OPERATIONS_TIMEOUT_MILLIS.asInt());
        OxygenServer.registerDataSyncHandler(new ShopDataSyncHandlerServer());
        OxygenServer.registerOperationsHandler(new ShopNetworkOperationsHandlerServer());
        ShopPrivileges.register();
        CommandOxygenOperator.registerArgument(new ShopArgumentOperator());
        if (event.getSide() == Side.CLIENT) {
            ShopManagerClient.instance();
            MinecraftCommon.registerEventHandler(new ShopEventsClient());
            OxygenClient.registerDataSyncHandler(new ShopDataSyncHandlerClient());
            OxygenClient.registerOperationsHandler(new ShopNetworkOperationsHandlerClient());
            ShopSettings.register();
            OxygenMenuHelper.addMenuEntry(ShopScreen.SHOP_SCREEN_MENU_ENTRY);
            OxygenClient.registerScreen(SCREEN_ID_SHOP, ShopScreen::open,
                    ShopConfig.ENABLE_SHOP_ACCESS_CLIENT_SIDE::asBoolean);
        }
    }
}
