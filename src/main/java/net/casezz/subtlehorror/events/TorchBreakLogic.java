package net.casezz.subtlehorror.events;

import net.casezz.subtlehorror.util.ModUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.player.PlayerEntity;

public class TorchBreakLogic {
    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 20; //Checks every 20 seconds
    private static final float CHANCE_PER_CHECK = 0.5f; //0.5% chance
    private static final int SEARCH_RANGE_VERTICAL = 120; //120 blocks vertical search range

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            BlockPos playerChunkOrigin = player.getChunkPos().getStartPos();
            ServerWorld world = (ServerWorld) player.getWorld();
            if (RANDOM.nextFloat() * 100 < CHANCE_PER_CHECK) {
                removeTorchesInArea(world, playerChunkOrigin, player);
            }
        });
    }

    private static void removeTorchesInArea(ServerWorld world, BlockPos chunkOrigin, PlayerEntity player) {
        int torchesBroken = 0;
        boolean foundTorch = false;

        //Iterates over all blocks on the chunk
        for (int y = world.getBottomY(); y < world.getBottomY() + SEARCH_RANGE_VERTICAL; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos currentPos = new BlockPos(chunkOrigin.getX() + x, y, chunkOrigin.getZ() + z);
                    BlockState state = world.getBlockState(currentPos);

                    //Checks if block is a torch
                    if (state.isOf(Blocks.TORCH) || state.isOf(Blocks.WALL_TORCH)) {
                        boolean wasBroken = world.breakBlock(currentPos, true, null);
                        if (wasBroken) {
                            torchesBroken++;
                            foundTorch = true;
                            System.out.println("DEBUG: Torch broken at X:" + currentPos.getX() + ", Y:" + currentPos.getY() + ", Z:" + currentPos.getZ());
                        }
                    }
                }
            }
        }

        // Play cave sound
        if (foundTorch)
            playCaveSound(player);
    }

    private static void playCaveSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.AMBIENT_CAVE,
                SoundCategory.AMBIENT,
                0.8f,
                1.0f
        );
    }
}