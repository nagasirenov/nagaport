package com.hbm.lib;

import net.minecraft.util.EnumFacing;

/**
 * NTM-local compatibility representation for the six-direction API used by
 * the original 1.7.10 source. Forge/Minecraft-facing methods must use
 * {@link EnumFacing}; convert through {@link #toEnumFacing()} only after
 * checking that a direction is valid.
 */
public enum ForgeDirection {
	DOWN(EnumFacing.DOWN, 0, -1, 0),
	UP(EnumFacing.UP, 0, 1, 0),
	NORTH(EnumFacing.NORTH, 0, 0, -1),
	SOUTH(EnumFacing.SOUTH, 0, 0, 1),
	WEST(EnumFacing.WEST, -1, 0, 0),
	EAST(EnumFacing.EAST, 1, 0, 0),
	UNKNOWN(null, 0, 0, 0);

	/** The six real directions in legacy ordinal order (DOWN through EAST). */
	public static final ForgeDirection[] VALID_DIRECTIONS = {
			DOWN, UP, NORTH, SOUTH, WEST, EAST
	};

	public final int offsetX;
	public final int offsetY;
	public final int offsetZ;
	/** Legacy bit mask; UNKNOWN retains its ordinal bit for Forge 1.7.10 compatibility. */
	public final int flag;
	private final EnumFacing facing;

	ForgeDirection(EnumFacing facing, int offsetX, int offsetY, int offsetZ) {
		this.facing = facing;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.flag = 1 << ordinal();
	}

	/** Returns UNKNOWN for invalid ordinals, matching the legacy API. */
	public static ForgeDirection getOrientation(int ordinal) {
		return ordinal >= 0 && ordinal < VALID_DIRECTIONS.length
				? VALID_DIRECTIONS[ordinal]
				: UNKNOWN;
	}

	/** Converts null and non-directional cases to the NTM UNKNOWN sentinel. */
	public static ForgeDirection fromEnumFacing(EnumFacing facing) {
		if (facing == null) return UNKNOWN;
		switch (facing) {
			case DOWN: return DOWN;
			case UP: return UP;
			case NORTH: return NORTH;
			case SOUTH: return SOUTH;
			case WEST: return WEST;
			case EAST: return EAST;
			default: return UNKNOWN;
		}
	}

	/** Returns null for UNKNOWN; callers must handle that sentinel explicitly. */
	public EnumFacing toEnumFacing() {
		return facing;
	}

	public ForgeDirection getOpposite() {
		switch (this) {
			case DOWN: return UP;
			case UP: return DOWN;
			case NORTH: return SOUTH;
			case SOUTH: return NORTH;
			case WEST: return EAST;
			case EAST: return WEST;
			default: return UNKNOWN;
		}
	}

	/**
	 * Rotates this unit direction by one quarter-turn around the supplied
	 * cardinal axis. UNKNOWN and parallel directions remain unchanged.
	 */
	public ForgeDirection getRotation(ForgeDirection axis) {
		if (this == UNKNOWN || axis == null || axis == UNKNOWN) return this;

		// Legacy ForgeDirection rotates the direction vector by d x axis.
		int x = offsetY * axis.offsetZ - offsetZ * axis.offsetY;
		int y = offsetZ * axis.offsetX - offsetX * axis.offsetZ;
		int z = offsetX * axis.offsetY - offsetY * axis.offsetX;
		if (x == 0 && y == 0 && z == 0) return this;
		if (x == 0 && y == -1 && z == 0) return DOWN;
		if (x == 0 && y == 1 && z == 0) return UP;
		if (x == 0 && y == 0 && z == -1) return NORTH;
		if (x == 0 && y == 0 && z == 1) return SOUTH;
		if (x == -1 && y == 0 && z == 0) return WEST;
		if (x == 1 && y == 0 && z == 0) return EAST;
		return this;
	}
}
