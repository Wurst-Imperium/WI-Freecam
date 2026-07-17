/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.gametest;

import com.mojang.blaze3d.platform.InputConstants;

import java.nio.file.Path;
import java.util.function.Predicate;

import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SingleplayerTest
{
	protected final ClientGameTestContext context;
	protected final TestSingleplayerContext spContext;
	protected final TestInput input;
	protected final TestClientLevelContext world;
	protected final TestServerContext server;
	protected final Logger logger = WiFreecamTest.LOGGER;
	
	public SingleplayerTest(ClientGameTestContext context,
		TestSingleplayerContext spContext)
	{
		this.context = context;
		this.spContext = spContext;
		this.input = context.getInput();
		this.world = spContext.getClientLevel();
		this.server = spContext.getServer();
	}
	
	/**
	 * Runs the test and verifies cleanup afterward.
	 */
	public final void run()
	{
		runImpl();
		waitForScreenshotMatch(
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
	
	protected final void waitForScreenshotMatch(String fileName,
		String templateUrl)
	{
		WiModsTestHelper.waitForScreenshotMatch(context, fileName, templateUrl);
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
	
	protected final void waitFor(Predicate<Minecraft> predicate,
		String errorMsg)
	{
		waitFor(predicate, ClientGameTestContext.DEFAULT_TIMEOUT, errorMsg);
	}
	
	protected final void waitFor(Predicate<Minecraft> predicate, int timeout,
		String errorMsg)
	{
		try
		{
			context.waitFor(predicate, timeout);
			
		}catch(AssertionError e)
		{
			WiModsTestHelper.ghSummary(errorMsg);
			throw new AssertionError(errorMsg);
		}
	}
	
	protected final void setBlockAndWait(int x, int y, int z, Block block)
	{
		BlockState state = block.defaultBlockState();
		setBlockAndWait(x, y, z, state);
	}
	
	protected final void setBlockAndWait(int x, int y, int z, BlockState state)
	{
		BlockPos pos = new BlockPos(x, y, z);
		server.runOnServer(mc -> mc.getLevel(Level.OVERWORLD).setBlock(pos,
			state, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS));
		context.waitFor(mc -> mc.level.getBlockState(pos) == state);
		world.waitForChunksRender();
	}
	
	protected final void fillAndWait(int x1, int y1, int z1, int x2, int y2,
		int z2, Block block)
	{
		BlockState state = block.defaultBlockState();
		fillAndWait(x1, y1, z1, x2, y2, z2, state);
	}
	
	protected final void fillAndWait(int x1, int y1, int z1, int x2, int y2,
		int z2, BlockState state)
	{
		BlockPos pos1 = new BlockPos(x1, y1, z1);
		BlockPos pos2 = new BlockPos(x2, y2, z2);
		server.runOnServer(mc -> BlockPos.betweenClosedStream(pos1, pos2)
			.forEach(pos -> mc.getLevel(Level.OVERWORLD).setBlock(pos, state,
				Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS)));
		context.waitFor(mc -> BlockPos.betweenClosedStream(pos1, pos2)
			.allMatch(pos -> mc.level.getBlockState(pos) == state));
		world.waitForChunksRender();
	}
	
	protected final void waitForBlock(int relX, int relY, int relZ, Block block)
	{
		context.waitFor(mc -> mc.level
			.getBlockState(mc.player.blockPosition().offset(relX, relY, relZ))
			.getBlock() == block);
	}
	
	protected final void clearChat()
	{
		context.runOnClient(mc -> mc.gui.hud.getChat().clearMessages(true));
	}
	
	protected final void clearInventory()
	{
		input.pressKey(InputConstants.KEY_T);
		input.typeChars("/clear");
		input.pressKey(InputConstants.KEY_RETURN);
	}
	
	protected final void clearParticles()
	{
		context.runOnClient(mc -> mc.particleEngine.clearParticles());
	}
}
