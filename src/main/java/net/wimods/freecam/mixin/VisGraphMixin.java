/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.wimods.freecam.WiFreecam;

@Mixin(VisGraph.class)
public class VisGraphMixin
{
	/**
	 * Turns off the visibility graph when in Freecam, making things like caves
	 * become visible that would normally be hidden behind other blocks and thus
	 * skipped for better rendering performance.
	 */
	@Inject(at = @At("HEAD"),
		method = "setOpaque(Lnet/minecraft/core/BlockPos;)V",
		cancellable = true)
	private void onSetOpaque(BlockPos pos, CallbackInfo ci)
	{
		if(WiFreecam.INSTANCE.isEnabled())
			ci.cancel();
	}
}
