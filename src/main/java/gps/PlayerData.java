package gps;

import net.minecraft.util.math.BlockPos;

public class PlayerData {
	public final String username;
	public final int dimension;
	public final BlockPos pos;

	public PlayerData(String username, int dimension, BlockPos pos) {
		this.username = username;
		this.dimension = dimension;
		this.pos = pos;
	}
}
