/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 *
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import java.util.List;

import aroma1997.core.items.AromicItem;
import aroma1997.core.util.ServerUtil;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemGPS extends AromicItem {
	public ItemGPS() {
		super();
		setUnlocalizedName(Reference.MOD_ID.toLowerCase() + ":gps");
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(GPS.tabGPS);
	}

	// --> Item

	@Override
    public String getUnlocalizedName(ItemStack stack)
    {
    	return super.getUnlocalizedName() + "_" + getMode(stack).name().toLowerCase();
    }

	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
		if (world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);

		Mode next = getMode(stack).cycle(stack, player);
		setMode(next, stack);

		String s = I18n.translateToLocalFormatted(Reference.MOD_ID + ":gps.switch", next.formatting + I18n.translateToLocal(Reference.MOD_ID + ":gps.mode." + next.name()));
		player.addChatMessage(ServerUtil.getChatForString(s));

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean debug) {
		Mode m = getMode(stack);
		info.add(m.formatting + I18n.translateToLocal(Reference.MOD_ID + ":gps.mode." + m.name()));
	}

	@Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
    	for (Mode mode : Mode.VALUES) {
    		subItems.add(new ItemStack(itemIn, 1, mode.ordinal()));
    	}
    }


	//<--- Item

	public static ItemStack getGPS(InventoryPlayer inv) {
		for (int i = 0; i < inv.mainInventory.length; i++) {
			if (inv.mainInventory[i] != null && inv.mainInventory[i].getItem() == GPS.gps) return inv.mainInventory[i];
		}

		return null;
	}

	public static boolean isGPSEnabled(EntityPlayer player) {
		ItemStack gps = getGPS(player.inventory);
		if (gps == null) return false;
		Mode mode = getMode(gps);

		return mode.isOn;
	}

	/**
	 * Wether the second player should be shown on the firs player's GPS.
	 * @param a the first player
	 * @param b the second player
	 * @return true, if the second player should be shown on the firs player's GPS. false otherwise.
	 */
	public static boolean shouldShowOnGPS(EntityPlayer a, EntityPlayer b) {
		ItemStack gpsA = getGPS(a.inventory);
		if (gpsA == null) return false;
		Mode modeA = getMode(gpsA);
		if (modeA == Mode.SUPERCHARGED) {
			return true;
		}
		ItemStack gpsB = getGPS(b.inventory);
		if (gpsB == null) return false;
		Mode modeB = getMode(gpsB);
		return modeB != Mode.OFF;
	}

	public static Mode getMode(ItemStack stack) {
		if (stack == null || stack.getItem() != GPS.gps) return Mode.OFF;
		if (stack.getItemDamage() < Mode.VALUES.length && stack.getItemDamage() >= 0) {
			return Mode.VALUES[stack.getItemDamage()];
		}
		return Mode.OFF;
	}

	public static void setMode(Mode mode, ItemStack stack) {
		stack.setItemDamage(mode.ordinal());
	}

	public static enum Mode {
		OFF(TextFormatting.RED, false),
		IDLE(TextFormatting.YELLOW, false),
		ON(TextFormatting.GREEN, true),
		SUPERCHARGED(TextFormatting.GREEN, true);

		public static Mode[] VALUES = values();

		private final TextFormatting formatting;
		public final boolean isOn;

		private Mode(TextFormatting formatting, boolean isOn) {
			this.formatting = formatting;
			this.isOn = isOn;
		}

		public Mode cycle(ItemStack stack, EntityPlayer player) {
			if (this == ON && !player.capabilities.isCreativeMode) return OFF;
			return VALUES[(ordinal() + 1) % VALUES.length];
		}
	}
}
