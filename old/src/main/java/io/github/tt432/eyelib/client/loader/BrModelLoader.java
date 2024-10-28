package io.github.tt432.eyelib.client.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.tt432.eyelib.client.model.ModelMaterial;
import io.github.tt432.eyelib.client.model.bedrock.BrModel;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TT432
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
@Slf4j
public class BrModelLoader extends SimpleJsonResourceReloadListener {
    private static final BrModelLoader INSTANCE = new BrModelLoader();

    @SubscribeEvent
    public static void onEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(INSTANCE);
    }

    private final Map<ResourceLocation, BrModel> models = new HashMap<>();
    private final Map<ResourceLocation, ModelMaterial> materials = new HashMap<>();

    public static BrModel getModel(ResourceLocation location) {
        return INSTANCE.models.get(location);
    }

    public static ModelMaterial getMaterial(ResourceLocation location) {
        return INSTANCE.materials.get(location);
    }

    private BrModelLoader() {
        super(new Gson(), "bedrock_models");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        models.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation key = entry.getKey();

            try {
                if (key.getPath().endsWith("material")) {
                    materials.put(key, ModelMaterial.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow());
                } else {
                    models.put(key, BrModel.parse(key.toString(), entry.getValue().getAsJsonObject()));
                }
            } catch (Exception e) {
                log.error("can't load model {}", key, e);
            }
        }
    }
}
