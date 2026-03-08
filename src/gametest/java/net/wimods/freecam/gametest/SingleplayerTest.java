/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.gametest;

import java.nio.file.Path;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public abstract class SingleplayerTest
{
	protected final ClientGameTestContext context;
	protected final TestSingleplayerContext spContext;
	protected final TestInput input;
	protected final TestClientWorldContext world;
	protected final TestServerContext server;
	protected final Logger logger = WiFreecamTest.LOGGER;
	
	public SingleplayerTest(ClientGameTestContext context,
		TestSingleplayerContext spContext)
	{
		this.context = context;
		this.spContext = spContext;
		this.input = context.getInput();
		this.world = spContext.getClientWorld();
		this.server = spContext.getServer();
	}
	
	/**
	 * Runs the test and verifies cleanup afterward.
	 */
	public final void run()
	{
		runImpl();
		assertScreenshotEquals(
			getClass().getSimpleName().toLowerCase() + "_cleanup",
			"https://i.imgur.com/i2Nr9is.png");
	}
	
	/**
	 * Implement the actual test logic here. The test is responsible for
	 * cleaning up after itself.
	 */
	protected abstract void runImpl();
	
	protected final void runCommand(String command)
	{
		WiModsTestHelper.runCommand(server, command);
	}
	
	protected final void assertScreenshotEquals(String fileName,
		String templateUrl)
	{
		WiModsTestHelper.assertScreenshotEquals(context, fileName, templateUrl);
	}
	
	/**
	 * Takes a screenshot, writes a GitHub Actions summary with the screenshot
	 * uploaded to Imgur, and throws a {@link RuntimeException} with the given
	 * error message.
	 */
	protected final void failWithScreenshot(String fileName, String title,
		String errorMessage)
	{
		Path screenshotPath = context.takeScreenshot(fileName);
		
		WiModsTestHelper.ghSummary("### " + title + "\n" + errorMessage + "\n");
		String url = WiModsTestHelper.tryUploadToImgur(screenshotPath);
		if(url != null)
			WiModsTestHelper.ghSummary("![" + fileName + "](" + url + ")");
		else
			WiModsTestHelper.ghSummary("Couldn't upload " + fileName
				+ ".png to Imgur. Check the Test Screenshots.zip artifact.");
		
		throw new RuntimeException(title + ": " + errorMessage);
	}
	
	protected final void clearChat()
	{
		context.runOnClient(mc -> mc.gui.getChat().clearMessages(true));
	}
	
	protected final void clearInventory()
	{
		input.pressKey(GLFW.GLFW_KEY_T);
		input.typeChars("/clear");
		input.pressKey(GLFW.GLFW_KEY_ENTER);
	}
	
	protected final void clearParticles()
	{
		context.runOnClient(mc -> mc.particleEngine.clearParticles());
	}
}
