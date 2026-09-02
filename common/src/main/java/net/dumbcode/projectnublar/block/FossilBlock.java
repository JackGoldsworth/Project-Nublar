package net.dumbcode.projectnublar.block;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.Quality;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;

public class FossilBlock extends DropExperienceBlock {

    final Identifier entityType;
    final FossilPiece fossilPiece;
    final Quality quality;
    final Block base;

    public FossilBlock(Properties properties, Identifier pEntityType,FossilPiece pPiece, Quality pQuality, Block pBase) {
        super(ConstantInt.ZERO, properties);
        this.entityType = pEntityType;
        this.fossilPiece = pPiece;
        this.quality = pQuality;
        this.base = pBase;
    }

    public FossilPiece getFossilPiece() {
        return fossilPiece;
    }
    public Identifier getEntityType() {
        return entityType;
    }
    public Quality getQuality() {
        return quality;
    }
    public Block getBase() {
        return base;
    }
}
