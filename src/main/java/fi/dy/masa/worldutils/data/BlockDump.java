package fi.dy.masa.worldutils.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.worldutils.util.ModNameUtils;

public class BlockDump extends DataDump
{
    private BlockDump()
    {
        super(8);
    }

    protected List<String> getLines()
    {
        List<String> lines = new ArrayList<String>();

        this.generateFormatStrings();

        lines.add(this.lineSeparator);
        lines.add("*** WARNING ***");
        lines.add("The block and item IDs are dynamic and will be different on each world!");
        lines.add("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        lines.add("*** ALSO ***");
        lines.add("The server doesn't have a list of sub block and sub items");
        lines.add("(= items with different damage value or blocks with different metadata).");
        lines.add("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");
        lines.add("NOTE: The metadata value displayed is from the ItemStacks from getSubBlocks(), it's NOT necessarily the metadata value in world!!");
        lines.add("NOTE: For blocks, Subtypes = true is only based on the number of returned ItemStacks from getSubBlocks() being > 1");
        lines.add("NOTE: For blocks, Subtypes = ? means that Item.getItemFromBlock(block) returned null or the command was run on the server side");

        // Get the actual data
        this.getFormattedData(lines);

        return lines;
    }

    public void addData(Block block, ResourceLocation rl, boolean subTypesKnown, boolean hasSubTypes, @Nonnull ItemStack stack)
    {
        String blockId = String.valueOf(Block.getIdFromBlock(block));
        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : block.getLocalizedName();
        Item item = Item.getItemFromBlock(block);
        String itemId = item != Items.AIR ? String.format("%5d", Item.getIdFromItem(item)) : "-";
        String itemMeta = stack.isEmpty() ? "-" : String.format("%5d", stack.getMetadata());
        String subTypes = subTypesKnown ? String.valueOf(hasSubTypes) : "?";
        // FIXME 1.12 registry rewrite broke this
        //@SuppressWarnings("deprecation")
        String exists = "? FIXME ?"; //GameData.getBlockRegistry().isDummied(rl) ? "false" : "true";

        this.addData(modName, registryName, blockId, subTypes, itemId, itemMeta, displayName, exists);
    }

    public static List<String> getFormattedBlockDump()
    {
        BlockDump blockDump = new BlockDump();
        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            blockDump.addData(entry.getValue(), entry.getKey(), false, false, ItemStack.EMPTY);
        }

        blockDump.addTitle("Mod name", "Registry name", "BlockID", "Subtypes", "Item ID", "Item meta", "Display name", "Exists");

        blockDump.setColumnAlignment(2, Alignment.RIGHT); // ID
        blockDump.setColumnAlignment(3, Alignment.RIGHT); // sub-types
        blockDump.setColumnAlignment(4, Alignment.RIGHT); // item id
        blockDump.setColumnAlignment(5, Alignment.RIGHT); // item meta
        blockDump.setUseColumnSeparator(true);

        return blockDump.getLines();
    }
}
