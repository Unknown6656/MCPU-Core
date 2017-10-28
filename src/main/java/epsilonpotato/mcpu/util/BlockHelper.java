
package epsilonpotato.mcpu.util;


import org.bukkit.block.Block;


public final class BlockHelper
{
    private BlockHelper()
    {   
    }
    
    public static final boolean isOpaque(Block block)
    {
        switch (block.getType())
        {
            case ACACIA_STAIRS:
            case BEDROCK:
            case BEETROOT_BLOCK:
            case BIRCH_WOOD_STAIRS:
            case BLACK_GLAZED_TERRACOTTA:
            case BLUE_GLAZED_TERRACOTTA:
            case BONE_BLOCK:
            case BOOKSHELF:
            case BRICK:
            case BRICK_STAIRS:
            case BROWN_GLAZED_TERRACOTTA:
            case BURNING_FURNACE:
            case CLAY:
            case CLAY_BRICK:
            case COAL_BLOCK:
            case COAL_ORE:
            case COBBLESTONE:
            case COBBLESTONE_STAIRS:
            case CONCRETE:
            case CONCRETE_POWDER:
            case CYAN_GLAZED_TERRACOTTA:
            case DIAMOND_BLOCK:
            case DIAMOND_ORE:
            case DIRT:
            case DISPENSER:
            case DROPPER:
            case EGG:
            case EMERALD_BLOCK:
            case EMERALD_ORE:
            case ENCHANTMENT_TABLE:
            case ENDER_STONE:
            case END_BRICKS:
            case FURNACE:
            case GLOWING_REDSTONE_ORE:
            case GLOWSTONE:
            case GOLD_BLOCK:
            case GOLD_ORE:
            case GRASS:
            case GRAVEL:
            case GRAY_GLAZED_TERRACOTTA:
            case GREEN_GLAZED_TERRACOTTA:
            case HARD_CLAY:
            case HAY_BLOCK:
            case HOPPER:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case IRON_BLOCK:
            case IRON_ORE:
            case JACK_O_LANTERN:
            case JUNGLE_WOOD_STAIRS:
            case LAPIS_BLOCK:
            case LAPIS_ORE:
            case LIGHT_BLUE_GLAZED_TERRACOTTA:
            case LIME_GLAZED_TERRACOTTA:
            case LOG:
            case LOG_2:
            case MAGENTA_GLAZED_TERRACOTTA:
            case MELON_BLOCK:
            case MOSSY_COBBLESTONE:
            case MYCEL:
            case NETHERRACK:
            case NETHER_BRICK:
            case NETHER_BRICK_ITEM:
            case NETHER_BRICK_STAIRS:
            case NETHER_WART_BLOCK:
            case OBSIDIAN:
            case ORANGE_GLAZED_TERRACOTTA:
            case PINK_GLAZED_TERRACOTTA:
            case PISTON_BASE:
            case PISTON_EXTENSION:
            case PISTON_MOVING_PIECE:
            case PISTON_STICKY_BASE:
            case PRISMARINE:
            case PRISMARINE_CRYSTALS:
            case PUMPKIN:
            case PURPLE_GLAZED_TERRACOTTA:
            case PURPUR_BLOCK:
            case PURPUR_DOUBLE_SLAB:
            case PURPUR_PILLAR:
            case PURPUR_SLAB:
            case PURPUR_STAIRS:
            case QUARTZ:
            case QUARTZ_BLOCK:
            case QUARTZ_ORE:
            case QUARTZ_STAIRS:
            case REDSTONE_BLOCK:
            case REDSTONE_ORE:
            case RED_NETHER_BRICK:
            case RED_SANDSTONE:
            case RED_SANDSTONE_STAIRS:
            case ROTTEN_FLESH:
            case SADDLE:
            case SAND:
            case SANDSTONE:
            case SANDSTONE_STAIRS:
            case SEA_LANTERN:
            case SILVER_GLAZED_TERRACOTTA:
            case SMOOTH_BRICK:
            case SMOOTH_STAIRS:
            case SOIL:
            case SPRUCE_WOOD_STAIRS:
            case STAINED_CLAY:
            case STEP:
            case STONE:
            case STONE_SLAB2:
            case SULPHUR:
            case TNT:
            case WHITE_GLAZED_TERRACOTTA:
            case WOOD:
            case WOOD_DOUBLE_STEP:
            case WOOD_STAIRS:
            case WOOD_STEP:
            case WOOL:
            case WORKBENCH:
            case YELLOW_GLAZED_TERRACOTTA:
            	return true;
            default:
            	return false;
        }
    }
}
