package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.item.*;
import net.minecraft.world.item.Item;
import net.dumbcode.projectnublar.platform.DeferredItem;
import net.dumbcode.projectnublar.platform.DeferredRegister;

public class ItemInit {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MODID);

    public static final DeferredItem<Item> FOSSIL_ITEM = ITEMS.registerItem("fossil", FossilItem::new);
    public static final DeferredItem<Item> AMBER_ITEM = ITEMS.registerItem("amber", AmberItem::new);

    public static final DeferredItem<Item> DEV_STICK = ITEMS.registerItem("dev_stick", DebugStick::new);

    public static final DeferredItem<Item> TEST_TUBE_ITEM = ITEMS.registerItem("test_tube", TestTubeItem::new);

    public static final DeferredItem<Item> IRON_FILTER = ITEMS.registerItem("iron_filter", props -> new FilterItem(props.durability(100), 0.25));
    public static final DeferredItem<Item> GOLD_FILTER = ITEMS.registerItem("gold_filter", props -> new FilterItem(props.durability(100),0.5));
    public static final DeferredItem<Item> DIAMOND_FILTER = ITEMS.registerItem("diamond_filter", props -> new FilterItem(props.durability(100),1));
    public static final DeferredItem<Item> DEV_FILTER = ITEMS.registerItem("dev_filter", props -> new FilterItem(props,1));

    public static final DeferredItem<Item> IRON_TANK_UPGRADE = ITEMS.registerItem("iron_tank_upgrade", props -> new TankItem(props, 3000, 2000, 24, 128));
    public static final DeferredItem<Item> GOLD_TANK_UPGRADE = ITEMS.registerItem("gold_tank_upgrade", props -> new TankItem(props, 4000, 3000, 32, 192));
    public static final DeferredItem<Item> DIAMOND_TANK_UPGRADE = ITEMS.registerItem("diamond_tank_upgrade", props -> new TankItem(props,8000, 4000, 40, -1));

    public static final DeferredItem<Item> IRON_COMPUTER_CHIP = ITEMS.registerItem("iron_computer_chip", props -> new ComputerChipItem(props, 3*20*60, 7 * 20 * 60, 8 * 20 * 60));
    public static final DeferredItem<Item> GOLD_COMPUTER_CHIP = ITEMS.registerItem("gold_computer_chip", props -> new ComputerChipItem(props,2*20*60, 4 * 60 * 20, 6 * 20 * 60));
    public static final DeferredItem<Item> DIAMOND_COMPUTER_CHIP = ITEMS.registerItem("diamond_computer_chip", props -> new ComputerChipItem(props,20*60, -1, 4 * 20 * 60));
    public static final DeferredItem<Item> DEV_COMPUTER_CHIP = ITEMS.registerItem("dev_computer_chip", props -> new ComputerChipItem(props,10, 10, 10));

    public static final DeferredItem<Item> HARD_DRIVE = ITEMS.registerItem("hard_drive", props -> new DiskStorageItem(props, 10*20));
    public static final DeferredItem<Item> SSD = ITEMS.registerItem("ssd", props -> new DiskStorageItem(props, 5*20));
    public static final DeferredItem<Item> DEV_SSD = ITEMS.registerItem("dev_ssd", props -> new DiskStorageItem(props, 10));

    public static final DeferredItem<Item> SYRINGE = ITEMS.registerItem("syringe", SyringeItem::new);
    public static final DeferredItem<Item> SEQUENCER_DOOR = registerSingleItem("sequencer_door");
    public static final DeferredItem<Item> SEQUENCER_SCREEN = registerSingleItem("sequencer_monitor");
    public static final DeferredItem<Item> SEQUENCER_COMPUTER = registerSingleItem("sequencer_computer");

    public static final DeferredItem<Item> CRACKED_ARTIFICIAL_EGG = ITEMS.registerItem("cracked_artificial_egg", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> ARTIFICIAL_EGG = ITEMS.registerItem("artificial_egg", props -> new Item(props.stacksTo(1)));
    public static final DeferredItem<Item> UNINCUBATED_EGG = ITEMS.registerItem("unincubated_egg", UnincubatedEggItem::new);
    public static final DeferredItem<Item> SMALL_CONTAINER_UPGRADE = ITEMS.registerItem("small_container_upgrade", Item::new);
    public static final DeferredItem<Item> LARGE_CONTAINER_UPGRADE = ITEMS.registerItem("large_container_upgrade", Item::new);
    public static final DeferredItem<Item> WARM_BULB = ITEMS.registerItem("warm_bulb", props -> new BulbItem(props, 15*20));
    public static final DeferredItem<Item> WARMER_BULB = ITEMS.registerItem("warmer_bulb", props -> new BulbItem(props, 12*20));
    public static final DeferredItem<Item> HOT_BULB = ITEMS.registerItem("hot_bulb", props -> new BulbItem(props,9*20));
    public static final DeferredItem<Item> DEV_BULB = ITEMS.registerItem("dev_bulb", props -> new BulbItem(props,2));
    public static final DeferredItem<Item> IRON_PLANT_TANK = ITEMS.registerItem("iron_plant_tank_ugprade", props -> new PlantTankItem(props,128));
    public static final DeferredItem<Item> GOLD_PLANT_TANK = ITEMS.registerItem("gold_plant_tank_upgrade", props -> new PlantTankItem(props, 192));
    public static final DeferredItem<Item> INCUBATOR_NEST = ITEMS.registerItem("incubator_nest", Item::new);
    public static final DeferredItem<Item> INCUBATOR_LID = ITEMS.registerItem("incubator_lid", Item::new);
    public static final DeferredItem<Item> INCUBATOR_ARM_BASE = ITEMS.registerItem("incubator_arm_base", Item::new);
    public static final DeferredItem<Item> INCUBATOR_ARM = ITEMS.registerItem("incubator_arm", Item::new);
    public static final DeferredItem<Item> INCUBATED_EGG = ITEMS.registerItem("incubated_egg", IncubatedEggItem::new);
    public static final DeferredItem<Item> LEVELING_SENSOR = ITEMS.registerItem("leveling_sensor", Item::new);
    public static final DeferredItem<Item> WIRE_SPOOL = ITEMS.registerItem("wire_spool", Item::new);

    public static DeferredItem<Item> registerSingleItem(String name) {
        return ITEMS.registerItem(name, props -> new Item(props.stacksTo(1)));
    }

    public static void registerTo() {
        ITEMS.register();
    }
}
