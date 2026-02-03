/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public enum WurstRenderLayers
{
	;
	
	/**
	 * Similar to {@link RenderType#lines()}, but with line width 2.
	 */
	public static final RenderType.CompositeRenderType LINES = RenderType
		.create("wi_freecam:lines", 1536, WurstShaderPipelines.DEPTH_TEST_LINES,
			RenderType.CompositeState.builder()
				.setLineState(
					new RenderStateShard.LineStateShard(OptionalDouble.of(2)))
				.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
				.setOutputState(RenderType.ITEM_ENTITY_TARGET)
				.createCompositeState(false));
	
	/**
	 * Similar to {@link RenderType#lines()}, but with line width 2 and no
	 * depth test.
	 */
	public static final RenderType.CompositeRenderType ESP_LINES = RenderType
		.create("wi_freecam:esp_lines", 1536, WurstShaderPipelines.ESP_LINES,
			RenderType.CompositeState.builder()
				.setLineState(
					new RenderStateShard.LineStateShard(OptionalDouble.of(2)))
				.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
				.setOutputState(RenderType.ITEM_ENTITY_TARGET)
				.createCompositeState(false));
	
	/**
	 * Returns either {@link #LINES} or {@link #ESP_LINES} depending on the
	 * value of {@code depthTest}.
	 */
	public static RenderType.CompositeRenderType getLines(boolean depthTest)
	{
		return depthTest ? LINES : ESP_LINES;
	}
}
