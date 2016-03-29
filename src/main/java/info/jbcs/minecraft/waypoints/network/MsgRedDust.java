package info.jbcs.minecraft.waypoints.network;

import info.jbcs.minecraft.waypoints.Waypoint;
import info.jbcs.minecraft.waypoints.WaypointPlayerInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MsgRedDust extends AbstractMessage.AbstractClientMessage<MsgRedDust> {
    private int dimension;
    private double x, y, z;

    public MsgRedDust() {
    }

    public MsgRedDust(int dimension, double x, double y, double z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException {
        dimension = buffer.readInt();
        x = buffer.readDouble();
        y = buffer.readDouble() + 2;
        z = buffer.readDouble();

    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException {
        buffer.writeInt(dimension);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);

    }

    @Override
    public void process(EntityPlayer player, Side side) {
        World world = Minecraft.getMinecraft().theWorld;
        for (int ex = 0; ex < 8; ex++) {
            for (int ey = 0; ey < 8; ey++) {
                for (int ez = 0; ez < 8; ez++) {
                    world.spawnParticle(EnumParticleTypes.REDSTONE, this.x - 1 + ex/4.0, this.y - 1.8 + ey/4.0,
                            this.z - 1 + ez/4.0, 110.0D/250, 25.0D/250, 130.0D/250);
                }
            }
        }
    }


    public int getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}