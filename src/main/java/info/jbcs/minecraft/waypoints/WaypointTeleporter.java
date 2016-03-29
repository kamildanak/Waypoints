package info.jbcs.minecraft.waypoints;

import info.jbcs.minecraft.waypoints.block.BlockWaypoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
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
            int size = BlockWaypoint.checkSize(thePlayer.worldObj, w.pos, 0);
            double x = w.pos.getX() + size / 2.0;
            double y = w.pos.getY() + 0.5;
            double z = w.pos.getZ() + size / 2.0;

            thePlayer.setLocationAndAngles(x, y, z, thePlayer.rotationYaw, thePlayer.rotationPitch);
            thePlayer.setPositionAndUpdate(x, y, z);
            return true;
        } else if (entity instanceof EntityLiving) {
            EntityLiving theMob = (EntityLiving) entity;
            if (theMob.dimension != dim) {
                theMob.timeUntilPortal = 300;
                travelToDimension(theMob, dim, w);
            } else {
                int size = BlockWaypoint.checkSize(theMob.worldObj, w.pos, 0);
                double x = w.pos.getX() + size / 2.0;
                double y = w.pos.getY() + 0.5;
                double z = w.pos.getZ() + size / 2.0;
                theMob.setLocationAndAngles(x, y, z, theMob.rotationYaw, theMob.rotationPitch);
                theMob.setPositionAndUpdate(x, y, z);
            }
            return true;
        }
        return false;
    }

    public void travelToDimension(EntityLiving entityIn, int dim, Waypoint w) {
        if (!entityIn.worldObj.isRemote && !entityIn.isDead) {
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            int j = entityIn.dimension;
            WorldServer wsOld = minecraftserver.worldServerForDimension(j);
            WorldServer wsNew = minecraftserver.worldServerForDimension(dim);
            WorldProvider pOld = wsOld.provider;
            WorldProvider pNew = wsNew.provider;
            double moveFactor = pOld.getMovementFactor() / pNew.getMovementFactor();
            double d0 = entityIn.posX * moveFactor;
            double d1 = entityIn.posZ * moveFactor;
            double d2 = 8.0D;
            float f = entityIn.rotationYaw;
            wsOld.theProfiler.startSection("moving");
            if(entityIn.dimension == 1) {
                int size = BlockWaypoint.checkSize(wsNew, w.pos, 0);
                double x = w.pos.getX() + size / 2.0;
                double y = w.pos.getY() + 0.5;
                double z = w.pos.getZ() + size / 2.0;

                BlockPos blockpos = new BlockPos(x,y,z);

                d0 = (double)blockpos.getX();
                entityIn.posY = (double)blockpos.getY();
                d1 = (double)blockpos.getZ();
                entityIn.setLocationAndAngles(d0, y, d1, entityIn.rotationYaw, entityIn.rotationPitch);
                if(entityIn.isEntityAlive()) {
                    wsOld.updateEntityWithOptionalForce(entityIn, false);
                }
            }

            wsOld.theProfiler.endSection();
            if(j != 1) {
                wsOld.theProfiler.startSection("placing");
                int size = BlockWaypoint.checkSize(wsNew, w.pos, 0);
                double x = w.pos.getX() + size / 2.0;
                double y = w.pos.getY() + 0.5;
                double z = w.pos.getZ() + size / 2.0;

                BlockPos blockpos = new BlockPos(x,y,z);

                d0 = (double)blockpos.getX();
                entityIn.posY = (double)blockpos.getY();
                d1 = (double)blockpos.getZ();

                if(entityIn.isEntityAlive()) {
                    entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
                    this.placeInPortal(entityIn, f);
                    wsNew.spawnEntityInWorld(entityIn);
                    wsNew.updateEntityWithOptionalForce(entityIn, false);
                }

                wsOld.theProfiler.endSection();
            }

            entityIn.setWorld(wsNew);
        }

    }

    @Override
    public boolean placeInExistingPortal(Entity entityIn, float p_180620_2_) {
        return false;
    }

    @Override
    public void removeStalePortalLocations(long par1) {
    }

    @Override
    public void placeInPortal(Entity entityIn, float rotationYaw) {
    }
}