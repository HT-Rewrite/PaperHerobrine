package me.pk2.pherob.util.bukkit;

import org.bukkit.entity.Entity;

public class RayTraceResultEntity {
    private final Entity entity;

    public RayTraceResultEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}