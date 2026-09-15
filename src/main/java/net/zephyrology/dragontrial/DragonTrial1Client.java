package net.zephyrology.dragontrial;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.zephyrology.dragontrial.entity.ModEntities;
import net.zephyrology.dragontrial.entity.client.DragonRenderer;

public class DragonTrial1Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DRAGON, DragonRenderer::new);
    }
}
