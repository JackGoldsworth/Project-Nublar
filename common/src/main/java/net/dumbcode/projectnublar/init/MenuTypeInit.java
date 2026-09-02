package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.dumbcode.projectnublar.platform.DeferredHolder;
import net.dumbcode.projectnublar.platform.DeferredRegister;
import net.dumbcode.projectnublar.platform.Services;

public class MenuTypeInit {
    public static DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Constants.MODID);
    public static DeferredHolder<MenuType<?>, MenuType<ProcessorMenu>> PROCESSOR = MENU_TYPES.register("processor", () -> new MenuType<>(ProcessorMenu::new, FeatureFlags.VANILLA_SET));
    public static DeferredHolder<MenuType<?>, MenuType<SequencerMenu>> SEQUENCER = MENU_TYPES.register("sequencer", () -> Services.PLATFORM.createPosMenuType(SequencerMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<EggPrinterMenu>> EGG_PRINTER = MENU_TYPES.register("egg_printer", () -> new MenuType<>(EggPrinterMenu::new, FeatureFlags.VANILLA_SET));
    public static DeferredHolder<MenuType<?>, MenuType<IncubatorMenu>> INCUBATOR = MENU_TYPES.register("incubator", () -> Services.PLATFORM.createPosMenuType(IncubatorMenu::new));
    public static DeferredHolder<MenuType<?>, MenuType<GeneratorMenu>> GENERATOR_MENU = MENU_TYPES.register("generator", ()-> new MenuType<>(GeneratorMenu::new, FeatureFlags.VANILLA_SET));

    public static void registerTo() {
        MENU_TYPES.register();
    }
}
