/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 *
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import aroma1997.core.log.LogHelper;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class GpsPacket implements IMessage,
		IMessageHandler<GpsPacket, IMessage> {

	public GpsPacket(EntityPlayer target, MinecraftServer server) {
		dataList = new LinkedList<PlayerData>();
		for (EntityPlayer player : server.getPlayerList().getPlayerList()) {
			if (player == target) continue;
			if (ItemGPS.shouldShowOnGPS(target, player)) {
				dataList.add(new PlayerData(player.getDisplayNameString(), player.worldObj.provider.getDimension(), player.getPosition()));
			}
		}
	}

	public GpsPacket(){};

	List<PlayerData> dataList;

	@Override
	public IMessage onMessage(GpsPacket message, MessageContext ctx) {
		if (ctx.side == Side.CLIENT) {
			ClientProxy proxy = (ClientProxy) GPS.proxy;
			List<PlayerData> data = message.dataList;
			Collections.sort(data, proxy.playerDatComparator);
			proxy.dataList = data;
		}
		else {
			LogHelper.log(Level.ERROR, "Received GPS message on the server. Aborting.");
		}
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int size = buf.readUnsignedByte();
		dataList = new ArrayList<PlayerData>(size);
		for (int i = 0; i < size; i++) {
			dataList.add(readPlayerData(buf));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(dataList.size());
		for (PlayerData  data : dataList) {
			writePlayerData(data, buf);
		}
	}

	private PlayerData readPlayerData(ByteBuf buf) {
		char[] chars = new char[buf.readUnsignedByte()];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = buf.readChar();
		}
		String username = new String(chars);
		BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		int dimension = buf.readInt();
		return new PlayerData(username, dimension, pos);
	}

	private void writePlayerData(PlayerData data, ByteBuf buf) {
		buf.writeByte(data.username.length());
		for (int i = 0; i < data.username.length(); i++) {
			buf.writeChar(data.username.charAt(i));
		}
		buf.writeInt(data.pos.getX());
		buf.writeInt(data.pos.getY());
		buf.writeInt(data.pos.getZ());
		buf.writeInt(data.dimension);
	}

}
