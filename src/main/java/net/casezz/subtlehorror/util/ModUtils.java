package net.casezz.subtlehorror.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.function.BiConsumer;

public class ModUtils {

    //Handles ticks checks for every player
    public static void playerTickHandler(int CHECK_INTERVAL_TICKS, BiConsumer<MinecraftServer, PlayerEntity> onTick) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
                //Iterates over every connected player
                for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    onTick.accept(server, player);
                }
            }
        });
    }
}
