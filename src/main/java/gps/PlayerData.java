/**
 * The code of the GPS mod and all related materials like textures is licensed under the
 * GNU GENERAL PUBLIC LICENSE Version 3.
 * <p>
 * See https://github.com/Aroma1997/GPS/blob/master/license.txt for more information.
 */
package gps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class PlayerData {
	public final String username;
	public final int dimension;
	//TODO: only sync the distance. Having the position available defeats the purpose of trilaterating.
	public final BlockPos pos;

	public PlayerData(String username, int dimension, BlockPos pos) {
		this.username = username;
		this.dimension = dimension;
		this.pos = pos;
	}

	public double getDistance(Vec3i other) {
		return Math.sqrt(pos.distanceSq(other));
	}
}
