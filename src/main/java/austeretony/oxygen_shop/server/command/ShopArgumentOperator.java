package austeretony.oxygen_shop.server.command;

import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_shop.server.ShopManagerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class ShopArgumentOperator implements ArgumentExecutor {

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP playerMP = null;
        if (sender instanceof EntityPlayerMP)
            playerMP = CommandBase.getCommandSenderAsPlayer(sender);
        if (args.length >= 2) {
            if (args[1].equals("-reload-offers"))
                ShopManagerServer.instance().getOffersManager().reloadOffers(playerMP);
            else if (args[1].equals("-save-offers"))
                ShopManagerServer.instance().getOffersManager().saveOffers(playerMP);
            else if (args[1].equals("-create-offer")) {
                if (args.length == 5) {
                    if (!(sender instanceof EntityPlayerMP))
                        throw new WrongUsageException("Command available only for player!");
                    if (playerMP.getHeldItemMainhand() == ItemStack.EMPTY) 
                        throw new WrongUsageException("Main hand is empty!");

                    ItemStackWrapper stackWrapper = ItemStackWrapper.getFromStack(playerMP.getHeldItemMainhand());
                    int 
                    amount = CommandBase.parseInt(args[2], 0, Short.MAX_VALUE),
                    discount = CommandBase.parseInt(args[4], 0, 100);
                    long price = CommandBase.parseLong(args[3], 1, Long.MAX_VALUE);
                    ShopManagerServer.instance().getOffersManager().createOffer(playerMP, stackWrapper, amount, price, discount);
                }
            } else if (args[1].equals("-remove-offer")) {
                if (!(sender instanceof EntityPlayerMP))
                    throw new WrongUsageException("Command available only for player!");
                if (playerMP.getHeldItemMainhand() == ItemStack.EMPTY) 
                    throw new WrongUsageException("Main hand is empty!");

                ItemStackWrapper stackWrapper = ItemStackWrapper.getFromStack(playerMP.getHeldItemMainhand());
                ShopManagerServer.instance().getOffersManager().removeOffer(playerMP, stackWrapper);
            }
        }
    }
}