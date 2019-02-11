/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 * <p>
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ServerProxy implements IGuiHandler {
	public int ticker = 0;
	private int currentPlayer = 0;

	public ServerProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == Phase.END) {
			if (GPS.updatesPerTick < 0) {
				if (--ticker <= GPS.updatesPerTick) {
					ticker = 0;
				} else {
					return;
				}
			}

			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			PlayerList manager = server.getPlayerList();
			if (manager == null) {
				return;
			}

			for (int i = 0; i < (GPS.updatesPerTick < 1 ? 1 : GPS.updatesPerTick); i++) {
				if (manager.getCurrentPlayerCount() < 1) {
					break;
				}

				EntityPlayerMP player = null;
				while (player == null || !GPS.gps.isGPSEnabled(player) || player.interactionManager.getGameType() == GameType.SPECTATOR) {
					currentPlayer++;
					if (currentPlayer >= manager.getCurrentPlayerCount()) {
						currentPlayer = -1;
						break;
					}

					player = manager.getPlayers().get(currentPlayer);
				}

				if (player == null || currentPlayer == -1) {
					break;
				}

				GpsPacket packet = new GpsPacket(player, server);
				GPS.ph.sendPacketToPlayer(player, packet);
			}
		}
	}

	public String getDimensionName(int dim) {
		try {
			DimensionType type = DimensionManager.getProviderType(dim);
			if (type != null) {
				return type.getName();
			}
		} catch (IllegalArgumentException e) {
		}
		return "Dim " + dim;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		assert false;
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		assert false;
		return null;
	}
}
