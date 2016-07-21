package gps;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabGPS extends CreativeTabs {
	public CreativeTabGPS() {
		super(Reference.MOD_ID.toLowerCase() + ":gps");
	}

	@Override
	public Item getTabIconItem() {
		return GPS.gps;
	}
}
