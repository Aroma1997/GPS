package gps.trilaterate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aroma1997.core.util.LocalizationHelper;
import gps.ClientProxy;
import gps.GPS;
import gps.PlayerData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTrilaterate extends GuiScreen {
	
	private final List<String> players;

	public GuiTrilaterate() {
    	players = new ArrayList<>();
    	for (PlayerData data : ((ClientProxy)GPS.proxy).dataList) {
    		if (data.dimension != Minecraft.getMinecraft().player.dimension) {
    			//The PlayerData List is sorted, so once we reached the first one from another dimension,
    			//The rest will be in a different dimension as well.
    			break;
    		}
    		players.add(data.username);
    	}
    	
    	for (String additional : ItemTrilaterate.additionalPositions.keySet()) {
    		players.add(additional);
    	}
    	
    	Collections.sort(players, (a, b) -> a.startsWith("_") ? b.startsWith("_") ? a.compareTo(b) : 1 : b.startsWith("_") ? -1 : a.compareTo(b));
	}
	
    @Override
	public void initGui()
    {
    	super.initGui();
    	
    	final int BUTTON_WIDTH = 200;
    	final int BUTTON_HEIGHT = 20;
    	int xStart = (width - BUTTON_WIDTH) / 2;
    	int yStart = height / 2 - (players.size() + 2) * BUTTON_HEIGHT / 2;

    	addButton(new GuiButton(-1, xStart, yStart, BUTTON_WIDTH, BUTTON_HEIGHT, LocalizationHelper.localize("gps:trilaterate.gui.start")));
    	
    	for (int i = 0; i < players.size(); i++) {
    		addButton(new GuiButton(i, xStart, yStart + 2 * BUTTON_HEIGHT + i * 20, BUTTON_WIDTH, BUTTON_HEIGHT, ItemTrilaterate.getDisplayName(players.get(i))));
    	}
    	
    }
	
	@Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
    	if (button.id >= 0 && button.id < players.size()) {
    		selectPlayer(button.id);
    	} else if (button.id == -1) {
    		GPS.trilaterate.doTrilateration(Minecraft.getMinecraft().player);
    	}
		Minecraft.getMinecraft().displayGuiScreen(null);
    }
	
	private void selectPlayer(int id) {
		PacketTrilaterate packet = new PacketTrilaterate();
		packet.playerName = players.get(id);
		GPS.ph.sendPacketToPlayers(packet);
	}

}
