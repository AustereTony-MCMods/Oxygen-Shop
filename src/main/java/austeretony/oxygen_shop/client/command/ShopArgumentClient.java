package austeretony.oxygen_shop.client.command;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_shop.common.main.ShopMain;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class ShopArgumentClient implements CommandArgument {

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            OxygenClient.openScreenWithDelay(ShopMain.SCREEN_ID_SHOP);
        }
    }
}
