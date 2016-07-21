package gps;

import java.io.File;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.DimensionType;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ServerProxy {
	public int ticker = 0;
	private int currentPlayer = 0;

	public ServerProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == Phase.END) {
			if (GPS.updatesPerTick < 0) {
				if (--ticker <= GPS.updatesPerTick) ticker = 0;
				else return;
			}

			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			PlayerList manager = server.getPlayerList();
			if (manager == null) return;

			for (int i = 0; i < (GPS.updatesPerTick < 1 ? 1 : GPS.updatesPerTick); i++) {
				if (manager.getCurrentPlayerCount() < 1) break;

				EntityPlayerMP player = null;
				while (player == null || !ItemGPS.isGPSEnabled(player)) {
					currentPlayer++;
					if (currentPlayer >= manager.getCurrentPlayerCount()) {
						currentPlayer = -1;
						break;
					}

					player = manager.getPlayerList().get(currentPlayer);
				}

				if (player == null) break;

				GpsPacket packet = new GpsPacket(player, server);
				GPS.ph.sendPacketToPlayer(player, packet);
			}
		}
	}

	public File getMinecraftDir() {
		return new File(".");
	}

	public int addArmor(String file) {
		return 0;
	}

	public String getDimensionName(int dim) {
		try {
			DimensionType type = DimensionManager.getProviderType(dim);
			if (type != null) return type.getName();
		} catch (IllegalArgumentException e) {}
		return "Dim "+dim;
	}
}
