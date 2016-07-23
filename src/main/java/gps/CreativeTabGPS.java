/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 *
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
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
