package info.jbcs.minecraft.waypoints;

import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class WaypointTeleporter extends Teleporter {

    private WorldServer worldserver;

    public WaypointTeleporter(WorldServer worldServer) {
        super(worldServer);
    }


    // Move the Entity to the portal
    public boolean teleport(Entity entity, World world, Waypoint w) {
        int dim = w.dimension;
        MinecraftServer mcServer = MinecraftServer.getServer();
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP thePlayer = (EntityPlayerMP) entity;
            if (thePlayer.dimension != dim) {
                mcServer.getConfigurationManager().transferPlayerToDimension(thePlayer, dim, new WaypointTeleporter(mcServer.worldServerForDimension(dim)));
            }
            int size = BlockWaypoint.checkSize(thePlayer.worldObj, w.x, w.y, w.z);
            double x = w.x + size / 2.0;
            double y = w.y + 0.5;
            double z = w.z + size / 2.0;

            thePlayer.setLocationAndAngles(x, y, z, thePlayer.rotationYaw, thePlayer.rotationPitch);
            thePlayer.setPositionAndUpdate(x, y, z);
            return true;
        } else if (entity instanceof EntityLiving) {
            EntityLiving theMob = (EntityLiving) entity;
            if (theMob.dimension != dim) {
                theMob.timeUntilPortal = 300;
                travelToDimension(theMob, dim, w);
            } else {
                int size = BlockWaypoint.checkSize(theMob.worldObj, w.x, w.y, w.z);
                double x = w.x + size / 2.0;
                double y = w.y + 0.5;
                double z = w.z + size / 2.0;
                theMob.setLocationAndAngles(x, y, z, theMob.rotationYaw, theMob.rotationPitch);
                theMob.setPositionAndUpdate(x, y, z);
            }
            return true;
        }
        return false;
    }

    public void travelToDimension(EntityLiving entityLiving, int dim, Waypoint w) {
        if (!entityLiving.worldObj.isRemote && !entityLiving.isDead) {
            entityLiving.worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            int j = entityLiving.dimension;
            WorldServer wsOld = minecraftserver.worldServerForDimension(j);
            WorldServer wsNew = minecraftserver.worldServerForDimension(dim);
            entityLiving.dimension = dim;

            if (j == 1 && dim == 1) {
                wsNew = minecraftserver.worldServerForDimension(0);
                entityLiving.dimension = 0;
            }

            entityLiving.worldObj.removeEntity(entityLiving);
            entityLiving.isDead = false;
            entityLiving.worldObj.theProfiler.startSection("reposition");
            minecraftserver.getConfigurationManager().transferEntityToWorld(entityLiving, j, wsOld, wsNew);
            entityLiving.worldObj.theProfiler.endStartSection("reloading");
            Entity entity = EntityList.createEntityByName(EntityList.getEntityString(entityLiving), wsNew);
            if (entity != null) {
                int size = BlockWaypoint.checkSize(wsNew, w.x, w.y, w.z);
                double x = w.x + size / 2.0;
                double y = w.y + 0.5;
                double z = w.z + size / 2.0;

                wsNew.spawnEntityInWorld(entity);
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
                ((EntityLiving) entity).setPositionAndUpdate(x, y, z);
            }

            entityLiving.isDead = true;
            entityLiving.worldObj.theProfiler.endSection();
            wsOld.resetUpdateEntityTick();
            wsNew.resetUpdateEntityTick();
            entityLiving.worldObj.theProfiler.endSection();
        }

    }

    @Override
    public boolean placeInExistingPortal(Entity par1Entity, double par2, double par4, double par6, float par8) {
        return false;
    }

    @Override
    public void removeStalePortalLocations(long par1) {
    }

    @Override
    public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8) {
    }
}