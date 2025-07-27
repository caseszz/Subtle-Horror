package net.casezz.subtlehorror.messages;

import net.casezz.subtlehorror.util.ModUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.MinecraftServer;

public class FakeLanMessageLogic {

    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 10; //Checks every 10 seconds
    private static final float CHANCE_PERCENT = 0.5f; //0.5% chance

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            //Check if it's a Singleplayer or LAN world
            if (server.isSingleplayer()) {
                if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                    sendFakeLanMessage(player, server);
                }
            }
        });
    }

    private static void sendFakeLanMessage(PlayerEntity player, MinecraftServer server) {
        int fakePort = generateFakePort(); //Random fake port number

        Text message = Text.literal("Local game hosted on port ")
                .append(Text.literal("["))
                .append(Text.literal(String.valueOf(fakePort)).formatted(Formatting.GREEN))
                .append(Text.literal("]"));

        player.sendMessage(message, false);
        System.out.println("DEBUG: Fake LAN sent to " + player.getName().getString() + " on port " + fakePort);
    }

    private static int generateFakePort() {
        return RANDOM.nextInt(65535 - 49152 + 1) + 49152;
    }
}
