/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.gametest;

import static net.wimods.freecam.gametest.WiModsTestHelper.*;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.wimods.freecam.WiFreecam;
import net.wimods.freecam.gametest.tests.FreecamTest;

public final class WiFreecamTest implements FabricClientGameTest
{
	public static final Logger LOGGER =
		LoggerFactory.getLogger("WI Freecam Test");
	
	@Override
	public void runTest(ClientGameTestContext context)
	{
		if(!TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER)
			throw new RuntimeException("Network synchronizer is not disabled");
		
		LOGGER.info("Starting WI Freecam Client GameTest");
		hideSplashTexts(context);
		waitForTitleScreenFade(context);
		
		LOGGER.info("Reached title screen");
		context.takeScreenshot("title_screen");
		
		// Check config values that aren't visible in screenshots
		if(!WiFreecam.INSTANCE.getPlausible().isEnabled())
			throw new AssertionError("Plausible should be enabled by default");
		
		LOGGER.info("Creating test world");
		TestWorldBuilder worldBuilder = context.worldBuilder();
		worldBuilder.adjustSettings(creator -> {
			String mcVersion = SharedConstants.getCurrentVersion().name();
			creator.setName("E2E Test " + mcVersion);
			creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
			creator.getGameRules().set(GameRules.SEND_COMMAND_FEEDBACK, false,
				null);
			applyFlatPresetWithSmoothStone(creator);
		});
		
		try(TestSingleplayerContext spContext = worldBuilder.create())
		{
			testInWorld(context, spContext);
			LOGGER.info("Exiting test world");
		}
		
		LOGGER.info("Test complete");
	}
	
	private void testInWorld(ClientGameTestContext context,
		TestSingleplayerContext spContext)
	{
		TestInput input = context.getInput();
		TestClientWorldContext world = spContext.getClientWorld();
		TestServerContext server = spContext.getServer();
		
		// Disable anisotropic filtering
		context.runOnClient(mc -> mc.options.maxAnisotropyBit().set(0));
		
		// Disable chunk fade
		context.runOnClient(mc -> mc.options.chunkSectionFadeInTime().set(0.0));
		
		runCommand(server, "time set noon");
		runCommand(server, "tp 0 -57 0");
		runCommand(server, "fill ~ ~-3 ~ ~ ~-1 ~ smooth_stone");
		runCommand(server, "fill ~-12 ~-3 ~10 ~12 ~9 ~10 smooth_stone");
		
		LOGGER.info("Loading chunks");
		context.waitTicks(2);
		world.waitForChunksRender();
		
		assertScreenshotEquals(context, "in_game",
			"https://i.imgur.com/i2Nr9is.png");
		
		LOGGER.info("Recording debug menu");
		input.pressKey(GLFW.GLFW_KEY_F3);
		context.takeScreenshot("debug_menu");
		input.pressKey(GLFW.GLFW_KEY_F3);
		
		LOGGER.info("Checking for broken mixins");
		MixinEnvironment.getCurrentEnvironment().audit();
		
		LOGGER.info("Opening inventory");
		input.pressKey(GLFW.GLFW_KEY_E);
		assertScreenshotEquals(context, "inventory",
			"https://i.imgur.com/GP74ZNS.png");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		
		new FreecamTest(context, spContext).run();
		TranslationTest.testKeybindTranslationSafety(context);
		
		LOGGER.info("Opening game menu");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.takeScreenshot("game_menu");
		
		LOGGER.info("Clicking Options button");
		for(int i = 0; i < 6; i++)
			input.pressKey(GLFW.GLFW_KEY_TAB);
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		context.takeScreenshot("options_screen");
		
		LOGGER.info("Clicking Controls button");
		for(int i = 0; i < 6; i++)
			input.pressKey(GLFW.GLFW_KEY_TAB);
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		context.takeScreenshot("controls_screen");
		
		LOGGER.info("Clicking Key Binds button");
		input.pressKey(GLFW.GLFW_KEY_TAB);
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		// Select the last keybind in the list
		for(int i = 0; i < 2; i++)
			pressKeyWithModifiers(context, GLFW.GLFW_KEY_TAB,
				GLFW.GLFW_MOD_SHIFT);
		assertScreenshotEquals(context, "freecam_keybind_default",
			"https://i.imgur.com/291TsOi.png");
		
		LOGGER.info("Changing switch control keybind to B");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		input.pressKey(GLFW.GLFW_KEY_B);
		assertScreenshotEquals(context, "switch_control_keybind_changed",
			"https://i.imgur.com/pDgINGm.png");
		
		LOGGER.info("Closing screens");
		for(int i = 0; i < 4; i++)
			input.pressKey(GLFW.GLFW_KEY_ESCAPE);
	}
	
	// because the grass texture is randomized and smooth stone isn't
	private void applyFlatPresetWithSmoothStone(WorldCreationUiState creator)
	{
		FlatLevelGeneratorSettings config = ((FlatLevelSource)creator
			.getSettings().selectedDimensions().overworld()).settings();
		
		List<FlatLayerInfo> layers =
			List.of(new FlatLayerInfo(1, Blocks.BEDROCK),
				new FlatLayerInfo(2, Blocks.DIRT),
				new FlatLayerInfo(1, Blocks.SMOOTH_STONE));
		
		creator.updateDimensions(
			(drm, dorHolder) -> dorHolder.replaceOverworldGenerator(drm,
				new FlatLevelSource(config.withBiomeAndLayers(layers,
					config.structureOverrides(), config.getBiome()))));
	}
}
