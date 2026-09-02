package net.dumbcode.projectnublar.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// Loader-neutral stand-in for NeoForge's DeferredRegister. Mirrors the API surface
// this mod used (create/createItems/createBlocks, register, registerItem,
// registerBlock, registerSimpleBlockItem, makeRegistry) so init classes only need
// their imports re-pointed. Behaviour per loader lives in IPlatformHelper.
public class DeferredRegister<T> {

    public record Entry(String name, Supplier<?> supplier, DeferredHolder<?, ?> holder) {
        @SuppressWarnings("unchecked")
        public <X> void bind(X value, net.minecraft.core.Holder<X> holder) {
            ((DeferredHolder<Object, X>) this.holder).bind(value, holder);
        }
    }

    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final String modid;
    private final List<Entry> entries = new ArrayList<>();

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        this.registryKey = registryKey;
        this.modid = modid;
        Services.PLATFORM.prepareRegister(this);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new DeferredRegister<>(registryKey, modid);
    }

    public static DeferredRegister.Items createItems(String modid) {
        return new DeferredRegister.Items(modid);
    }

    public static DeferredRegister.Blocks createBlocks(String modid) {
        return new DeferredRegister.Blocks(modid);
    }

    public ResourceKey<? extends Registry<T>> registryKey() {
        return this.registryKey;
    }

    public String modid() {
        return this.modid;
    }

    public List<Entry> entries() {
        return this.entries;
    }

    protected Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(this.modid, name);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        DeferredHolder<T, I> holder = this.createHolder(this.id(name));
        Services.PLATFORM.registerEntry(this, name, supplier, holder);
        this.entries.add(new Entry(name, supplier, holder));
        return holder;
    }

    protected <I extends T> DeferredHolder<T, I> createHolder(Identifier id) {
        return new DeferredHolder<>(ResourceKey.create(this.registryKey, id));
    }

    @SuppressWarnings("unchecked")
    public java.util.Collection<DeferredHolder<T, ? extends T>> getEntries() {
        java.util.List<DeferredHolder<T, ? extends T>> out = new java.util.ArrayList<>();
        for (Entry entry : this.entries) {
            out.add((DeferredHolder<T, ? extends T>) entry.holder());
        }
        return out;
    }

    /** Flushes entries into the real registries (registerTo() replacement for register(IEventBus)). */
    public void register() {
        Services.PLATFORM.bindRegister(this);
    }

    /** Creates the custom registry object for this register's key (modded registries only). */
    public Registry<T> makeRegistry() {
        return Services.PLATFORM.makeRegistry(this);
    }

    public static class Items extends DeferredRegister<Item> {

        protected Items(String modid) {
            super(Registries.ITEM, modid);
        }

        @Override
        protected <I extends Item> DeferredHolder<Item, I> createHolder(Identifier id) {
            return new DeferredItem<>(ResourceKey.create(this.registryKey, id));
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            return (DeferredItem<I>) super.register(name, supplier);
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, I> factory) {
            Identifier id = this.id(name);
            return this.register(name, () -> factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block) {
            Identifier id = this.id(name);
            return this.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
        }
    }

    public static class Blocks extends DeferredRegister<Block> {

        protected Blocks(String modid) {
            super(Registries.BLOCK, modid);
        }

        @Override
        protected <I extends Block> DeferredHolder<Block, I> createHolder(Identifier id) {
            return new DeferredBlock<>(ResourceKey.create(this.registryKey, id));
        }

        @Override
        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier) {
            return (DeferredBlock<I>) super.register(name, supplier);
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, B> factory) {
            Identifier id = this.id(name);
            return this.register(name, () -> factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id))));
        }
    }
}
