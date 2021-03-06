package fi.dy.masa.worldutils.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.worldutils.WorldUtils;

public class BlockData
{
    private DataType type;
    private int id;
    private int meta;
    private int blockStateId;
    private int[] blockStateIds = new int[0];
    private String name = "";
    private List<Pair<String, String>> props = new ArrayList<Pair<String, String>>();

    public BlockData(int id)
    {
        this.type = DataType.ID;
        this.id = id;
        this.setNumericValues();
    }

    public BlockData(int id, int meta)
    {
        this.type = DataType.ID_META;
        this.id = id;
        this.meta = meta;
        this.setNumericValues();
    }

    public BlockData(String name)
    {
        this.type = DataType.NAME;
        this.name = name;
        this.setNumericValues();
    }

    public BlockData(String name, int meta)
    {
        this.type = DataType.NAME_META;
        this.name = name;
        this.meta = meta;
        this.setNumericValues();
    }

    public BlockData(String name, List<Pair<String, String>> props)
    {
        this.type = DataType.NAME_PROPS;
        this.name = name;
        this.props.addAll(props);
        this.setNumericValues();
    }

    public DataType getType()
    {
        return this.type;
    }

    public String getName()
    {
        return this.name;
    }

    public int getId()
    {
        return this.id;
    }

    public int getMeta()
    {
        return this.meta;
    }

    public int getBlockStateId()
    {
        return this.blockStateId;
    }

    public int[] getBlockStateIds()
    {
        return this.blockStateIds;
    }

    public boolean ignoreMeta()
    {
        return this.type == DataType.ID || this.type == DataType.NAME;
    }

    public boolean isValid()
    {
        final boolean neid = WorldUtils.isModLoadedNEID();
        final int maxId = (neid ? (1 << 16) : (1 << 12)) - 1;

        switch (this.type)
        {
            case ID:
                return this.id >= 0 && this.id <= maxId;

            case ID_META:
                return this.id >= 0 && this.id <= maxId && this.meta >= 0 && this.meta < 16;

            case NAME:
                return Block.REGISTRY.containsKey(new ResourceLocation(this.name));

            case NAME_META:
                return Block.REGISTRY.containsKey(new ResourceLocation(this.name)) && this.meta >= 0 && this.meta < 16;

            case NAME_PROPS:
                if (Block.REGISTRY.containsKey(new ResourceLocation(this.name)) == false)
                {
                    return false;
                }

                Block block = Block.REGISTRY.getObject(new ResourceLocation(this.name));

                for (Pair<String, String> pair : this.props)
                {
                    IProperty<?> prop = block.getBlockState().getProperty(pair.getLeft());

                    if (prop == null || prop.parseValue(pair.getRight()).isPresent() == false)
                    {
                        return false;
                    }
                }

                return true;
        }

        return false;
    }

    private void setNumericValues()
    {
        if (this.isValid())
        {
            switch (this.type)
            {
                case NAME:
                    this.id = Block.getIdFromBlock(Block.REGISTRY.getObject(new ResourceLocation(this.name)));
                    break;

                case NAME_META:
                    this.id = Block.getIdFromBlock(Block.REGISTRY.getObject(new ResourceLocation(this.name)));
                    break;

                case NAME_PROPS:
                    Block block = Block.REGISTRY.getObject(new ResourceLocation(this.name));
                    IBlockState state = block.getDefaultState();

                    for (Pair<String, String> pair : this.props)
                    {
                        IProperty<?> prop = block.getBlockState().getProperty(pair.getLeft());

                        if (prop != null)
                        {
                            state = setPropertyValueFromString(state, prop, pair.getRight());
                        }
                    }

                    final boolean neid = WorldUtils.isModLoadedNEID();
                    final int metaShift = neid ? 16 : 12;
                    final int mask = neid ? 0xFFFF : 0xFFF;
                    int stateId = Block.getStateId(state);
                    this.id = stateId & mask;
                    this.meta = (stateId >> metaShift) & 0xF;

                    break;

                default:
            }

            this.setBlockstateIds();
        }
    }

    private void setBlockstateIds()
    {
        if (this.isValid())
        {
            final boolean neid = WorldUtils.isModLoadedNEID();
            final int metaShift = neid ? 16 : 12;

            this.blockStateId = (this.meta << metaShift) | this.id;

            if (this.ignoreMeta())
            {
                int[] ids = new int[16];

                for (int i = 0; i < 16; i++)
                {
                    ids[i] = (i << metaShift) | this.id;
                }

                this.blockStateIds = ids;
            }
            else
            {
                this.blockStateIds = new int[] { this.blockStateId };
            }
        }
    }

    @Override
    public String toString()
    {
        if (this.type == DataType.ID)
        {
            return "BlockData:{ type=" + this.type + ", id=" + this.id + " }";
        }
        else if (this.type == DataType.ID_META)
        {
            return "BlockData:{ type=" + this.type + ", id=" + this.id + ", meta=" + this.meta + " }";
        }
        else if (this.type == DataType.NAME)
        {
            return "BlockData:{ type=" + this.type + ", name=" + this.name + ", id=" + this.id + " }";
        }
        else if (this.type == DataType.NAME_META)
        {
            return "BlockData:{ type=" + this.type + ", name=" + this.name + ", meta=" + this.meta + ", id=" + this.id + " }";
        }
        else if (this.type == DataType.NAME_PROPS)
        {
            StringBuilder propStr = new StringBuilder(128);
            for (Pair<String, String> pair : this.props) { propStr.append(pair.getLeft()).append("=").append(pair.getRight()).append(","); }
            if (propStr.length() > 0) { propStr.deleteCharAt(propStr.length() - 1); }
            return "BlockData:{ type=" + this.type + ", name=" + this.name +
                    ", props=[" + propStr.toString() + "], id=" + this.id + ", meta=" + this.meta + " }";
        }
        else
        {
            return "BlockData:{type=INVALID}";
        }
    }

    public static BlockData parseBlockTypeFromString(String str)
    {
        try
        {
            Pattern patternId        = Pattern.compile("(?<id>[0-9]+)");
            Pattern patternIdMeta    = Pattern.compile("(?<id>[0-9]+)[@:]{1}(?<meta>[0-9]+)");
            Pattern patternName      = Pattern.compile("(?<name>([a-z0-9_]+:)?[a-z0-9\\._ ]+)");
            Pattern patternNameMeta  = Pattern.compile("(?<name>([a-z0-9_]+:)?[a-z0-9\\._ ]+)[@:]{1}(?<meta>[0-9]+)");
            Pattern patternNameProps = Pattern.compile("(?<name>([a-z0-9_]+:)?[a-z0-9\\._ ]+)\\[(?<props>[a-z0-9_]+=[a-z0-9_]+(,[a-z0-9_]+=[a-z0-9_]+)*)\\]");

            Matcher matcherId = patternId.matcher(str);
            if (matcherId.matches())
            {
                //System.out.printf("Type.ID - id: %d\n", Integer.parseInt(matcherId.group("id")));
                return new BlockData(Integer.parseInt(matcherId.group("id")));
            }

            Matcher matcherIdMeta = patternIdMeta.matcher(str);
            if (matcherIdMeta.matches())
            {
                // id@meta
                //System.out.printf("Type.ID_META - id: %d, meta: %d\n",
                //        Integer.parseInt(matcherIdMeta.group("id")), Integer.parseInt(matcherIdMeta.group("meta")));
                return new BlockData(Integer.parseInt(matcherIdMeta.group("id")), Integer.parseInt(matcherIdMeta.group("meta")));
            }

            Matcher matcherName = patternName.matcher(str);
            if (matcherName.matches())
            {
                //System.out.printf("Type.NAME - name: %s\n", matcherName.group("name"));
                return new BlockData(matcherName.group("name"));
            }

            Matcher matcherNameMeta = patternNameMeta.matcher(str);
            if (matcherNameMeta.matches())
            {
                // name@meta
                //System.out.printf("Type.NAME_META - name: %s, meta: %d\n",
                //        matcherNameMeta.group("name"), Integer.parseInt(matcherNameMeta.group("meta")));
                return new BlockData(matcherNameMeta.group("name"), Integer.parseInt(matcherNameMeta.group("meta")));
            }

            Matcher matcherNameProps = patternNameProps.matcher(str);
            if (matcherNameProps.matches())
            {
                // name[props]
                String name = matcherNameProps.group("name");
                String propStr = matcherNameProps.group("props");
                String[] propParts = propStr.split(",");
                Pattern patternProp = Pattern.compile("(?<prop>[a-zA-Z0-9\\._-]+)=(?<value>[a-zA-Z0-9\\._-]+)");
                List<Pair<String, String>> props = new ArrayList<Pair<String, String>>();

                for (int i = 0; i < propParts.length; i++)
                {
                    Matcher matcherProp = patternProp.matcher(propParts[i]);

                    if (matcherProp.matches())
                    {
                        props.add(Pair.of(matcherProp.group("prop"), matcherProp.group("value")));
                    }
                    else
                    {
                        WorldUtils.logger.warn("Invalid block property '{}'", propParts[i]);
                    }
                }

                Collections.sort(props); // the properties need to be in alphabetical order

                //System.out.printf("Type.NAME_PROPS - name: %s, props: %s (propStr: %s)\n", name, String.join(",", props), propStr);
                return new BlockData(name, props);
            }
        }
        catch (PatternSyntaxException e)
        {
            WorldUtils.logger.warn("Invalid regex pattern in parseBlockTypeFromString()", e);
        }
        catch (Exception e)
        {
            WorldUtils.logger.warn("Failed to parse block type in parseBlockTypeFromString()", e);
        }

        return null;
    }

    public static <T extends Comparable<T>> IBlockState setPropertyValueFromString(IBlockState state, IProperty<T> prop, String valueStr)
    {
        return state.withProperty(prop, prop.parseValue(valueStr).get());
    }

    public enum DataType
    {
        ID,
        ID_META,
        NAME,
        NAME_META,
        NAME_PROPS;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + meta;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlockData other = (BlockData) obj;
        if (id != other.id)
            return false;
        if (meta != other.meta)
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
