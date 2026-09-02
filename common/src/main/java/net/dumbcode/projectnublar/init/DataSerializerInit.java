package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.platform.DeferredRegister;
import net.dumbcode.projectnublar.platform.Services;

import java.util.Optional;
import java.util.UUID;

// 26.2: modded EntityDataSerializers must go through NeoForgeRegistries.ENTITY_DATA_SERIALIZERS
// (vanilla EntityDataSerializers.registerSerializer now throws). The serializer instances are
// created eagerly so defineId call sites can reference them before registry events run.
public class DataSerializerInit {

    //npc_data
    public static final StreamCodec<ByteBuf, DinoData> DINO_DATA_STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(DinoData::fromNBT, DinoData::toNBT);

    public static final EntityDataSerializer<DinoData> DINO_DATA = EntityDataSerializer.forValueType(DINO_DATA_STREAM_CODEC);
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));

    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Services.PLATFORM.entityDataSerializerRegistryKey(), Constants.MODID);

    static {
        SERIALIZERS.register("dino_data", () -> DINO_DATA);
        SERIALIZERS.register("compound_tag", () -> COMPOUND_TAG);
        SERIALIZERS.register("optional_uuid", () -> OPTIONAL_UUID);
    }
}
