/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.wimods.freecam.clickgui.screens.ClickGuiScreen;

public final class WiFreecamModMenuApi implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> {
			WiFreecam freecam = WiFreecam.INSTANCE;
			freecam.getPlausible().pageview("/config");
			return new ClickGuiScreen(freecam.getGui());
		};
	}
}
