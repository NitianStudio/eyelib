package io.github.tt432.eyelib.capability.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tt432.eyelib.client.animation.Animation;
import io.github.tt432.eyelib.client.animation.AnimationSet;
import io.github.tt432.eyelib.client.animation.bedrock.BrAnimation;
import io.github.tt432.eyelib.client.animation.bedrock.controller.BrAnimationControllers;
import io.github.tt432.eyelib.client.loader.BrAnimationControllerLoader;
import io.github.tt432.eyelib.client.loader.BrAnimationLoader;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TT432
 */
@Nullable
@Getter
public class AnimationComponent {
    public record SerializableInfo(
            ResourceLocation animationControllers,
            ResourceLocation targetAnimations
    ) {
        public static final Codec<SerializableInfo> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                ResourceLocation.CODEC.fieldOf("animationControllers").forGetter(o -> o.animationControllers),
                ResourceLocation.CODEC.fieldOf("targetAnimations").forGetter(o -> o.targetAnimations)
        ).apply(ins, SerializableInfo::new));

        public static final StreamCodec<ByteBuf, SerializableInfo> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                SerializableInfo::animationControllers,
                ResourceLocation.STREAM_CODEC,
                SerializableInfo::targetAnimations,
                AnimationComponent.SerializableInfo::new
        );
    }

    SerializableInfo serializableInfo;
    AnimationSet animationSet = AnimationSet.EMPTY;
    Map<String, Animation<?>> animations = new HashMap<>();
    Map<String, Object> animationData = new HashMap<>();

    public Object getAnimationData(String controllerName) {
        return animationData.computeIfAbsent(controllerName, s -> animations.get(s).createData());
    }

    public boolean serializable() {
        return serializableInfo != null
                && serializableInfo.animationControllers != null
                && serializableInfo.targetAnimations != null;
    }

    public void setup(ResourceLocation animationControllersName, ResourceLocation targetAnimationsName) {
        if (animationControllersName == null || targetAnimationsName == null) return;

        BrAnimationControllers animationControllers = BrAnimationControllerLoader.getController(animationControllersName);
        BrAnimation targetAnimations = BrAnimationLoader.getAnimation(targetAnimationsName);

        if (animationControllers == null || targetAnimations == null) return;

        if (serializableInfo != null
                && animationControllersName.equals(serializableInfo.animationControllers)
                && targetAnimationsName.equals(serializableInfo.targetAnimations)) return;

        serializableInfo = new SerializableInfo(animationControllersName, targetAnimationsName);

        this.animations = new HashMap<>(animationControllers.animationControllers());
        this.animationSet = AnimationSet.from(targetAnimations);

        animationData = new HashMap<>();
        for (var s : animations.values()) {
            var data = s.createData();
            animationData.put(s.name(), data);
        }
    }
}
