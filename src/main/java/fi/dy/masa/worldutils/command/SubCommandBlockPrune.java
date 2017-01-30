package fi.dy.masa.worldutils.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import fi.dy.masa.worldutils.WorldUtils;
import fi.dy.masa.worldutils.data.BlockTools;
import fi.dy.masa.worldutils.util.BlockData;
import fi.dy.masa.worldutils.util.BlockUtils;

public class SubCommandBlockPrune extends SubCommand
{
    private static List<String> toReplace = new ArrayList<String>();
    private static String replacement = "minecraft:air";

    public SubCommandBlockPrune(CommandWorldUtils baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("execute");
        this.subSubCommands.add("execute-also-in-loaded-chunks");
        this.subSubCommands.add("removelist");
        this.subSubCommands.add("replacement");
    }

    @Override
    public String getName()
    {
        return "blockprune";
    }

    @Override
    public void printHelpGeneric(ICommandSender sender)
    {
        this.sendMessage(sender, "worldutils.commands.help.generic.runhelpforallcommands", this.getUsageStringCommon() + " help");
    }

    @Override
    public void printFullHelp(ICommandSender sender, String[] args)
    {
        this.prinHelpRemovelist(sender);
        this.prinHelpReplacement(sender);
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " execute [dimension id]"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " execute-also-in-loaded-chunks [dimension id]"));
    }

    private void prinHelpRemovelist(ICommandSender sender)
    {
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " removelist clear"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " removelist list"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " removelist add <block | id>[@meta] ... Ex: minecraft:ice minecraft:wool@5"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " removelist add <block[prop1=val1,prop2=val2]> ... Ex: minecraft:stone[variant=granite]"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " removelist remove <block | id>[@meta] ... Ex: minecraft:ice minecraft:wool@5"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " removelist remove <block[prop1=val1,prop2=val2]> ... Ex: minecraft:stone[variant=granite]"));
    }

    private void prinHelpReplacement(ICommandSender sender)
    {
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " replacement set <block | id>[@meta] Ex: minecraft:ice minecraft:wool@5"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " replacement set <block[prop1=val1,prop2=val2]> Ex: minecraft:stone[variant=granite]"));
        sender.sendMessage(new TextComponentString(this.getUsageStringCommon() + " replacement print"));
    }

    @Override
    protected List<String> getTabCompletionsSub(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            return Collections.emptyList();
        }

        String cmd = args[0];
        args = CommandWorldUtils.dropFirstStrings(args, 1);

        if (cmd.equals("removelist"))
        {
            if (args.length == 1)
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "add", "clear", "list", "remove");
            }
            else if (args.length >= 2)
            {
                cmd = args[0];

                if (cmd.equals("add"))
                {
                    return CommandBase.getListOfStringsMatchingLastWord(args, BlockUtils.getAllBlockNames());
                }
                else if (cmd.equals("remove"))
                {
                    return CommandBase.getListOfStringsMatchingLastWord(args, toReplace);
                }
            }
        }
        else if (cmd.equals("replacement"))
        {
            if (args.length == 1)
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "print", "set");
            }
            else if (args.length >= 2)
            {
                cmd = args[0];

                if (cmd.equals("set"))
                {
                    List<String> options = new ArrayList<String>();

                    for (ResourceLocation rl : Block.REGISTRY.getKeys())
                    {
                        options.add(rl.toString());
                    }

                    return CommandBase.getListOfStringsMatchingLastWord(args, options);
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1 || args[0].equals("help") || (args[0].equals("execute") == false && args.length < 2))
        {
            this.printFullHelp(sender, args);
            return;
        }

        String cmd = args[0];
        args = CommandWorldUtils.dropFirstStrings(args, 1);

        if (cmd.equals("removelist") && args.length >= 1)
        {
            cmd = args[0];
            args = CommandWorldUtils.dropFirstStrings(args, 1);

            if (cmd.equals("clear") && args.length == 0)
            {
                toReplace.clear();
                this.sendMessage(sender, "worldutils.commands.blockprune.info.removelist.cleared");
            }
            else if (cmd.equals("list") && args.length == 0)
            {
                WorldUtils.logger.info("----------------------------------");
                WorldUtils.logger.info("  Blocks to be removed/replaced:  ");
                WorldUtils.logger.info("----------------------------------");

                for (String str : toReplace)
                {
                    WorldUtils.logger.info(str);
                    sender.sendMessage(new TextComponentString(str));
                }

                WorldUtils.logger.info("-------------- END ---------------");

                this.sendMessage(sender, "worldutils.commands.blockprune.info.removelist.print");
            }
            else if (cmd.equals("add") && args.length > 0)
            {
                for (int i = 0; i < args.length; i++)
                {
                    toReplace.add(args[i]);
                    this.sendMessage(sender, "worldutils.commands.generic.list.add", args[i]);
                }
            }
            else if (cmd.equals("remove") && args.length > 0)
            {
                for (int i = 0; i < args.length; i++)
                {
                    if (toReplace.remove(args[i]))
                    {
                        this.sendMessage(sender, "worldutils.commands.generic.list.remove.success", args[i]);
                    }
                    else
                    {
                        this.sendMessage(sender, "worldutils.commands.generic.list.remove.failure", args[i]);
                    }
                }
            }
            else
            {
                this.prinHelpRemovelist(sender);
            }
        }
        else if (cmd.equals("replacement"))
        {
            cmd = args[0];
            args = CommandWorldUtils.dropFirstStrings(args, 1);

            if (cmd.equals("print") && args.length == 0)
            {
                this.replacementPrint(replacement, sender);
            }
            else if (cmd.equals("set") && args.length == 1)
            {
                replacement = args[0];
                this.replacementPrint(replacement, sender);
            }
            else
            {
                this.prinHelpReplacement(sender);
            }
        }
        else if ((cmd.equals("execute") || cmd.equals("execute-also-in-loaded-chunks")) && args.length <= 1)
        {
            this.sendMessage(sender, "worldutils.commands.blockprune.execute.start");
            int dimension = this.getDimension(cmd, args, sender);
            BlockTools.instance().replaceBlocks(dimension, replacement, toReplace, cmd.equals("execute-also-in-loaded-chunks"), sender);
        }
        else
        {
            this.printFullHelp(sender, args);
        }
    }

    private void replacementPrint(String replacement, ICommandSender sender) throws CommandException
    {
        BlockData type = BlockData.parseBlockTypeFromString(replacement);

        if (type != null)
        {
            this.sendMessage(sender, "worldutils.commands.blockprune.info.replacement.print.valid", type.toString());
        }
        else
        {
            throwCommand("worldutils.commands.blockprune.info.replacement.print.invalid", replacement);
        }
    }

    private int getDimension(String cmd, String[] args, ICommandSender sender) throws CommandException
    {
        int dimension = sender instanceof EntityPlayer ? ((EntityPlayer) sender).getEntityWorld().provider.getDimension() : 0;

        if (args.length == 1)
        {
            dimension = CommandBase.parseInt(args[0]);
        }
        else if (args.length > 1)
        {
            throwUsage(this.getUsageStringCommon() + " " + cmd + " [dimension id]");
        }

        return dimension;
    }
}