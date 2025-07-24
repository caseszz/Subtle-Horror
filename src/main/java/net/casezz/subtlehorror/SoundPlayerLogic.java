package net.casezz.subtlehorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class SoundPlayerLogic {


    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 60;
    //0.5% chance of sound playing every 60 ticks
    private static final float CHANCE_PERCENT = 0.5f;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Iterates over all players connected at the moment
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.age % CHECK_INTERVAL_TICKS == 0) {
                    if (isPlayerInCave(player)) {
                        if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                            playCaveSound(player);
                        }
                    }
                }
            }
        });
    }

    private static boolean isPlayerInCave(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) {
            return false;
        }

        BlockPos playerPos = player.getBlockPos();

        if (!world.isSkyVisible(playerPos.up())){
            final int MAX_CEILING_CHECK_HEIGHT = 10;
            BlockPos currentCheckPos = playerPos.up(2);

            //Also checks blocks above player
            for (int i = 0; i < MAX_CEILING_CHECK_HEIGHT; i++){
                BlockState blockAtCheckPos = world.getBlockState(currentCheckPos);

                if(!blockAtCheckPos.isAir() && !blockAtCheckPos.isOf(Blocks.WATER) && !blockAtCheckPos.isOf(Blocks.LAVA)){
                    if (blockAtCheckPos.isOf(Blocks.STONE) ||
                    blockAtCheckPos.isOf(Blocks.DEEPSLATE)){
                        return true;
                    }
                }
                currentCheckPos = currentCheckPos.up();
            }
        }
        return false;
    }

    private static void playCaveSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENTITY_PLAYER_BREATH,
                SoundCategory.AMBIENT,
                0.5f,
                1.0f
        );
        System.out.println("DEBUG: Played sound near: " + player.getName().getString());
    }
}
