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
import net.wimods.freecam.FreecamInitialPosSetting.InitialPosition;
import net.wimods.freecam.FreecamInputSetting.ApplyInputTo;
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
			"https://i.imgur.com/638mC0V.png");
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
			"https://i.imgur.com/UE4MdRm.png");
		assertTrue(
			WiFreecam.INSTANCE.getGui().getWindows().getFirst().isPinned(),
			"Pinning Freecam Settings window didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals("clickgui_window_pinned",
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
		input.setCursorPos(150, 120);
		input.holdMouseFor(GLFW.GLFW_MOUSE_BUTTON_LEFT, 5);
		assertScreenshotEquals("dragging_horizontal_speed_slider",
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
		assertScreenshotEquals("clickgui_scroll_to_change_speed_disabled",
			"https://i.imgur.com/XqzqjIN.png");
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
		assertScreenshotEquals("clickgui_initial_position_dropdown_open",
			"https://i.imgur.com/PScz2kR.png");
		input.setCursorPos(250, 270);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_initial_position_in_front",
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
		assertScreenshotEquals("freecam_start_in_front",
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
		assertScreenshotEquals("clickgui_initial_position_above",
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
		assertScreenshotEquals("freecam_start_above",
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
		assertScreenshotEquals("freecam_moved",
			"https://i.imgur.com/HxrcHbh.png");
		
		// Enable tracer
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 270);
		input.pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
		assertScreenshotEquals("clickgui_tracer_enabled",
			"https://i.imgur.com/8Kh81kn.png");
		assertTrue(WiFreecam.INSTANCE.getSettings().tracer.isChecked(),
			"Enabling tracer didn't work");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals("freecam_tracer",
			"https://i.imgur.com/z3pQumc.png");
		
		// Change tracer color to cyan
		input.pressKey(GLFW.GLFW_KEY_RIGHT_CONTROL);
		context.waitForScreen(ClickGuiScreen.class);
		input.setCursorPos(150, 320);
		assertScreenshotEquals("clickgui_tracer_color_hovered",
			"https://i.imgur.com/PnmCGmV.png");
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
			"https://i.imgur.com/mQSMZWJ.png");
		input.pressKey(GLFW.GLFW_KEY_ESCAPE);
		context.waitForScreen(null);
		assertScreenshotEquals("freecam_tracer_cyan",
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
		assertScreenshotEquals("freecam_player_moved",
			"https://i.imgur.com/mf6NgQl.png");
		WiFreecam.INSTANCE.getSettings().applyInputTo
			.setSelected(ApplyInputTo.CAMERA);
		input.pressKey(GLFW.GLFW_KEY_U);
		context.waitTick();
		world.waitForChunksRender();
		
		// Clean up
		runCommand("fill 0 -58 1 0 -58 2 air");
		runCommand("tp @s 0 -57 0 0 0");
		// Restore body rotation - /tp only rotates the head as of 1.21.11
		context.runOnClient(mc -> mc.player.setYBodyRot(0));
		context.waitTicks(10); // for hand animation
	}
}
