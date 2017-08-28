package gps.trilaterate;

import gps.GPS;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import aroma1997.core.network.NetworkHelper;
import aroma1997.core.util.LocalizationHelper;
import aroma1997.core.util.ServerUtil;

public class PacketTrilaterate implements IMessage, IMessageHandler<PacketTrilaterate, IMessage> {

	String playerName;

	@Override
	public IMessage onMessage(PacketTrilaterate message, MessageContext ctx) {
		NetHandlerPlayServer netHandler = ctx.getServerHandler();
		EntityPlayer player = netHandler.player;
		ItemStack stack = GPS.trilaterate.getCurrentTrilaterationItem(player);
		if (stack.isEmpty() || message.playerName == null) {
			return null;
		}
		//player clicked on name to set target.
		GPS.trilaterate.setPlayer(stack, message.playerName);
		player.sendMessage(ServerUtil.getChatForString(LocalizationHelper.localizeFormatted("gps:trilaterationswitched", ItemTrilaterate.getDisplayName(message.playerName))));
		player.inventoryContainer.detectAndSendChanges();

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		playerName = NetworkHelper.readString(buf);

	}

	@Override
	public void toBytes(ByteBuf buf) {
		NetworkHelper.writeString(buf, playerName);

	}

}
