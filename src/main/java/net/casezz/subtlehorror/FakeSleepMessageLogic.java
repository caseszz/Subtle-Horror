package net.casezz.subtlehorror;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public class FakeSleepMessageLogic {

    private static final Random RANDOM = Random.create();
    private static final float CHANCE_PERCENT = 20.0f;

    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((player, pos) -> {
            if (!player.getWorld().isClient()) {
                MinecraftServer server = player.getWorld().getServer();

                if (server != null && server.isSingleplayer() && server.getPlayerManager().getCurrentPlayerCount() == 1) {
                    if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                        sendFakeSleepingMessage((PlayerEntity) player, server);
                    }
                }
            }
        });
    }

    private static void sendFakeSleepingMessage(PlayerEntity player, MinecraftServer server) {

        Text message = Text.literal("1/")
                .append(Text.literal("2")
                        .append(Text.literal(" players sleeping")));

        for (ServerPlayerEntity playerToSendTo : server.getPlayerManager().getPlayerList()) {
            playerToSendTo.sendMessage(message, true);
        }

        System.out.println("DEBUG: Sent fake sleep message");
    }
}

