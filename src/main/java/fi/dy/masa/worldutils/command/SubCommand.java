package fi.dy.masa.worldutils.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import fi.dy.masa.worldutils.WorldUtils;
import fi.dy.masa.worldutils.util.BlockData;

public abstract class SubCommand implements ISubCommand
{
    public static final String EMPTY_STRING = "";
    protected final CommandWorldUtils baseCommand;
    protected final ArrayList<String> subSubCommands = new ArrayList<String>();

    public SubCommand(CommandWorldUtils baseCommand)
    {
        this.baseCommand = baseCommand;
        this.subSubCommands.add("help");
    }

    public CommandWorldUtils getBaseCommand()
    {
        return this.baseCommand;
    }

    @Override
    public List<String> getSubSubCommands()
    {
        return this.subSubCommands;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if ((this.getSubSubCommands().size() > 1 && args.length == 1) || (args.length == 2 && args[0].equals("help")))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getSubSubCommands());
        }

        return this.getTabCompletionsSub(server, sender, args, targetPos);
    }

    protected List<String> getTabCompletionsSub(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.emptyList();
    }

    @Override
    public void printHelpGeneric(ICommandSender sender)
    {
        this.sendMessage(sender, "worldutils.commands.help.generic.availablevariants", String.join(", ", this.subSubCommands));
    }

    @Override
    public void printFullHelp(ICommandSender sender, String[] args)
    {
        this.printHelpGeneric(sender);
    }

    protected String getUsageStringCommon()
    {
        return "/" + this.getBaseCommand().getName() + " " + this.getName();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/worldutils command"
        if (args.length < 1)
        {
            this.printHelpGeneric(sender);
        }
        // "/worldutils command [help|unknown]"
        else if (args.length == 1)
        {
            if (args[0].equals("help"))
            {
                this.printHelpGeneric(sender);
            }
            else if (this.subSubCommands.contains(args[0]) == false)
            {
                throwCommand("worldutils.commands.error.unknowncommandvariant", args[0]);
            }
        }
        // "/worldutils command help subsubcommand [args]"
        else if (args.length >= 2 && args[0].equals("help"))
        {
            if (this.subSubCommands.contains(args[1]))
            {
                this.printFullHelp(sender, dropFirstStrings(args, 1));
            }
            else
            {
                throwCommand("worldutils.commands.error.unknowncommandargument", args[1]);
            }
        }
    }

    public static String[] dropFirstStrings(String[] input, int toDrop)
    {
        return CommandWorldUtils.dropFirstStrings(input, toDrop);
    }

    protected void sendMessage(ICommandSender sender, String message, Object... params)
    {
        CommandWorldUtils.sendMessage(sender, message, params);
    }

    public static void throwUsage(String message, Object... params) throws CommandException
    {
        CommandWorldUtils.throwUsage(message, params);
    }

    public static void throwNumber(String message, Object... params) throws CommandException
    {
        CommandWorldUtils.throwNumber(message, params);
    }

    public static void throwCommand(String message, Object... params) throws CommandException
    {
        CommandWorldUtils.throwCommand(message, params);
    }

    public static void sendClickableLinkMessage(ICommandSender sender, String messageKey, File file)
    {
        ITextComponent name = new TextComponentString(file.getName());

        if (WorldUtils.proxy.isSinglePlayer())
        {
            name.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            name.getStyle().setUnderlined(Boolean.valueOf(true));
        }

        sender.sendMessage(new TextComponentTranslation(messageKey, name));
    }

    protected void printBlockData(String blockStr, ICommandSender sender) throws CommandException
    {
        BlockData type = BlockData.parseBlockTypeFromString(blockStr);

        if (type != null && type.isValid())
        {
            this.sendMessage(sender, "worldutils.commands.blockreplace.block.print.valid", type.toString());
        }
        else
        {
            throwCommand("worldutils.commands.blockreplace.block.print.invalid", blockStr);
        }
    }

    protected int getDimension(String usage, String[] args, ICommandSender sender) throws CommandException
    {
        int dimension = sender instanceof EntityPlayer ? ((EntityPlayer) sender).getEntityWorld().provider.getDimension() : 0;

        if (args.length == 1)
        {
            dimension = CommandBase.parseInt(args[0]);
        }
        else if (args.length > 1)
        {
            throwUsage(usage);
        }

        return dimension;
    }
}
