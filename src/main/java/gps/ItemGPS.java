/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 * <p>
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import java.util.List;

import gps.ItemGPS.Mode;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import aroma1997.core.item.AromicItemMulti;
import aroma1997.core.util.LocalizationHelper;
import aroma1997.core.util.ServerUtil;

public class ItemGPS extends AromicItemMulti<Mode> {
	public ItemGPS() {
		super(Mode.class);
		setUnlocalizedName(Reference.MOD_ID.toLowerCase() + ":gps");
		setMaxStackSize(1);
		setCreativeTab(GPS.tabGPS);
	}

	// --> Item

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote) {
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}

		Mode next = getType(stack).cycle(stack, player);
		setMode(next, stack);

		String s = LocalizationHelper.localizeFormatted(Reference.MOD_ID + ":gps.switch", next.formatting + LocalizationHelper.localize(Reference.MOD_ID + ":gps.mode." + next.name()));
		player.sendMessage(ServerUtil.getChatForString(s));

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag flag) {
		Mode m = getType(stack);
		info.add(m.formatting + LocalizationHelper.localize(Reference.MOD_ID + ":gps.mode." + m.name()));
	}

	//<--- Item

	public ItemStack getGPS(InventoryPlayer inv) {
		for (ItemStack currentStack : inv.mainInventory) {
			if (!currentStack.isEmpty() && currentStack.getItem() == this) {
				return currentStack;
			}
		}
		return null;
	}

	public boolean isGPSEnabled(EntityPlayer player) {
		ItemStack gps = getGPS(player.inventory);
		if (gps == null) {
			return false;
		}
		Mode mode = getType(gps);

		return mode.isOn;
	}

	/**
	 * Whether the second player should be shown on the first player's GPS.
	 *
	 * @param a the first player
	 * @param b the second player
	 * @return true, if the second player should be shown on the first player's GPS. false otherwise.
	 */
	public boolean shouldShowOnGPS(EntityPlayer a, EntityPlayer b) {
		ItemStack gpsA = getGPS(a.inventory);
		if (gpsA == null) {
			return false;
		}
		Mode modeA = getType(gpsA);
		if (modeA == Mode.supercharged) {
			return true;
		}
		ItemStack gpsB = getGPS(b.inventory);
		if (gpsB == null) {
			return false;
		}
		Mode modeB = getType(gpsB);
		return modeB != Mode.off;
	}

	public void setMode(Mode mode, ItemStack stack) {
		stack.setItemDamage(mode.ordinal());
	}

	public static enum Mode {
		off(TextFormatting.RED, false),
		idle(TextFormatting.YELLOW, false),
		on(TextFormatting.GREEN, true),
		supercharged(TextFormatting.GREEN, true);

		public static Mode[] VALUES = values();

		private final TextFormatting formatting;
		public final boolean isOn;

		private Mode(TextFormatting formatting, boolean isOn) {
			this.formatting = formatting;
			this.isOn = isOn;
		}

		public Mode cycle(ItemStack stack, EntityPlayer player) {
			if (this == on && !player.capabilities.isCreativeMode) {
				return off;
			}
			return VALUES[(ordinal() + 1) % VALUES.length];
		}
	}
}
