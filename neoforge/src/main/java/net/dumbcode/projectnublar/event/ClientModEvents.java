package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.ClientRegistrationHolder;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.model.fossil.FossilItemModel;
import net.dumbcode.projectnublar.client.model.testtube.TestTubeItemModel;
import net.dumbcode.projectnublar.client.screen.EggPrinterScreen;
import net.dumbcode.projectnublar.client.screen.GeneratorScreen;
import net.dumbcode.projectnublar.client.screen.IncubatorScreen;
import net.dumbcode.projectnublar.client.screen.ProcessorScreen;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(Constants.modLoc("fossil"), FossilItemModel.Unbaked.MAP_CODEC);
        event.register(Constants.modLoc("test_tube"), TestTubeItemModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientRegistrationHolder.registerEntityRenderers();
        ClientRegistrationHolder.registerBlockEntityRenderers();
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuTypeInit.PROCESSOR.get(), ProcessorScreen::new);
        event.register(MenuTypeInit.SEQUENCER.get(), SequencerScreen::new);
        event.register(MenuTypeInit.EGG_PRINTER.get(), EggPrinterScreen::new);
        event.register(MenuTypeInit.INCUBATOR.get(), IncubatorScreen::new);
        event.register(MenuTypeInit.GENERATOR_MENU.get(), GeneratorScreen::new);
    }

    @SubscribeEvent
    public static void onFMLClient(FMLClientSetupEvent event) {
        // 26.2: texture upload asserts on the render thread — the deferred-work queue is a
        // worker thread, so hop onto the render thread via Minecraft#execute
        event.enqueueWork(() -> Minecraft.getInstance().execute(ClientRegistrationHolder::registerTextures));
        CommonClientClass.initClient();
    }
}
