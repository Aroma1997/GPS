/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 *
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gps.ItemGPS.Mode;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
	public Minecraft mc;
	private RenderItem renderItem;
	private ItemStack pocketSundial;
	private ItemStack compass;
	public List<PlayerData> dataList = new ArrayList<PlayerData>();
	private KeyBinding kb;
	private boolean displaying = false;

	public ClientProxy() {
		super();
		mc = Minecraft.getMinecraft();
		ClientRegistry.registerKeyBinding(kb = new KeyBinding("Toggle GPS", Keyboard.KEY_F7, "GPS") {

		});
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
			if (mc.thePlayer == null) return;

			ItemStack selected = mc.thePlayer.getHeldItemMainhand();
			Mode mode = ItemGPS.getMode(selected);
			if ((!displaying && !(ItemGPS.isGPSEnabled(mc.thePlayer))) || !mc.inGameHasFocus || mc.theWorld == null || mc.gameSettings.showDebugInfo || (mc.gameSettings.keyBindPlayerList.isKeyDown() && (!mc.isIntegratedServerRunning() || mc.thePlayer.connection.getPlayerInfoMap().size() > 1))) return;

			if (dataList.isEmpty()) {
				mc.fontRendererObj.drawStringWithShadow(I18n.translateToLocal("GPS:gps.hud.noplayers"), 2, 2, 0x00FFFFFF);
			} else {
				int y = 2;

				for (PlayerData data : dataList) {
					mc.fontRendererObj.drawStringWithShadow(data.username+": "+(data.dimension == mc.thePlayer.dimension ? Math.round(Math.sqrt(mc.thePlayer.getDistanceSq(data.pos)))+"m" : "@ "+GPS.proxy.getDimensionName(data.dimension)), 2, y, 0x00FFFFFF);
					y += 10;
				}
			}

			int h = new ScaledResolution(mc).getScaledHeight();

			if (renderItem == null) renderItem = mc.getRenderItem();
			if (pocketSundial == null) pocketSundial = new ItemStack(Items.CLOCK);
			if (compass == null) compass = new ItemStack(Items.COMPASS);
			RenderHelper.enableGUIStandardItemLighting();
			renderItem.renderItemIntoGUI(pocketSundial, 1, h - 18);
			renderItem.renderItemIntoGUI(compass, 1, h - 34);
			RenderHelper.disableStandardItemLighting();

			boolean ampm = true;
			double time = (mc.theWorld.getWorldTime() + 8000) % 24000;
			int hour = (int)Math.floor(time / 1000);
			if (ampm) {
				hour %= 12;
				if (hour == 0) hour = 12;
			}
			String s;
			if (mc.theWorld.provider.isSurfaceWorld()) s = String.format("%2d", hour).replace(' ', '0')+":"+String.format("%2d", (int)Math.floor((time / 16.66666666666667D) % 60)).replace(' ', '0')+(ampm ? (time >= 12000 ? "pm" : "am") : "");
			else s = "No time";
			mc.fontRendererObj.drawStringWithShadow(s, 19, h - 14, 0x00FFFFFF);

			BlockPos spawn = mc.theWorld.getSpawnPoint();
			s = Math.round(Math.sqrt(mc.thePlayer.getDistanceSq(spawn)))+"m";
			mc.fontRendererObj.drawStringWithShadow(s, 19, h - 30, 0x00FFFFFF);
		}
	}

	@Override
	public File getMinecraftDir() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	private void toggleDisplay() {
		displaying = !displaying;
		if (GraviSuitCompat.graviSuitEnabled) GraviSuitCompat.toggleHudPos(displaying);
	}
}
