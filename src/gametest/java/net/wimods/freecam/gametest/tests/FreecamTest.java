/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.gametest.tests;

import static net.wimods.freecam.gametest.WiModsTestHelper.*;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.wimods.freecam.FreecamInitialPosSetting.InitialPosition;
import net.wimods.freecam.FreecamInputSetting.ApplyInputTo;
import net.wimods.freecam.FreecamInteractionSetting.InteractFrom;
import net.wimods.freecam.WiFreecam;
import net.wimods.freecam.clickgui.screens.ClickGuiScreen;
import net.wimods.freecam.clickgui.screens.EditColorScreen;
import net.wimods.freecam.clickgui.screens.EditSliderScreen;
import net.wimods.freecam.gametest.SingleplayerTest;

public final class FreecamTest extends SingleplayerTest
{
	public FreecamTest(ClientGameTestContext context,
		TestSingleplayerContext spContext)
	{
		super(context, spContext);
	}
	
	@Override
	protected void runImpl()
	{
		logger.info("Testing Freecam");
		
		// Enable Freecam with default settings
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		assertScreenshotEquals("freecam_start_inside",
			"https://i.imgur.com/jdSno3u.png");
		
		// Open ClickGUI with /freecam
		input.pressKey(GLFW.GLFW_KEY_T);
		input.typeChars("/freecam");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		context.waitForScreen(ClickGuiScreen.class);
		assertScreenshotEquals("clickgui_screen",
			"https://i.imgur.com/rcdRH5Y.png");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Minimize Freecam Settings window
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(270, 40);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_minimize_button_clicked",
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
		assertScreenshotEquals("clickgui_pin_button_clicked",
			"https://i.imgur.com/08anClQ.png");
		assertTrue(
			WiFreecam.INSTANCE.getGui().getWindows().getFirst().isPinned(),
			"Pinning Freecam Settings window didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals("clickgui_window_pinned",
			"https://i.imgur.com/zLrcTts.png");
		
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
		assertScreenshotEquals("freecam_speed_scrolled",
			"https://i.imgur.com/DysLqZw.png");
		assertEquals(
			context.computeOnClient(
				mc -> mc.player.getInventory().getSelectedSlot()),
			0,
			"Scrolling while using Freecam with \"Scroll to change speed\" enabled changed the selected slot");
		
		// Change speed slider using normal mouse click
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 146);
		input.holdMouseFor(GLFW.GLFW_MOUSE_BUTTON_LEFT, 5);
		assertScreenshotEquals("dragging_horizontal_speed_slider",
			"https://i.imgur.com/tbRrEBD.png");
		assertEquals(
			WiFreecam.INSTANCE.getSettings().horizontalSpeed.getValue(), 4.7,
			"Changing horizontal speed slider using normal mouse click didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Change speed slider using precise input
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 146);
		pressMouseWithModifiers(context, GLFW.GLFW_MOUSE_BUTTON_LEFT,
			GLFW.GLFW_MOD_CONTROL);
		context.waitForScreen(EditSliderScreen.class);
		assertScreenshotEquals("edit_horizontal_speed_screen",
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
		input.setCursorPos(150, 146);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		assertEquals(
			WiFreecam.INSTANCE.getSettings().horizontalSpeed.getValue(), 1,
			"Right click resetting horizontal speed slider didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Turn off "Scroll to change speed"
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 216);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_scroll_to_change_speed_disabled",
			"https://i.imgur.com/EWgRwoi.png");
		assertTrue(
			!WiFreecam.INSTANCE.getSettings().scrollToChangeSpeed.isChecked(),
			"Turning off \"Scroll to change speed\" didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		
		// Scroll to change selected slot
		input.scroll(1);
		context.waitTick();
		assertScreenshotEquals("freecam_hotbar_scrolled",
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
		input.setCursorPos(150, 216);
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
		input.setCursorPos(250, 266);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_initial_position_dropdown_open",
			"https://i.imgur.com/Lc6gkLw.png");
		input.setCursorPos(250, 296);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_initial_position_in_front",
			"https://i.imgur.com/hjSGgNS.png");
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
		assertScreenshotEquals("freecam_start_in_front",
			"https://i.imgur.com/nrMP191.png");
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Change "Initial position" dropdown to "Above"
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(250, 266);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.takeScreenshot("clickgui_initial_position_dropdown_open_again");
		input.setCursorPos(250, 316);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_initial_position_above",
			"https://i.imgur.com/s45CRIa.png");
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
		assertScreenshotEquals("freecam_start_above",
			"https://i.imgur.com/3LbAtRj.png");
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Reset "Initial position" dropdown
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(250, 266);
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
		context.waitTick();
		assertScreenshotEquals("freecam_moved",
			"https://i.imgur.com/SQPSG5S.png");
		
		// Enable tracer
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 296);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_tracer_enabled",
			"https://i.imgur.com/YmqoXis.png");
		assertTrue(WiFreecam.INSTANCE.getSettings().tracer.isChecked(),
			"Enabling tracer didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals("freecam_tracer",
			"https://i.imgur.com/z3pQumc.png");
		
		// Change tracer color to cyan
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 346);
		assertScreenshotEquals("clickgui_tracer_color_hovered",
			"https://i.imgur.com/8gPCt3G.png");
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.waitForScreen(EditColorScreen.class);
		assertScreenshotEquals("edit_color_screen",
			"https://i.imgur.com/FtivKXs.png");
		input.typeChars("00FFFF");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
		assertTrue(
			WiFreecam.INSTANCE.getSettings().color.getColorI() == 0xFF00FFFF,
			"Changing tracer color to cyan didn't work");
		context.waitForScreen(ClickGuiScreen.class);
		assertScreenshotEquals("clickgui_tracer_color_changed",
			"https://i.imgur.com/5FRIZFm.png");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals("freecam_tracer_cyan",
			"https://i.imgur.com/Utkf8ld.png");
		
		// Reset tracer color
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 346);
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
		assertScreenshotEquals("freecam_with_hand",
			"https://i.imgur.com/6tahHsE.png");
		WiFreecam.INSTANCE.getSettings().hideHand.setChecked(true);
		
		// Enable player movement, walk forward, and turn around
		runCommand("fill 0 -58 1 0 -58 2 smooth_stone");
		WiFreecam.INSTANCE.getSettings().applyInputTo
			.setSelected(ApplyInputTo.PLAYER);
		input.holdKeyFor(GLFW.GLFW_KEY_W, 10);
		for(int i = 0; i < 10; i++)
		{
			input.moveCursor(120, 0);
			context.waitTick();
		}
		context.waitTick();
		// Open and close chat to reset cursor position
		input.pressKey(GLFW.GLFW_KEY_T);
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		assertScreenshotEquals("freecam_player_moved",
			"https://i.imgur.com/mf6NgQl.png");
		WiFreecam.INSTANCE.getSettings().applyInputTo
			.setSelected(ApplyInputTo.CAMERA);
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Reset player and remove walkway
		runCommand("fill 0 -58 1 0 -58 2 air");
		runCommand("tp @s 0 -57 0 0 0");
		// Restore body rotation - /tp only rotates the head as of 1.21.11
		context.runOnClient(mc -> mc.player.setYBodyRot(0));
		
		// Test "Interact from" setting
		runCommand("setblock 0 -56 2 smooth_stone");
		waitForBlock(0, 1, 2, Blocks.SMOOTH_STONE);
		runCommand("setblock 0 -56 1 lever[face=wall,facing=north]");
		runCommand("setblock 0 -56 3 lever[face=wall,facing=south]");
		waitForBlock(0, 1, 3, Blocks.LEVER);
		context.waitTick();
		world.waitForChunksRender();
		context.takeScreenshot("freecam_interact_setup");
		
		// Enable Freecam and fly to a side view
		WiFreecam.INSTANCE.getSettings().horizontalSpeed.setValue(0.95);
		input.pressKey(GLFW.GLFW_KEY_U);
		input.holdKeyFor(GLFW.GLFW_KEY_W, 3);
		context.waitTick();
		WiFreecam.INSTANCE.getSettings().horizontalSpeed.setValue(1);
		for(int i = 0; i < 6; i++)
		{
			input.moveCursor(120, 0);
			context.waitTick();
		}
		input.holdKeyFor(GLFW.GLFW_KEY_S, 2);
		context.waitTick();
		world.waitForChunksRender();
		context.takeScreenshot("freecam_interact_side_view");
		
		// Right click with "Interact from: Player"
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		context.waitTick();
		assertLeverState(0, -56, 1, true, "near lever, player mode");
		assertLeverState(0, -56, 3, false, "far lever, player mode");
		
		// Right click with "Interact from: Camera"
		WiFreecam.INSTANCE.getSettings().interactFrom
			.setSelected(InteractFrom.CAMERA);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
		context.waitTick();
		assertLeverState(0, -56, 3, true, "far lever, camera mode");
		assertLeverState(0, -56, 1, true, "near lever, camera mode");
		
		// Replace levers with chickens
		runCommand("fill 0 -56 1 0 -56 3 air strict");
		Chicken nearChicken = spawnChicken(1.5);
		Chicken farChicken = spawnChicken(3.5);
		clearParticles();
		context.waitTick();
		
		// Left click with "Interact from: Player"
		WiFreecam.INSTANCE.getSettings().interactFrom
			.setSelected(InteractFrom.PLAYER);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.waitTick();
		assertChickenHealth(nearChicken, true, "near chicken, player mode");
		assertChickenHealth(farChicken, false, "far chicken, player mode");
		
		// Left click with "Interact from: Camera"
		nearChicken.discard();
		nearChicken = spawnChicken(1.5);
		context.waitTick();
		WiFreecam.INSTANCE.getSettings().interactFrom
			.setSelected(InteractFrom.CAMERA);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		context.waitTick();
		assertChickenHealth(farChicken, true, "far chicken, camera mode");
		assertChickenHealth(nearChicken, false, "near chicken, camera mode");
		
		// Clean up
		nearChicken.discard();
		farChicken.discard();
		WiFreecam.INSTANCE.getSettings().interactFrom
			.setSelected(InteractFrom.PLAYER);
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTicks(3);
	}
	
	private Chicken spawnChicken(double z)
	{
		return server.computeOnServer(s -> {
			Chicken c = EntityTypes.CHICKEN.create(s.overworld(),
				EntitySpawnReason.COMMAND);
			c.setPos(0.5, -56, z);
			c.setNoAi(true);
			c.setNoGravity(true);
			s.overworld().addFreshEntity(c);
			return c;
		});
	}
	
	private void assertLeverState(int x, int y, int z, boolean expectedPowered,
		String description)
	{
		BlockState state = server.computeOnServer(
			s -> s.overworld().getBlockState(new BlockPos(x, y, z)));
		
		String errorMessage = null;
		if(state.getBlock() != Blocks.LEVER)
			errorMessage = "Expected lever at " + x + ", " + y + ", " + z
				+ description + ") but found " + state;
		else if(state.getValue(LeverBlock.POWERED) != expectedPowered)
			errorMessage = "Lever at " + x + ", " + y + ", " + z + " ("
				+ description + ") expected powered=" + expectedPowered
				+ " but was powered=" + !expectedPowered;
		
		if(errorMessage == null)
			return;
		
		failWithScreenshot("freecam_block_interaction_failed",
			"Freecam block interaction test failed", errorMessage);
	}
	
	private void assertChickenHealth(Chicken chicken, boolean expectedDamaged,
		String description)
	{
		float health = chicken.getHealth();
		boolean isDamaged = health < 4.0f;
		if(isDamaged == expectedDamaged)
			return;
		
		String errorMessage = "Chicken (" + description + ") expected "
			+ (expectedDamaged ? "damaged" : "full health") + health;
		
		failWithScreenshot("freecam_entity_interaction_failed",
			"Freecam entity interaction test failed", errorMessage);
	}
}
