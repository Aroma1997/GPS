/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 *
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import aroma1997.core.network.NetworkHelper;
import aroma1997.core.network.PacketHandler;
import aroma1997.core.util.AromaRegistry;
import aroma1997.core.util.registry.AutoRegister;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, dependencies = "required-after:Aroma1997Core")
public class GPS {
	@SidedProxy(clientSide = "gps.ClientProxy", serverSide = "gps.ServerProxy")
	public static ServerProxy proxy;

	public static CreativeTabGPS tabGPS = new CreativeTabGPS();
	@AutoRegister
	public static Item gps;
	public static PacketHandler ph;

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

		AromaRegistry.registerShapedAromicRecipe(new ItemStack(gps), false, "RRR", "@I#", "RRR", 'R', Items.REDSTONE, 'I', Items.IRON_INGOT, '@', Items.CLOCK, '#', Items.COMPASS);
		AromaRegistry.registerShapelessAromicRecipe(new ItemStack(gps, 1, ItemGPS.Mode.SUPERCHARGED.ordinal()), false, gps, Items.REDSTONE);
	}

	@EventHandler
	public void onLoaded(FMLPostInitializationEvent event) {
		GraviSuitCompat.init();
	}
}
