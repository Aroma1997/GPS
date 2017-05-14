package gps.trilaterate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import aroma1997.core.client.util.Colors;
import aroma1997.core.item.AromicItem;
import aroma1997.core.util.LocalizationHelper;
import aroma1997.core.util.ServerUtil;
import gps.GPS;
import gps.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ItemTrilaterate extends AromicItem {
	
	static final Map<String, Function<EntityPlayer, ? extends Vec3i>> additionalPositions = new HashMap<>();
	
	public BiConsumer<EntityPlayer, Tuple<Vec3i, String>> finishedSetting = this::printTrilaterationResult;
	
	public ItemTrilaterate() {
		setUnlocalizedName(Reference.MOD_ID.toLowerCase() + ":gps");
		setMaxStackSize(1);
		setCreativeTab(GPS.tabGPS);
		
		//Names need to start with a "_"
		additionalPositions.put("_worldspawn", player -> player.world.getSpawnPoint());
		additionalPositions.put("_playerspawn", player -> player.getBedLocation() == null ? player.world.getSpawnPoint() : player.getBedLocation());
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		//No GPS, no Trilateration.
		if (!GPS.gps.isGPSEnabled(player)) {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		NBTTagCompound nbt;
		if (stack.hasTagCompound()) {
			nbt = stack.getTagCompound();
		} else {
			stack.setTagCompound(nbt = new NBTTagCompound());
		}
		
		if (player.isSneaking()) {
			//Open GUI
			player.openGui(GPS.instance, 0, world, (int)player.posX, (int)player.posY, (int)player.posZ);
//			}
		} else {
			//Add point
			if (!world.isRemote) {
				PointList list = new PointList();
				list.readFromNBT(nbt);
				Vec3i addingPos = getPositionFor(list.trackingPlayer, player);
				if (addingPos == null) {
					player.sendMessage(ServerUtil.getChatForString(LocalizationHelper.localize("gps:trilaterationaddingfailed")));
				} else {
					list.addPoint(player.getPosition(), (int)Math.floor(Math.sqrt(addingPos.distanceSq(player.getPosition()))));
					player.sendMessage(ServerUtil.getChatForString(LocalizationHelper.localize("gps:trilaterationadded")));
					
					nbt = list.writeToNBT(nbt);
					player.sendMessage(ServerUtil.getChatForString("Now contains the following points: " + list));
					stack.setTagCompound(nbt);
					player.setHeldItem(hand, stack);
				}
			}
		}
		
    	return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }
	
	public ItemStack setPlayer(ItemStack stack, String playername) {
		NBTTagCompound nbt;
		if (stack.hasTagCompound()) {
			nbt = stack.getTagCompound();
		} else {
			stack.setTagCompound(nbt = new NBTTagCompound());
		}
		PointList list = new PointList();
		list.readFromNBT(nbt);
		list.setTrackingPlayer(playername);
		nbt = list.writeToNBT(nbt);
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public Vec3i getPositionFor(String name, EntityPlayer player) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return Vec3i.NULL_VECTOR;
		}
		Function<EntityPlayer, ? extends Vec3i> supplier = additionalPositions.get(name);
		if (supplier != null) {
			return supplier.apply(player);
		}
		
		for (EntityPlayer otherPlayer : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
			if (otherPlayer.world == player.world && otherPlayer.getName().equals(name)) {
				return otherPlayer.getPosition();
			}
		}
		return null;
	}
	
	public void doTrilateration(EntityPlayer player) {
		ItemStack stack = getCurrentTrilaterationItem(player);
		Vec3i pos;
		String name = "";
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			
			PointList list = new PointList();
			list.readFromNBT(nbt);
			if (stack.isEmpty()) {
				pos = Vec3i.NULL_VECTOR;
			} else {
				pos = trilaterate(list);
			}
			name = getDisplayName(list.trackingPlayer);
		} else {
			pos = Vec3i.NULL_VECTOR;
		}
		finishedSetting.accept(player, new Tuple<Vec3i, String>(pos, name));
	}
	
	private void printTrilaterationResult(EntityPlayer player, Tuple<Vec3i, String> t) {
		Vec3i pos = t.getFirst();
		String name = t.getSecond();
		if (pos == Vec3i.NULL_VECTOR) {
			player.sendMessage(ServerUtil.getChatForString(Colors.RED + LocalizationHelper.localize("gps:trilaterationfailed")));
		} else {
			player.sendMessage(ServerUtil.getChatForString(Colors.GREEN + LocalizationHelper.localizeFormatted("gps:trilaterationsucceeded", name, pos.getX(), pos.getY(), pos.getZ())));
		}
	}
	
	public Vec3i trilaterate(ItemStack stack) {
		NBTTagCompound nbt;
		if (stack.hasTagCompound()) {
			nbt = stack.getTagCompound();
		} else {
			return Vec3i.NULL_VECTOR;
		}
		PointList list = new PointList();
		list.readFromNBT(nbt);
		return trilaterate(list);
	}
	
	private Vec3i trilaterate(PointList points) {
		if (points.points.size() < 2) {
			return Vec3i.NULL_VECTOR;
		}
		double[][] positions = new double[points.points.size()][3];
		double[] distances = new double[points.points.size()];
		
		for (int i = 0; i < points.points.size(); i++) {
			Tuple<Vec3i, Integer> t = points.points.get(i);
			positions[i][0] = t.getFirst().getX();
			positions[i][1] = t.getFirst().getY();
			positions[i][2] = t.getFirst().getZ();
			distances[i] = t.getSecond();
		}

		NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
		Optimum optimum = solver.solve();

		// the answer
		double[] answer = optimum.getPoint().toArray();
		return new Vec3i(answer[0], answer[1], answer[2]);
	}
	
	public ItemStack getCurrentTrilaterationItem(EntityPlayer player) {
		for (int i = -2; i < player.inventory.mainInventory.size(); i++) {
			ItemStack currentStack;
			if (i < 0) {
				currentStack = player.getHeldItem(EnumHand.values()[i + 2]);
			} else {
				currentStack = player.inventory.mainInventory.get(i);
			}
			
			if (currentStack.getItem() == this) {
				return currentStack;
			}
		}
		return ItemStack.EMPTY;
	}
	
	public static String getDisplayName(String internalName) {
		if (LocalizationHelper.hasLocalization("gps:trilaterate.gui.additionals." + internalName)) {
			return LocalizationHelper.localize("gps:trilaterate.gui.additionals." + internalName);
		}
		return internalName;
	}
	
	private static class PointList {
		private String trackingPlayer = "";
		private List<Tuple<Vec3i, Integer>> points = new ArrayList<>();
		
		public void readFromNBT(NBTTagCompound nbt) {
			trackingPlayer = nbt.getString("player");
			NBTTagList list = nbt.getTagList("points", NBT.TAG_COMPOUND);
			points.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound current = list.getCompoundTagAt(i);
				Vec3i pos = new Vec3i(current.getInteger("x"), current.getInteger("y"), current.getInteger("z"));
				int distance = current.getInteger("distance");
				points.add(new Tuple(pos, distance));
			}
		}
		
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setString("player", trackingPlayer);
			NBTTagList list = new NBTTagList();
			nbt.setTag("points", list);
			for (Tuple<Vec3i, Integer> t : points) {
				NBTTagCompound current = new NBTTagCompound();
				current.setInteger("x", t.getFirst().getX());
				current.setInteger("y", t.getFirst().getY());
				current.setInteger("z", t.getFirst().getZ());
				current.setInteger("distance", t.getSecond());
				list.appendTag(current);
			}
			return nbt;
		}
		
		public void addPoint(Vec3i pos, int distance) {
			points.add(new Tuple<Vec3i, Integer>(pos, distance));
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("PointList: ");
			for (Tuple<Vec3i, Integer> t : points) {
				sb.append("(" + t.getFirst() + ": " + t.getSecond() + "),");
			}
			sb.append(" of player " + trackingPlayer);
			return sb.toString();
		}
		
		public void setTrackingPlayer(String name) {
			if (!trackingPlayer.equals(name)) {
				points.clear();
			}
			trackingPlayer = name;
		}
	}

}
