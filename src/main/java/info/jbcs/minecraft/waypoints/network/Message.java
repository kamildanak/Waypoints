package info.jbcs.minecraft.waypoints.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public abstract class Message implements IMessage {
    public abstract void fromBytes(ByteBuf buffer);

    public abstract void toBytes(ByteBuf buffer);
}