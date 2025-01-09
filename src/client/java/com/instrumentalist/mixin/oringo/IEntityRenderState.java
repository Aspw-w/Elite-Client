package com.instrumentalist.mixin.oringo;

import net.minecraft.entity.Entity;

public interface IEntityRenderState {
    Entity client$getEntity();

    void client$setEntity(Entity entity);
}