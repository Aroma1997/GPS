package gps.trilaterate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aroma1997.core.util.LocalizationHelper;
import gps.GPS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTrilaterate extends GuiScreen {
	
	private final List<Tuple<String, String>> players;

	public GuiTrilaterate() {
    	players = new ArrayList<>();
    	for (NetworkPlayerInfo info : Minecraft.getMinecraft().player.connection.getPlayerInfoMap()) {
    		String name = info.getGameProfile().getName();
    		players.add(new Tuple<String, String>(name, name));
    	}
    	
    	for (String additional : GPS.trilaterate.additionalPositions.keySet()) {
    		players.add(new Tuple<String, String>(additional, getDisplayName(additional)));
    	}
    	
    	Collections.sort(players, (tuple1, tuple2) -> tuple1.getFirst().compareTo(tuple2.getFirst()));
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
    		addButton(new GuiButton(i, xStart, yStart + 2 * BUTTON_HEIGHT + i * 20, BUTTON_WIDTH, BUTTON_HEIGHT, players.get(i).getSecond()));
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
		packet.playerName = players.get(id).getFirst();
		GPS.ph.sendPacketToPlayers(packet);
	}
	
	public static String getDisplayName(String internalName) {
		if (LocalizationHelper.hasLocalization("gps:trilaterate.gui.additionals." + internalName)) {
			return LocalizationHelper.localize("gps:trilaterate.gui.additionals." + internalName);
		}
		return internalName;
	}

}
