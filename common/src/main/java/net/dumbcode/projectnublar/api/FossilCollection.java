package net.dumbcode.projectnublar.api;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.AmberBlock;
import net.dumbcode.projectnublar.block.FossilBlock;
import net.dumbcode.projectnublar.init.BlockInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.dumbcode.projectnublar.platform.DeferredHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record FossilCollection(Map<Block,Map<Quality,Map<FossilPiece, DeferredHolder<Block, ?>>>> fossilblocks, Map<Block,DeferredHolder<Block, ?>> amberBlocks) {
    //store collections for use
    public static Map<String,FossilCollection> COLLECTIONS = new HashMap<>();
    //overload for PN Entities
    public static FossilCollection create(String fossilName) {
        return create(Constants.modLoc(fossilName));
   }
    //register a fossil collection based off an EntityType
    public static FossilCollection create(Identifier entityType) {
        Map<Block,Map<Quality,Map<FossilPiece,DeferredHolder<Block, ?>>>> fullFossilMap = new HashMap<>();
        Map<Block,DeferredHolder<Block, ?>> fullAmberMap = new HashMap<>();
        for (Block stone : stonelist) {
            String stoneName = BuiltInRegistries.BLOCK.getKey(stone).getPath();
            Map<Quality,Map<FossilPiece,DeferredHolder<Block, ?>>> qualityMap2 = new HashMap<>();
            for (Quality quality : Quality.values()) {
                String qualityName = quality == Quality.NONE ? "" : quality.getName().toLowerCase() + "_";
                Map<FossilPiece,DeferredHolder<Block, ?>> stoneMap2 = new HashMap<>();
                for (FossilPiece piece : FossilPieces.getPiecesByEntityType(entityType)) {
                    String name = qualityName + stoneName + "_" + entityType.getPath() + "_" + piece.name().toLowerCase() +"_fossil";
                    stoneMap2.put(piece, BlockInit.registerBlock(name, () -> new FossilBlock(BlockBehaviour.Properties.ofFullCopy(stone).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, Constants.modLoc(name))), entityType, piece, quality,stone)));
                }
                qualityMap2.put(quality,stoneMap2);
            }
            fullFossilMap.put(stone,qualityMap2);
            String amberName = stoneName + "_" + entityType.getPath() + "_amber";
            fullAmberMap.put(stone,BlockInit.registerBlock(amberName, () -> new AmberBlock(BlockBehaviour.Properties.ofFullCopy(stone).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, Constants.modLoc(amberName))), entityType,stone)));
        }
        return COLLECTIONS.put(entityType.toString(),new FossilCollection(fullFossilMap, fullAmberMap));
    }

    public static List<Block> stonelist = List.of(
            Blocks.STONE,
            Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE, Blocks.SANDSTONE, Blocks.DEEPSLATE,
            Blocks.TERRACOTTA, Blocks.DYED_TERRACOTTA.pick(DyeColor.RED), Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW), Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN), Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY)
    );
}
