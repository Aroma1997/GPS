/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 * <p>
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gps.trilaterate.GuiTrilaterate;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import aroma1997.core.util.LocalizationHelper;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
	public final Minecraft mc;
	private RenderItem renderItem;
	private ItemStack pocketSundial;
	private ItemStack compass;
	public List<PlayerData> dataList = new ArrayList<>();
	private KeyBinding kb;
	private boolean displaying = true;
	public final Comparator<PlayerData> playerDatComparator;

	public ClientProxy() {
		super();
		mc = Minecraft.getMinecraft();
		ClientRegistry.registerKeyBinding(kb = new KeyBinding("Toggle GPS", Keyboard.KEY_F7, "GPS"));
		playerDatComparator = new Comparator<PlayerData>() {

			@Override
			public int compare(PlayerData o1, PlayerData o2) {
				int comp = 0;
				EntityPlayerSP player = mc.player;
				if (o1.dimension != o2.dimension) {
					//Players in the same dimension first.
					comp = Integer.compare(Math.abs(player.dimension - o1.dimension), Math.abs(player.dimension - o2.dimension));
				}
				if (comp == 0 && player.dimension == o1.dimension) {
					//If the first player hast the same dimension as the client player, the second one does, too,
					//because otherwise comp would be set to 0.
					//Sort by distance.
					comp = Double.compare(o1.getDistance(player.getPosition()), o2.getDistance(player.getPosition()));
				}
				if (comp == 0) {
					//Sort by username.
					comp = o1.username.compareTo(o2.username);
				}
				return comp;
			}
		};

	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.START) {
			if (kb.isPressed()) {
				toggleDisplay();
			}
		}
	}

	@SubscribeEvent
	public void renderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == Phase.END) {
			if (mc.player == null) {
				return;
			}

			if ((!displaying || !(GPS.gps.isGPSEnabled(mc.player))) || !mc.inGameHasFocus || mc.world == null || mc.gameSettings.showDebugInfo || (mc.gameSettings.keyBindPlayerList.isKeyDown() && (!mc.isIntegratedServerRunning() || mc.player.connection.getPlayerInfoMap().size() > 1))) {
				return;
			}

			int h = new ScaledResolution(mc).getScaledHeight();

			if (dataList.isEmpty()) {
				mc.fontRenderer.drawStringWithShadow(LocalizationHelper.localize("gps:gps.hud.noplayers"), 2, 2, 0x00FFFFFF);
			} else {
				int y = 2;

				for (PlayerData data : dataList) {
					mc.fontRenderer.drawStringWithShadow(data.username + ": " + (data.dimension == mc.player.dimension ? Math.round(data.getDistance(mc.player.getPosition())) + "m" : "@ " + GPS.proxy.getDimensionName(data.dimension)), 2, y, 0x00FFFFFF);
					y += 10;
					if (y + 35 >= h) {
						//Complete screen is filled.
						break;
					}
				}
			}

			if (renderItem == null) {
				renderItem = mc.getRenderItem();
			}
			if (pocketSundial == null) {
				pocketSundial = new ItemStack(Items.CLOCK);
			}
			if (compass == null) {
				compass = new ItemStack(Items.COMPASS);
			}
			RenderHelper.enableGUIStandardItemLighting();
			renderItem.renderItemIntoGUI(pocketSundial, 1, h - 18);
			renderItem.renderItemIntoGUI(compass, 1, h - 34);
			RenderHelper.disableStandardItemLighting();

			boolean ampm = true;
			double time = (mc.world.getWorldTime() + 8000) % 24000;
			int hour = (int) Math.floor(time / 1000);
			if (ampm) {
				hour %= 12;
				if (hour == 0) {
					hour = 12;
				}
			}
			String s;
			if (mc.world.provider.isSurfaceWorld()) {
				s = String.format("%2d", hour).replace(' ', '0') + ":" + String.format("%2d", (int) Math.floor((time / 16.66666666666667D) % 60)).replace(' ', '0') + (ampm ? (time >= 12000 ? "pm" : "am") : "");
			} else {
				s = "No time";
			}
			mc.fontRenderer.drawStringWithShadow(s, 19, h - 14, 0x00FFFFFF);

			BlockPos spawn = mc.world.getSpawnPoint();
			s = Math.round(Math.sqrt(mc.player.getDistanceSq(spawn))) + "m";
			mc.fontRenderer.drawStringWithShadow(s, 19, h - 30, 0x00FFFFFF);
		}
	}

	private void toggleDisplay() {
		//Do not toggle display if the GPS is disabled.
		if (!(GPS.gps.isGPSEnabled(mc.player))) {
			return;
		}
		displaying = !displaying;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case 0:
			return new GuiTrilaterate();
		default:
			assert false;
			return null;
		}
	}
}
