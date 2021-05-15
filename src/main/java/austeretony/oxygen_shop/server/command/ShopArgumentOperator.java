package austeretony.oxygen_shop.server.command;

import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_shop.common.config.ShopConfig;
import austeretony.oxygen_shop.common.shop.ShopEntry;
import austeretony.oxygen_shop.server.ShopManagerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ShopArgumentOperator implements CommandArgument {

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 2) {
            if (args[1].equals("reload")) {
                Future<Boolean> future = OxygenServer.addTask(() -> {
                    ShopManagerServer.instance().loadShopEntries();
                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Shop entries reloaded"));
                        OxygenMain.logInfo(1, "[Shop] {} successfully reloaded shop entries.", sender.getName());
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to reload shop entries"));
                        OxygenMain.logInfo(1, "[Shop] {} failed to reloaded shop entries.", sender.getName());
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        } else if (args.length == 3) {
            if (args[1].equals("open")) {
                EntityPlayerMP playerMP = CommandBase.getPlayer(server, sender, args[2]);

                OxygenServer.addTask(() -> ShopManagerServer.instance().openShopScreen(playerMP));
            } else if (args[1].equals("entry-remove")) {
                if (!ShopConfig.ENABLE_SHOP_MANAGEMENT_IN_GAME.asBoolean()) {
                    throw new CommandException("In game shop management disabled");
                }
                long entryId = CommandBase.parseLong(args[2], 0L, Long.MAX_VALUE);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    ShopManagerServer manager = ShopManagerServer.instance();
                    ShopEntry shopEntry = manager.removeShopEntry(entryId);
                    if (shopEntry != null) {
                        manager.saveShopEntries();

                        OxygenMain.logInfo(1, "[Shop] {} removed shop entry: {}",
                                sender.getName(), shopEntry.getId());

                        return true;
                    }
                    return false;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Shop entry removed: " + args[2]));
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to remove shop entry: " + args[2]));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        } else if (args.length == 7) {
            if (args[1].equals("entry-add")) {
                if (!(sender instanceof EntityPlayer)) {
                    throw new CommandException("Command available for players only");
                }
                if (!ShopConfig.ENABLE_SHOP_MANAGEMENT_IN_GAME.asBoolean()) {
                    throw new CommandException("In game shop management disabled");
                }

                long entryId = CommandBase.parseLong(args[2], 0L, Long.MAX_VALUE);
                ShopEntry.Type type = parseShopEntryType(args[3]);
                long price = CommandBase.parseLong(args[4], 1L, Long.MAX_VALUE);
                float discount = (float) CommandBase.parseDouble(args[5], 0.0, 1.0);
                ItemStackWrapper stackWrapper = getHeldItemStackWrapper(CommandBase.getCommandSenderAsPlayer(sender));
                int itemsAmount = CommandBase.parseInt(args[6], 1, Short.MAX_VALUE);

                Future<Boolean> future = OxygenServer.addTask(() -> {
                    ShopManagerServer manager = ShopManagerServer.instance();
                    ShopEntry shopEntry = new ShopEntry(entryId, type, price, discount, stackWrapper, itemsAmount);

                    manager.addShopEntry(shopEntry);
                    manager.saveShopEntries();

                    OxygenMain.logInfo(1, "[Shop] {} added shop entry: {}",
                            sender.getName(), shopEntry);

                    return true;
                });

                try {
                    Boolean result = future.get();
                    if (result != null && result) {
                        sender.sendMessage(new TextComponentString("Shop entry added"));
                    } else {
                        sender.sendMessage(new TextComponentString("Failed to add shop entry"));
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "reload", "open", "entry-remove", "entry-add");
        }
        return Collections.emptyList();
    }

    private ShopEntry.Type parseShopEntryType(String str) throws CommandException {
        try {
            return ShopEntry.Type.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new CommandException("Invalid shop entry type: " + str);
        }
    }
}
