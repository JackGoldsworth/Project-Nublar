package net.dumbcode.projectnublar.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.function.Supplier;

// Loader-neutral stand-in for NeoForge's DeferredHolder. get()/getId() mirror the
// NeoForge API. NOTE: this cannot implement Holder<T> — vanilla 26.2 Holder is a
// sealed interface (NeoForge unseals it, fabric/vanilla reject it), so holder()
// exposes the real registry holder for call sites that need one.
// get()/holder() throw if called before the entry is actually registered.
public class DeferredHolder<R, T extends R> implements Supplier<T> {

    private final ResourceKey<R> key;
    private Supplier<T> delegate;
    private Holder<T> holderDelegate;

    public DeferredHolder(ResourceKey<R> key) {
        this.key = key;
        this.delegate = () -> {
            throw new IllegalStateException("Registry object used before registration: " + key.identifier());
        };
    }

    public Identifier getId() {
        return this.key.identifier();
    }

    @SuppressWarnings("unchecked")
    public ResourceKey<T> getKey() {
        return (ResourceKey<T>) this.key;
    }

    @Override
    public T get() {
        return this.delegate.get();
    }

    public void setDelegate(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    public void bind(T value, Holder<T> holder) {
        this.delegate = () -> value;
        this.holderDelegate = holder;
    }

    public void setHolderDelegate(Holder<T> holderDelegate) {
        this.holderDelegate = holderDelegate;
    }

    public Holder<T> holder() {
        Holder<T> h = this.holderDelegate;
        if (h == null) {
            throw new IllegalStateException("Registry holder used before registration: " + this.key.identifier());
        }
        return h;
    }

    public boolean isBound() {
        return this.holderDelegate != null && this.holderDelegate.isBound();
    }

    @SuppressWarnings("unchecked")
    public Optional<ResourceKey<T>> unwrapKey() {
        return this.holderDelegate != null ? this.holderDelegate.unwrapKey() : Optional.of((ResourceKey<T>) this.key);
    }
}
