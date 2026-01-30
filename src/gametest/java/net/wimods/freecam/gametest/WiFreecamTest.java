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
import net.wimods.freecam.FreecamInitialPosSetting.InitialPosition;
import net.wimods.freecam.FreecamInputSetting.ApplyInputTo;
import net.wimods.freecam.WiFreecam;
import net.wimods.freecam.clickgui.screens.ClickGuiScreen;
import net.wimods.freecam.clickgui.screens.EditColorScreen;
import net.wimods.freecam.clickgui.screens.EditSliderScreen;

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
		
		testFreecam(context, spContext);
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
	
	private void testFreecam(ClientGameTestContext context,
		TestSingleplayerContext spContext)
	{
		LOGGER.info("Testing Freecam");
		TestInput input = context.getInput();
		TestClientWorldContext world = spContext.getClientWorld();
		TestServerContext server = spContext.getServer();
		
		// Enable Freecam with default settings
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		assertScreenshotEquals(context, "freecam_start_inside",
			"https://i.imgur.com/jdSno3u.png");
		
		// Open ClickGUI with /freecam
		input.pressKey(GLFW.GLFW_KEY_T);
		input.typeChars("/freecam");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		context.waitForScreen(ClickGuiScreen.class);
		assertScreenshotEquals(context, "clickgui_screen",
			"https://i.imgur.com/638mC0V.png");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Minimize Freecam Settings window
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(270, 40);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context, "clickgui_minimize_button_clicked",
			"https://i.imgur.com/WGL2qoq.png");
		assertTrue(
			WiFreecam.INSTANCE.getGui().getWindows().getFirst().isMinimized(),
			"Minimizing Freecam Settings window didn't work");
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertTrue(
			!WiFreecam.INSTANCE.getGui().getWindows().getFirst().isMinimized(),
			"Un-minimizing Freecam Settings window didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Pin Freecam Settings window
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(290, 40);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context, "clickgui_pin_button_clicked",
			"https://i.imgur.com/UE4MdRm.png");
		assertTrue(
			WiFreecam.INSTANCE.getGui().getWindows().getFirst().isPinned(),
			"Pinning Freecam Settings window didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals(context, "clickgui_window_pinned",
			"https://i.imgur.com/rnwzIjX.png");
		
		// Unpin Freecam Settings window
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(290, 40);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertTrue(
			!WiFreecam.INSTANCE.getGui().getWindows().getFirst().isPinned(),
			"Unpinning Freecam Settings window didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Scroll to change speed
		input.scroll(1);
		context.waitTick();
		assertScreenshotEquals(context, "freecam_speed_scrolled",
			"https://i.imgur.com/DysLqZw.png");
		assertEquals(
			context.computeOnClient(
				mc -> mc.player.getInventory().getSelectedSlot()),
			0,
			"Scrolling while using Freecam with \"Scroll to change speed\" enabled changed the selected slot");
		
		// Change speed slider using normal mouse click
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 120);
		input.holdMouseFor(GLFW.GLFW_MOUSE_BUTTON_LEFT, 5);
		assertScreenshotEquals(context, "dragging_horizontal_speed_slider",
			"https://i.imgur.com/EcR7Iku.png");
		assertEquals(
			WiFreecam.INSTANCE.getSettings().horizontalSpeed.getValue(), 4.7,
			"Changing horizontal speed slider using normal mouse click didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Change speed slider using precise input
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 120);
		pressMouseWithModifiers(context, GLFW.GLFW_MOUSE_BUTTON_LEFT,
			GLFW.GLFW_MOD_CONTROL);
		context.waitForScreen(EditSliderScreen.class);
		assertScreenshotEquals(context, "edit_horizontal_speed_screen",
			"https://i.imgur.com/F8qpnqX.png");
		input.typeChars("5");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		context.waitForScreen(ClickGuiScreen.class);
		assertEquals(
			WiFreecam.INSTANCE.getSettings().horizontalSpeed.getValue(), 5,
			"Changing horizontal speed slider using precise input didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Reset horizontal speed slider
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 120);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		assertEquals(
			WiFreecam.INSTANCE.getSettings().horizontalSpeed.getValue(), 1,
			"Right click resetting horizontal speed slider didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Turn off "Scroll to change speed"
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 190);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context,
			"clickgui_scroll_to_change_speed_disabled",
			"https://i.imgur.com/XqzqjIN.png");
		assertTrue(
			!WiFreecam.INSTANCE.getSettings().scrollToChangeSpeed.isChecked(),
			"Turning off \"Scroll to change speed\" didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Scroll to change selected slot
		input.scroll(1);
		context.waitTick();
		assertScreenshotEquals(context, "freecam_hotbar_scrolled",
			"https://i.imgur.com/edjDUxr.png");
		assertEquals(
			context.computeOnClient(
				mc -> mc.player.getInventory().getSelectedSlot()),
			8,
			"Scrolling while using Freecam with \"Scroll to change speed\" disabled didn't change the selected slot");
		context.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
		
		// Reset "Scroll to change speed" checkbox
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 190);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		assertTrue(
			WiFreecam.INSTANCE.getSettings().scrollToChangeSpeed.isChecked(),
			"Right click resetting \"Scroll to change speed\" didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Change "Initial position" dropdown to "In front"
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(250, 240);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context,
			"clickgui_initial_position_dropdown_open",
			"https://i.imgur.com/PScz2kR.png");
		input.setCursorPos(250, 270);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context, "clickgui_initial_position_in_front",
			"https://i.imgur.com/h8XNFLX.png");
		assertTrue(
			WiFreecam.INSTANCE.getSettings().initialPos
				.getSelected() == InitialPosition.IN_FRONT,
			"Changing \"Initial position\" dropdown to \"In front\" didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Enable Freecam with initial position in front
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		assertScreenshotEquals(context, "freecam_start_in_front",
			"https://i.imgur.com/nrMP191.png");
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Change "Initial position" dropdown to "Above"
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(250, 240);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.takeScreenshot("clickgui_initial_position_dropdown_open_again");
		input.setCursorPos(250, 290);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context, "clickgui_initial_position_above",
			"https://i.imgur.com/Eibc0HO.png");
		assertTrue(
			WiFreecam.INSTANCE.getSettings().initialPos
				.getSelected() == InitialPosition.ABOVE,
			"Changing \"Initial position\" dropdown to \"Above\" didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Enable Freecam with initial position above
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		assertScreenshotEquals(context, "freecam_start_above",
			"https://i.imgur.com/3LbAtRj.png");
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Reset "Initial position" dropdown
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(250, 240);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		assertTrue(
			WiFreecam.INSTANCE.getSettings().initialPos
				.getSelected() == InitialPosition.INSIDE,
			"Right click resetting \"Initial position\" dropdown didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Enable Freecam, then fly back and up a bit
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		input.holdKeyFor(GLFW.GLFW_KEY_S, 2);
		input.holdKeyFor(GLFW.GLFW_KEY_SPACE, 1);
		assertScreenshotEquals(context, "freecam_moved",
			"https://i.imgur.com/HxrcHbh.png");
		
		// Enable tracer
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 270);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals(context, "clickgui_tracer_enabled",
			"https://i.imgur.com/8Kh81kn.png");
		assertTrue(WiFreecam.INSTANCE.getSettings().tracer.isChecked(),
			"Enabling tracer didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals(context, "freecam_tracer",
			"https://i.imgur.com/z3pQumc.png");
		
		// Change tracer color to cyan
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 320);
		assertScreenshotEquals(context, "clickgui_tracer_color_hovered",
			"https://i.imgur.com/PnmCGmV.png");
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.waitForScreen(EditColorScreen.class);
		assertScreenshotEquals(context, "edit_color_screen",
			"https://i.imgur.com/FtivKXs.png");
		input.typeChars("00FFFF");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		assertTrue(
			WiFreecam.INSTANCE.getSettings().color.getColorI() == 0xFF00FFFF,
			"Changing tracer color to cyan didn't work");
		context.waitForScreen(ClickGuiScreen.class);
		assertScreenshotEquals(context, "clickgui_tracer_color_changed",
			"https://i.imgur.com/mQSMZWJ.png");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals(context, "freecam_tracer_cyan",
			"https://i.imgur.com/Utkf8ld.png");
		
		// Reset tracer color
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 320);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		assertTrue(
			WiFreecam.INSTANCE.getSettings().color.getColorI() == 0xFFFFFFFF,
			"Resetting tracer color didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Disable tracer and un-hide hand
		WiFreecam.INSTANCE.getSettings().tracer.setChecked(false);
		WiFreecam.INSTANCE.getSettings().hideHand.setChecked(false);
		context.waitTick();
		assertScreenshotEquals(context, "freecam_with_hand",
			"https://i.imgur.com/6tahHsE.png");
		WiFreecam.INSTANCE.getSettings().hideHand.setChecked(true);
		
		// Enable player movement, walk forward, and turn around
		runCommand(server, "fill 0 -58 1 0 -58 2 smooth_stone");
		WiFreecam.INSTANCE.getSettings().applyInputTo
			.setSelected(ApplyInputTo.PLAYER);
		input.holdKeyFor(GLFW.GLFW_KEY_W, 10);
		for(int i = 0; i < 10; i++)
		{
			input.moveCursor(120, 0);
			context.waitTick();
		}
		context.waitTick();
		assertScreenshotEquals(context, "freecam_player_moved",
			"https://i.imgur.com/mf6NgQl.png");
		WiFreecam.INSTANCE.getSettings().applyInputTo
			.setSelected(ApplyInputTo.CAMERA);
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Clean up
		runCommand(server, "fill 0 -58 1 0 -58 2 air");
		runCommand(server, "tp @s 0 -57 0 0 0");
		context.waitTicks(2);
		world.waitForChunksRender();
		// Restore body rotation - /tp only rotates the head as of 1.21.11
		context.runOnClient(mc -> mc.player.setYBodyRot(0));
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
