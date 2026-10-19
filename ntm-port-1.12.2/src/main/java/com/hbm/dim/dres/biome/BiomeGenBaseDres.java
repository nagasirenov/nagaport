/*******************************************************************************
 * Copyright 2015 SteveKunG - More Planets Mod
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package com.hbm.dim.dres.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public abstract class BiomeGenBaseDres extends BiomeGenBaseCelestial {

	public static Biome dresPlains;
	public static Biome dresCanyon;

	public BiomeGenBaseDres(int id, BiomeProperties properties) {
		super(id, properties);

		this.topBlock = ModBlocks.sellafield_slaked;
		this.fillerBlock = ModBlocks.sellafield_slaked;
	}

	@Override
	protected BiomeDecorator createBiomeDecorator() {
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.dres_rock);
		decorator.generateLakes = false;
		return decorator;
	}

}
