package net.dumbcode.projectnublar.client.renderer.state;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.HashMap;
import java.util.Map;

// Vanilla render states only gain GeckoLib's GeoRenderState duck interface via runtime mixins, so
// our renderers need an explicit subclass that implements it at compile time.
public class DinosaurRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> data = new HashMap<>();

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.data;
    }

    // GeckoLib's EntityRenderState mixin overrides addGeckolibData/hasGeckolibData to write a private
    // map inside EntityRenderState; our getDataMap() override would shadow it (writes to GL's map,
    // reads from ours) and every lookup returns null. Route writes/lookups through our map too.
    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
        this.data.put(dataTicket, data);
    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return this.data.containsKey(dataTicket);
    }
}
