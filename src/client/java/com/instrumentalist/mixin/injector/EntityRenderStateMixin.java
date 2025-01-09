package com.instrumentalist.mixin.injector;

import com.instrumentalist.mixin.oringo.IEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements IEntityRenderState {
    @Unique
    private Entity entity;

    @Override
    public Entity client$getEntity() {
        return entity;
    }

    @Override
    public void client$setEntity(Entity entity) {
        this.entity = entity;
    }
}