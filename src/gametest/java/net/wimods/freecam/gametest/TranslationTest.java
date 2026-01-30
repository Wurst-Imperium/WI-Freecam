/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.gametest;

import static net.wimods.freecam.gametest.WiFreecamTest.*;
import static net.wimods.freecam.gametest.WiModsTestHelper.*;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.wimods.freecam.WiFreecam;

public enum TranslationTest
{
	;
	
	/**
	 * See https://wurst.wiki/sign_translation_vulnerability
	 */
	public static void testKeybindTranslationSafety(
		ClientGameTestContext context)
	{
		LOGGER.info("Testing keybind translation safety");
		
		assertTranslationEquals(context, "key.forward", "Walk Forward");
		assertKeybindEquals(context, "key.forward", "W");
		
		assertTranslationEquals(context, "key.wi_freecam.wi_freecam",
			"key.wi_freecam.wi_freecam");
		
		for(KeyMapping key : WiFreecam.INSTANCE.getKeybinds().getAll())
		{
			assertTranslationEquals(context, key.getName(), key.getName());
			assertKeybindEquals(context, key.getName(), key.getName());
		}
	}
	
	private static void assertTranslationEquals(ClientGameTestContext context,
		String key, String expected)
	{
		String actual = context
			.computeOnClient(mc -> Component.translatable(key).getString());
		if(expected.equals(actual))
			return;
		
		String message = "Expected translatable component '" + key
			+ "' to resolve to '" + expected + "', but got '" + actual + "'";
		ghSummary(message);
		throw new AssertionError(message);
	}
	
	private static void assertKeybindEquals(ClientGameTestContext context,
		String key, String expected)
	{
		String actual =
			context.computeOnClient(mc -> Component.keybind(key).getString());
		if(expected.equals(actual))
			return;
		
		String message = "Expected keybind component '" + key
			+ "' to resolve to '" + expected + "', but got '" + actual + "'";
		ghSummary(message);
		throw new AssertionError(message);
	}
}
