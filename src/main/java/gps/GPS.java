/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 * <p>
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import gps.trilaterate.ItemTrilaterate;
import gps.trilaterate.PacketTrilaterate;
import org.apache.logging.log4j.Level;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import aroma1997.core.item.AromicCreativeTab;
import aroma1997.core.log.LogHelper;
import aroma1997.core.network.NetworkHelper;
import aroma1997.core.network.PacketHandler;
import aroma1997.core.util.AromaRegistry;
import aroma1997.core.util.registry.AutoRegister;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, dependencies = "required-after:aroma1997core", certificateFingerprint = "dfbfe4c473253d8c5652417689848f650b2cbe32")
public class GPS {

	@Instance
	public static GPS instance;

	@SidedProxy(clientSide = "gps.ClientProxy", serverSide = "gps.ServerProxy")
	public static ServerProxy proxy;

	@AutoRegister
	public static ItemGPS gps;
	@AutoRegister
	public static ItemTrilaterate trilaterate;
	public static PacketHandler ph;

	public static CreativeTabs tabGPS = new AromicCreativeTab(Reference.MOD_ID + ":gps", () -> new ItemStack(gps, 1, ItemGPS.Mode.on.ordinal()));

	public static int updatesPerTick = 5;

	@EventHandler
	public void onLoad(FMLPreInitializationEvent event) {
		ph = NetworkHelper.getPacketHandler(Reference.MOD_ID);
		ph.registerMessage(GpsPacket.class, GpsPacket.class, 0, Side.CLIENT);
		ph.registerMessage(PacketTrilaterate.class, PacketTrilaterate.class, 1, Side.SERVER);
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		Property prop;

		prop = config.get(Configuration.CATEGORY_GENERAL, "updatesPerTick", updatesPerTick);
		prop.setComment("Number of updates per tick to send, or for negative values, update interval in ticks. Each update sends a single packet to a player.");
		updatesPerTick = prop.getInt();
		if (updatesPerTick <= 0) {
			updatesPerTick = 1;
		}

		gps = new ItemGPS();
		trilaterate = new ItemTrilaterate();
		AromaRegistry.register(this);

		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
	}

	@EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		LogHelper.log(Level.WARN, "Invalid fingerprint detected! The version of the mod is most likely modified and an inofficial release.");
		LogHelper.log(Level.WARN, "Use with caution.");

	}
}
