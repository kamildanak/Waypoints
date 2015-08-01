package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class MsgRedDust extends Message {
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
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readDouble();
        y = buf.readDouble() + 2;
        z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
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

    public static class Handler implements IMessageHandler<MsgRedDust, IMessage> {

        @Override
        public IMessage onMessage(MsgRedDust message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().theWorld;
            for (int ex = 0; ex < 8; ex++) {
                for (int ey = 0; ey < 8; ey++) {
                    for (int ez = 0; ez < 8; ez++) {
                        world.spawnParticle("reddust", message.x - 1 + ex / 4.0, message.y - 1.8 + ey / 4.0, message.z - 1 + ez / 4.0, 110.0D / 250, 25.0D / 250, 130.0D / 250);
                    }
                }
            }
            return null;
        }
    }
}