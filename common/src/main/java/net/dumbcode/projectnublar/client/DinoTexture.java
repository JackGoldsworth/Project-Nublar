package net.dumbcode.projectnublar.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// NOTE: currently unreferenced by any live code (left over from the old skin-dyeing system);
// ported as-is to the 26.2 ReloadableTexture API in case it is revived.
public class DinoTexture extends ReloadableTexture {
    private static final Set<UUID> usedIds = new HashSet<>();
    private final ByteBuffer dataRef;

    public static DinoTexture create(Identifier name, ByteBuffer data) {
        return new DinoTexture(name, data);
    }

    private DinoTexture(Identifier location, ByteBuffer data) {
        super(location);
        this.dataRef = data;
    }

    public Identifier getLocation() {
        return this.resourceId();
    }

    public NativeImage asNative(){
        ByteBuffer data = this.dataRef;

        if (data == null)
            return null;

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer lwjglData = memoryStack.malloc(data.capacity());
            lwjglData.put(data);
            data.rewind();
            lwjglData.rewind();
            return NativeImage.read(lwjglData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 26.2: textures load via TextureContents instead of manually uploading in load(ResourceManager)
    @Override
    public TextureContents loadContents(ResourceManager manager) {
        NativeImage image = asNative();
        if (image == null) {
            return TextureContents.createMissing();
        }
        return new TextureContents(image, new TextureMetadataSection(
                TextureMetadataSection.DEFAULT_BLUR, TextureMetadataSection.DEFAULT_CLAMP,
                MipmapStrategy.AUTO, TextureMetadataSection.DEFAULT_ALPHA_CUTOFF_BIAS));
    }
}
