package gps.trilaterate.journeymap;

import javax.annotation.ParametersAreNonnullByDefault;

import aroma1997.core.client.util.Colors;
import aroma1997.core.log.LogHelper;
import aroma1997.core.util.LocalizationHelper;
import aroma1997.core.util.ServerUtil;
import gps.GPS;
import gps.Reference;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.event.ClientEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@ParametersAreNonnullByDefault
@ClientPlugin
public class Plugin implements IClientPlugin {

	private IClientAPI api;

	@Override
	public String getModId() {
		return Reference.MOD_ID;
	}

	@Override
	public void initialize(IClientAPI api) {
		this.api = api;
		GPS.trilaterate.finishedSetting = GPS.trilaterate.finishedSetting.andThen(this::addWaypoint);
	}

	@Override
	public void onEvent(ClientEvent event) {
		// TODO Auto-generated method stub

	}
	
	private void addWaypoint(EntityPlayer player, Tuple<Vec3i, String> t) {
		Vec3i pos = t.getFirst();
		if (pos == Vec3i.NULL_VECTOR) {
			//Invalid result. Nothing to do.
			return;
		}
		String name = t.getSecond();
		try {
			if (api.playerAccepts(Reference.MOD_ID, DisplayType.Waypoint)) {
				// Waypoint itself
				ModWaypoint waypoint = new ModWaypoint(Reference.MOD_ID, name, "Trilateration Points", name, new BlockPos(pos),
						null, name.hashCode() & 0xFFFFFF, true, player.world.provider.getDimension());
	
				// Add or update
				api.show(waypoint);
			}

		} catch (Exception e) {
			LogHelper.logException(e.getMessage(), e);
		}
		
		player.sendMessage(ServerUtil.getChatForString(Colors.GREEN + LocalizationHelper.localize("gps:trilaterate.journeymap.success")));
	}

}
