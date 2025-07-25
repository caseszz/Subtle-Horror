package net.casezz.subtlehorror.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DoorOpeningLogic {

    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 60; //Checks every minute
    private static final float CHANCE_PER_CHECK = 3.0f; //3% chance

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
                for (ServerWorld world : server.getWorlds()) {
                    if (world.getRegistryKey() == World.OVERWORLD) {
                        server.getPlayerManager().getPlayerList().forEach(player -> {

                            BlockPos playerChunkOrigin = player.getChunkPos().getStartPos();

                            if (RANDOM.nextFloat() * 100 < CHANCE_PER_CHECK) {
                                openDoorInArea(world, playerChunkOrigin, player);
                            }
                        });
                    }
                }
            }
        });
    }

    private static boolean openDoorInArea(ServerWorld world, BlockPos chunkOrigin, PlayerEntity player) {
        //Iterates over all blocks on the chunk
        int baseY = player.getBlockY() - 4;
        for (int y = baseY; y < baseY + 8; y++){
            for (int x = 0; x < 16; x++){
                for (int z = 0; z < 16; z++){
                    BlockPos currentPos = new BlockPos(chunkOrigin.getX() + x, chunkOrigin.getY() + y, chunkOrigin.getZ() + z);
                    BlockState state = world.getBlockState(currentPos);
                    Block block = state.getBlock();

                    System.out.println("Checking: " + currentPos + " -> " + block.getTranslationKey());

                    //Check if block is a door
                    if (block instanceof DoorBlock) {
                        System.out.println("DEBUG: Door found");
                        Vec3d playerEyePos = player.getEyePos();
                        Vec3d doorCenterPos = Vec3d.ofCenter(currentPos);
                        Vec3d toDoor = doorCenterPos.subtract(playerEyePos).normalize();

                        // Player look direction
                        Vec3d lookVec = player.getRotationVec(1.0F).normalize();

                        // Dot product between look direction and vector to door
                        double dot = lookVec.dotProduct(toDoor);

                        // If player is looking too directly at the door, skip it
                        if (dot > 0.2) {
                            System.out.println("DEBUG: Skipping door because player is looking at it");
                            continue;
                        }
                        BooleanProperty OPEN = DoorBlock.OPEN;

                        if (state.contains(DoorBlock.HALF) && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                            boolean isOpen = state.get(OPEN);
                            world.setBlockState(currentPos, state.with(OPEN, !isOpen), 3);
                            playDoorSound(player);
                            System.out.println("DEBUG: Door opened");
                        }
                        return true;
                    }
                }
            }
        }
        System.out.println("DEBUG: Door not found");
        return false;
    }

    private static void playDoorSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_WOODEN_DOOR_OPEN,
                SoundCategory.BLOCKS,
                0.5f,
                1.0f
        );
        System.out.println("DEBUG: Played sound near: " + player.getName().getString());
    }
}
