package austeretony.oxygen_shop.client.command;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_shop.client.ShopManagerClient;
import austeretony.oxygen_shop.client.ShopMenuManager;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.main.EnumShopPrivilege;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class ShopArgumentClient implements ArgumentExecutor {

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            if (ShopConfig.ENABLE_SHOP_ACCESS_CLIENTSIDE.asBoolean() 
                    && PrivilegesProviderClient.getAsBoolean(EnumShopPrivilege.SHOP_ACCESS.id(), ShopConfig.ENABLE_SHOP_ACCESS.asBoolean()))
                OxygenHelperClient.scheduleTask(ShopMenuManager::openShopMenuDelegated, 100L, TimeUnit.MILLISECONDS);
        } else if (args.length == 2) {
            if (args[1].equals("-reset-data")) {
                ShopManagerClient.instance().getOffersContainer().reset();
                ClientReference.showChatMessage("oxygen_shop.command.client.dataReset");
            }
        }
    }
}
