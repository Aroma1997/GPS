/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 *
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import aroma1997.core.item.AromicCreativeTab;
import aroma1997.core.network.NetworkHelper;
import aroma1997.core.network.PacketHandler;
import aroma1997.core.util.AromaRegistry;
import aroma1997.core.util.registry.AutoRegister;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, dependencies = "required-after:aroma1997core")
public class GPS {
	@SidedProxy(clientSide = "gps.ClientProxy", serverSide = "gps.ServerProxy")
	public static ServerProxy proxy;

	@AutoRegister
	public static ItemGPS gps;
	public static PacketHandler ph;

	public static CreativeTabs tabGPS = new AromicCreativeTab(Reference.MOD_ID + ":gps", () -> new ItemStack(gps, 1, ItemGPS.Mode.on.ordinal()));

	public static int updatesPerTick = 5;

	@EventHandler
	public void onLoad(FMLPreInitializationEvent event) {
		ph = NetworkHelper.getPacketHandler(Reference.MOD_ID);
		ph.registerMessage(GpsPacket.class, GpsPacket.class, 0, Side.CLIENT);
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		Property prop;

		prop = config.get(Configuration.CATEGORY_GENERAL, "updatesPerTick", updatesPerTick);
		prop.setComment("Number of updates per tick to send, or for negative values, update interval in ticks. Each update sends a single packet to a player.");
		updatesPerTick = prop.getInt();
		if (updatesPerTick <= 0) updatesPerTick = 1;

		gps = new ItemGPS();
		AromaRegistry.register(getClass());

		config.save();

		AromaRegistry.registerShapedRecipe(new ItemStack(gps), "RRR", "@I#", "RRR", 'R', Items.REDSTONE, 'I', Items.IRON_INGOT, '@', Items.CLOCK, '#', Items.COMPASS);
		AromaRegistry.registerShapelessRecipe(new ItemStack(gps, 1, ItemGPS.Mode.supercharged.ordinal()), gps, Items.REDSTONE);
	}
}
