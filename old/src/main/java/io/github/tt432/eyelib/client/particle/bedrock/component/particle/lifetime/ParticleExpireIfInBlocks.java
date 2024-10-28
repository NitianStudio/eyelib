package io.github.tt432.eyelib.client.particle.bedrock.component.particle.lifetime;

import com.mojang.serialization.Codec;
import io.github.tt432.eyelib.client.particle.bedrock.BrParticleParticle;
import io.github.tt432.eyelib.client.particle.bedrock.component.ComponentTarget;
import io.github.tt432.eyelib.client.particle.bedrock.component.RegisterParticleComponent;
import io.github.tt432.eyelib.client.particle.bedrock.component.particle.ParticleParticleComponent;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

/**
 * @author TT432
 */
@RegisterParticleComponent(value = "particle_expire_if_in_blocks", target = ComponentTarget.PARTICLE)
public record ParticleExpireIfInBlocks(
        List<String> blocks
) implements ParticleParticleComponent {
    public static final Codec<ParticleExpireIfInBlocks> CODEC =
            Codec.STRING.listOf().xmap(ParticleExpireIfInBlocks::new, ParticleExpireIfInBlocks::blocks);

    @Override
    public void onFrame(BrParticleParticle particle) {
        if (blocks.contains(BuiltInRegistries.BLOCK.getKey(particle.level()
                .getBlockState(particle.getBlockPosition())
                .getBlock()).toString())) {
            particle.remove();
        }
    }
}
