/*
 * Copyright (c) 2026-2026 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wimods.freecam.gametest.tests;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.ModListPayload;
import net.minecraft.resources.Identifier;
import net.wimods.freecam.gametest.WiFreecamTest;

public final class ModListPrivacyTest
{
	private final ConcurrentLinkedQueue<ModListPayload> payloads =
		new ConcurrentLinkedQueue<>();
	private boolean recording;
	
	public void start()
	{
		recording = true;
		ClientLoginConnectionEvents.INIT.register((listener, _) -> {
			if(!recording)
				return;
				
			// Observe real outgoing packets, including any extra payloads
			// Fabric might send without using ModListPayload.createClient().
			listener.connection.channel.pipeline()
				.addLast(new ChannelOutboundHandlerAdapter()
				{
					@Override
					public void write(ChannelHandlerContext ctx, Object msg,
						ChannelPromise promise) throws Exception
					{
						if(msg instanceof ServerboundCustomPayloadPacket packet
							&& packet
								.payload() instanceof ModListPayload payload)
							payloads.add(payload);
						
						super.write(ctx, msg, promise);
					}
				});
		});
	}
	
	public void finish()
	{
		WiFreecamTest.LOGGER.info("Checking minecraft:mod_list privacy");
		
		if(payloads.isEmpty())
			throw new AssertionError(
				"No minecraft:mod_list payload was observed");
		
		for(ModListPayload payload : payloads)
			for(var entry : payload.entries().entrySet())
			{
				// Don't assume a particular identifier namespace.
				Identifier id = entry.getKey();
				boolean identifiesFreecam = id.getNamespace()
					.equals("wi_freecam")
					|| id.getPath().equals("wi_freecam")
					|| entry.getValue().properties().values().stream()
						.anyMatch(value -> value.equalsIgnoreCase("wi_freecam")
							|| value.equalsIgnoreCase("WI Freecam"));
				if(identifiesFreecam)
					throw new AssertionError(
						"WI Freecam leaked in minecraft:mod_list: " + entry);
			}
		
		recording = false;
	}
}
