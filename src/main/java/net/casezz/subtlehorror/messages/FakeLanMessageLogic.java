package net.casezz.subtlehorror.messages;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.MinecraftServer;

public class FakeLanMessageLogic {

    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 60 * 10; //Checks every 10 minutes
    private static final float CHANCE_PERCENT = 1.0f; //1% chance

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
                // Iterates over all connected players
                for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    //Check if it's a Singleplayer or LAN world
                    if (isIntegratedServer(server)) {
                        if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                            sendFakeLanMessage(player, server);
                        }
                    }
                }
            }
        });
    }

    private static boolean isIntegratedServer(MinecraftServer server) {
        return server.isSingleplayer();
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
