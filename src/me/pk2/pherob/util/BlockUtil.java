package me.pk2.pherob.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import me.pk2.pherob.util.bukkit.BoundingBox;
import me.pk2.pherob.util.bukkit.RayTraceResult;
import me.pk2.pherob.util.bukkit.RayTraceResultEntity;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class BlockUtil {
    public static List<Block> getBlocks(Block start, int radius){
        if (radius < 0) {
            return new ArrayList<>(0);
        }
        int iterations = (radius * 2) + 1;
        List<Block> blocks = new ArrayList<>(iterations * iterations * iterations);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(start.getRelative(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static float[] calculateYawPitch(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();

        double yaw = Math.atan2(-dx, dz) * 180 / Math.PI;
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        double pitch = Math.atan2(-dy, distanceXZ) * 180 / Math.PI;

        return new float[] {(float) yaw, (float) pitch};
    }

    public static BlockFace getDirection(Vector vector) {
        BlockFace dir = BlockFace.SELF;
        float minAngle = Float.MAX_VALUE;
        float angle;
        Vector oVec = new Vector();
        for (BlockFace tested : BlockFace.values()) {
            oVec.setX(tested.getModX());
            oVec.setY(tested.getModY());
            oVec.setZ(tested.getModZ());

            if (tested != BlockFace.SELF) {
                angle = vector.angle(oVec);
                if (!Float.isNaN(angle) && angle < minAngle) {
                    minAngle = angle;
                    dir = tested;
                }
            }
        }
        return dir;
    }

    public static RayTraceResult rayTraceEntities(CraftWorld craftWorld, Location start, Vector direction, double maxDistance) {
        return rayTraceEntities(craftWorld, start, direction, maxDistance, null);
    }

    public static RayTraceResult rayTraceEntities(CraftWorld craftWorld, Location start, Vector direction, double maxDistance, double raySize) {
        return rayTraceEntities(craftWorld, start, direction, maxDistance, raySize, null);
    }

    public static  RayTraceResult rayTraceEntities(CraftWorld craftWorld, Location start, Vector direction, double maxDistance, Predicate<? super Entity> filter) {
        return rayTraceEntities(craftWorld, start, direction, maxDistance, 0.0D, filter);
    }

    public static RayTraceResult rayTraceEntities(CraftWorld craftWorld, Location start, Vector direction, double maxDistance, double raySize, Predicate<? super Entity> filter) {
        Preconditions.checkArgument(start != null, "Location start cannot be null");
        Preconditions.checkArgument(craftWorld.getName().equals(start.getWorld().getName()), "Location start cannot be in a different world");
        locCheckFinite(start);

        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        vectorCheckFinite(direction);

        Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude (%s) need to be greater than 0", direction.lengthSquared());

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector startPos = start.toVector();
        Vector dir = direction.clone().normalize().multiply(maxDistance);
        BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
        Collection<Entity> entities = getNearbyEntities(craftWorld, aabb, filter);

        Entity nearestHitEntity = null;
        RayTraceResult nearestHitResult = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : entities) {
            AxisAlignedBB fuckedBB = ((CraftEntity)entity).getHandle().getBoundingBox();

            BoundingBox boundingBox = new BoundingBox(fuckedBB.a, fuckedBB.b, fuckedBB.c, fuckedBB.d, fuckedBB.e, fuckedBB.f).expand(raySize);
            RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);

            if (hitResult != null) {
                double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());

                if (distanceSq < nearestDistanceSq) {
                    nearestHitEntity = entity;
                    nearestHitResult = hitResult;
                    nearestDistanceSq = distanceSq;
                }
            }
        }

        return (nearestHitEntity == null) ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
    }

    public static Collection<Entity> getNearbyEntities(CraftWorld craftWorld, Location location, double x, double y, double z) {
        return getNearbyEntities(craftWorld, location, x, y, z, null);
    }

    public static Collection<Entity> getNearbyEntities(CraftWorld craftWorld, Location location, double x, double y, double z, Predicate<? super Entity> filter) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(craftWorld.getName().equals(location.getWorld().getName()), "Location cannot be in a different world");

        BoundingBox aabb = BoundingBox.of(location, x, y, z);
        return getNearbyEntities(craftWorld, aabb, filter);
    }

    public static Collection<Entity> getNearbyEntities(CraftWorld craftWorld, BoundingBox boundingBox) {
        return getNearbyEntities(craftWorld, boundingBox, null);
    }

    public static Collection<Entity> getNearbyEntities(CraftWorld craftWorld, BoundingBox boundingBox, Predicate<? super Entity> filter) {
        Preconditions.checkArgument(boundingBox != null, "BoundingBox cannot be null");

        AxisAlignedBB bb = new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        List<net.minecraft.server.v1_8_R3.Entity> entityList = craftWorld.getHandle().getEntities(null, bb);
        List<Entity> bukkitEntityList = new ArrayList<>(entityList.size());

        for (net.minecraft.server.v1_8_R3.Entity entity : entityList) {
            CraftEntity bukkitEntity = entity.getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity))
                bukkitEntityList.add(bukkitEntity);
        }

        return bukkitEntityList;
    }

    public static Vector vectorNormalizeZeros(final Vector v) {
        if(v.getX() == -0.0D)
            v.setX(0.0D);
        if(v.getY() == -0.0D)
            v.setY(0.0D);
        if(v.getZ() == -0.0D)
            v.setZ(0.0D);
        return v;
    }

    public static void vectorCheckFinite(Vector v) throws IllegalArgumentException {
        NumberConversions.checkFinite(v.getX(), "x not finite");
        NumberConversions.checkFinite(v.getY(), "y not finite");
        NumberConversions.checkFinite(v.getZ(), "z not finite");
    }

    public static Vector bfGetDirection(BlockFace b) {
        Vector direction = new Vector(b.getModX(), b.getModY(), b.getModZ());
        if (b.getModX() != 0 || b.getModY() != 0 || b.getModZ() != 0)
            direction.normalize();

        return direction;
    }

    public static void locCheckFinite(Location l) throws IllegalArgumentException {
        NumberConversions.checkFinite(l.getX(), "x not finite");
        NumberConversions.checkFinite(l.getY(), "y not finite");
        NumberConversions.checkFinite(l.getZ(), "z not finite");
        NumberConversions.checkFinite(l.getPitch(), "pitch not finite");
        NumberConversions.checkFinite(l.getYaw(), "yaw not finite");
    }

    // ye gpt made it
    public static RayTraceResultEntity rayTraceEntitiesGPT(CraftWorld world, Location start, Vector direction, double maxDistance, double raySize, Predicate<? super Entity> filter) {
        Vector normalizedDirection = direction.clone().normalize();
        Location currentPos = start.clone();
        double stepSize = 0.1; // Adjust step size for accuracy
        Vector step = normalizedDirection.multiply(stepSize);

        for (double i = 0; i < maxDistance; i += stepSize) {
            Collection<Entity> entities = getNearbyEntities(world, currentPos, raySize, raySize, raySize);
            for (Entity entity : entities) {
                if (filter.test(entity)) {
                    BoundingBox entityBB = new BoundingBox(((net.minecraft.server.v1_8_R3.Entity)entity).getBoundingBox());
                    if (entityBB.contains(currentPos.toVector())) {
                        return new RayTraceResultEntity(entity);
                    }
                }
            }

            // Move to the next position along the ray
            currentPos.add(step);
        }

        return null; // No entity found
    }

    /**
     * Gets entities inside a cone.
     *
     * @param entities - {@code List<Entity>}, list of nearby entities
     * @param startpoint - {@code Location}, center point
     * @param radius - {@code int}, radius of the circle
     * @param degrees - {@code int}, angle of the cone
     * @param direction - {@code int}, direction of the cone
     * @return {@code List<Entity>} - entities in the cone
     */
    public static List<Entity> getEntitiesInCone(List<Entity> entities, Location startpoint, int radius, int degrees, double direction)
    {
        List<Entity> newEntities = new ArrayList<>();

        int[] startPos = new int[] { (int)startpoint.getX(), (int)startpoint.getZ() };

        int[] endA = new int[] { (int)(radius * Math.cos(direction - ((double) degrees / 2))), (int)(radius * Math.sin(direction - ((double) degrees / 2))) };

        for(Entity e : entities)
        {
            Location l = e.getLocation();
            int[] entityVector = getVectorForPoints(startPos[0], startPos[1], l.getBlockX(), l.getBlockY());

            double angle = getAngleBetweenVectors(endA, entityVector);
            if(Math.toDegrees(angle) < degrees && Math.toDegrees(angle) > 0)
                newEntities.add(e);
        }
        return newEntities;
    }

    public static boolean isInsideCone(Location l, Location start, int radius, int degrees, double direction) {
        int[] startPos = new int[] { (int)start.getX(), (int)start.getZ() };
        int[] endA = new int[] { (int)(radius * Math.cos(direction - ((double) degrees / 2))), (int)(radius * Math.sin(direction - ((double) degrees / 2))) };
        int[] entityVector = getVectorForPoints(startPos[0], startPos[1], l.getBlockX(), l.getBlockY());

        double angle = getAngleBetweenVectors(endA, entityVector);
        return Math.toDegrees(angle) < degrees && Math.toDegrees(angle) > 0;
    }
    /**
     * Created an integer vector in 2d between two points
     *
     * @param x1 - {@code int}, X pos 1
     * @param y1 - {@code int}, Y pos 1
     * @param x2 - {@code int}, X pos 2
     * @param y2 - {@code int}, Y pos 2
     * @return {@code int[]} - vector
     */
    public static int[] getVectorForPoints(int x1, int y1, int x2, int y2)
    {
        return new int[] { x2 - x1, y2 - y1 };
    }
    /**
     * Get the angle between two vectors.
     *
     * @param vector1 - {@code int[]}, vector 1
     * @param vector2 - {@code int[]}, vector 2
     * @return {@code double} - angle
     */
    public static double getAngleBetweenVectors(int[] vector1, int[] vector2)
    {
        return Math.atan2(vector2[1], vector2[0]) - Math.atan2(vector1[1], vector1[0]);
    }

    public static boolean isInFOV(Player player, Location target, double fov) {
        Location eyeLocation = player.getEyeLocation();
        Vector playerDirection = eyeLocation.getDirection();

        Vector toTarget = target.toVector().subtract(eyeLocation.toVector());

        double angle = playerDirection.angle(toTarget);
        double fovInRadians = Math.toRadians(fov / 2);
        return angle <= fovInRadians;
    }

    /*public static boolean isEntityInPlayerView(Player player, int specificEntityId) {
        Location start = player.getEyeLocation();

        Collection<Entity> nearEntities = player.getWorld().getNearbyEntities(player.getLocation(), 16, 16, 16);
        List<Entity> entities = getEntitiesInCone((List<Entity>) nearEntities, start, 16, 90, player.getLocation().getYaw());
        entities.removeIf(e -> !(e instanceof Player));

        System.out.println("-- Trace2 --");
        for(Entity entity : entities) {
            System.out.println("Trace! (" + entity.getClass().getSimpleName() + ") " + entity.getLocation());
            return entity.getEntityId() == specificEntityId;
        }

        return false;
    }*/
}