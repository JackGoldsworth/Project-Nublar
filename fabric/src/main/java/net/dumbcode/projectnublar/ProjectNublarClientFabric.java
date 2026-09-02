package net.dumbcode.projectnublar;

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
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemModels;

public class ProjectNublarClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Item model codecs (ItemModels.ID_MAPPER is opened by fabric's transitive access widener)
        ItemModels.ID_MAPPER.put(Constants.modLoc("fossil"), FossilItemModel.Unbaked.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Constants.modLoc("test_tube"), TestTubeItemModel.Unbaked.MAP_CODEC);

        ClientRegistrationHolder.registerEntityRenderers();
        ClientRegistrationHolder.registerBlockEntityRenderers();

        // MenuScreens.register is private in 26.2 vanilla and opened by fabric's transitive AW
        MenuScreens.register(MenuTypeInit.PROCESSOR.get(), ProcessorScreen::new);
        MenuScreens.register(MenuTypeInit.SEQUENCER.get(), SequencerScreen::new);
        MenuScreens.register(MenuTypeInit.EGG_PRINTER.get(), EggPrinterScreen::new);
        MenuScreens.register(MenuTypeInit.INCUBATOR.get(), IncubatorScreen::new);
        MenuScreens.register(MenuTypeInit.GENERATOR_MENU.get(), GeneratorScreen::new);

        // 26.2: texture upload asserts on the render thread; client init may run elsewhere
        Minecraft.getInstance().execute(ClientRegistrationHolder::registerTextures);
        CommonClientClass.initClient();
    }
}
