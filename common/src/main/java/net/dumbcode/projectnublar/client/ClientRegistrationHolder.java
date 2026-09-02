package net.dumbcode.projectnublar.client;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.renderer.CarnivoreFeederRenderer;
import net.dumbcode.projectnublar.client.renderer.ElectricFenceRenderer;
import net.dumbcode.projectnublar.client.renderer.ElectricWireRenderer;
import net.dumbcode.projectnublar.client.renderer.IncubatorRenderer;
import net.dumbcode.projectnublar.client.renderer.ProcessorRenderer;
import net.dumbcode.projectnublar.client.renderer.SequencerRenderer;
import net.dumbcode.projectnublar.client.renderer.dinosaurs.*;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

public class ClientRegistrationHolder {

    public static void registerEntityRenderers() {
        EntityRenderers.register(EntityInit.DINOSAUR_PART.get(), NoopRenderer::new);
        EntityRenderers.register(EntityInit.TYRANNOSAURUS_REX.get(), (ctx) -> new TyrannosaurusRenderer(ctx, new DefaultedEntityGeoModel<>(Constants.modLoc("tyrannosaurus_rex"))));
        EntityRenderers.register(EntityInit.VELOCIRAPTOR.get(), (ctx) -> new VelociraptorRenderer(ctx, new DefaultedEntityGeoModel<>(Constants.modLoc("velociraptor"))));
        EntityRenderers.register(EntityInit.DILOPHOSAURUS.get(), (ctx) -> new DilophosaurusRenderer(ctx, new DefaultedEntityGeoModel<>(Constants.modLoc("dilophosaurus"))));
        EntityRenderers.register(EntityInit.TRICERATOPS.get(), (ctx) -> new TriceratopsRenderer(ctx, new DefaultedEntityGeoModel<>(Constants.modLoc("triceratops"))));
        EntityRenderers.register(EntityInit.BRACHIOSAURUS.get(), (ctx) -> new BrachiosaurusRenderer(ctx, new DefaultedEntityGeoModel<>(Constants.modLoc("brachiosaurus"))));
        EntityRenderers.register(EntityInit.GALLIMIMUS.get(), (ctx) -> new GallimimusRenderer(ctx, new DefaultedEntityGeoModel<>(Constants.modLoc("gallimimus"))));
    }

    // Menu screens are registered via RegisterMenuScreensEvent (MenuScreens.register is gone in 26.2);
    // see ClientModEvents.
    public static void registerTextures() {
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/tyrannosaurus_rex.png"), createRexTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/velociraptor.png"), createVelociraptorTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/dilophosaurus.png"), createDiloTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/triceratops.png"), createTrikeTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/brachiosaurus.png"), createBrachTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/gallimimus.png"), createGalliTexture());
    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(BlockInit.FEEDER_BLOCK_ENTITY.get(), (ctx) -> new CarnivoreFeederRenderer(ctx));
        BlockEntityRenderers.register(BlockInit.PROCESSOR_BLOCK_ENTITY.get(), (ctx) -> new ProcessorRenderer(ctx));
        BlockEntityRenderers.register(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), (ctx) -> new SequencerRenderer(ctx));
        BlockEntityRenderers.register(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (ctx) -> new GeoBlockRenderer<>(ctx, new DefaultedBlockGeoModel<>(Identifier.fromNamespaceAndPath(Constants.MODID, "egg_printer"))));
        BlockEntityRenderers.register(BlockInit.INCUBATOR_BLOCK_ENTITY.get(), IncubatorRenderer::new);
        BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(), ElectricFenceRenderer::new);
        BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(), (ctx) -> new ElectricWireRenderer());
    }

    // TODO(port 3.6): syringe "filled" item property (ItemProperties is gone in 26.2); needs a
    // conditional item model against DataComponentInit.DNA_DATA — tracked in ModBlockStateProvider.
    public static AbstractTexture createRexTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/tyrannosaurus_rex/female/tyrannosaurus_rex.png"));}
    public static AbstractTexture createVelociraptorTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/velociraptor/male/velociraptor.png"));}
    public static AbstractTexture createDiloTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/dilophosaurus/male/dilophosaurus.png"));}
    public static AbstractTexture createTrikeTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/triceratops/male/triceratops.png"));}
    public static AbstractTexture createBrachTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/brachiosaurus/male/brachiosaurus.png"));}
    public static AbstractTexture createGalliTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/gallimimus/male/gallimimus.png"));}
}
