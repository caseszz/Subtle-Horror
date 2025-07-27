package net.casezz.subtlehorror.events;

import net.casezz.subtlehorror.util.ModUtils;
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
    private static final int CHECK_INTERVAL_TICKS = 20 * 20; //Checks every 20 seconds
    private static final float CHANCE_PER_CHECK = 0.5f; //0.5% chance

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
                BlockPos playerChunkOrigin = player.getChunkPos().getStartPos();
                ServerWorld world = (ServerWorld) player.getWorld();

                if (RANDOM.nextFloat() * 100 < CHANCE_PER_CHECK) {
                    openDoorInArea(world, playerChunkOrigin, player);
                }
            }
        });
    }

    public static boolean openDoorInArea(ServerWorld world, BlockPos chunkOrigin, PlayerEntity player) {
        //Iterates over all nearest blocks on the chunk
        int baseY = player.getBlockY() - 4;
        for (int y = baseY; y < baseY + 8; y++){
            for (int x = 0; x < 16; x++){
                for (int z = 0; z < 16; z++){
                    BlockPos currentPos = new BlockPos(chunkOrigin.getX() + x, chunkOrigin.getY() + y, chunkOrigin.getZ() + z);
                    BlockState state = world.getBlockState(currentPos);
                    Block block = state.getBlock();

                    //Check if block is a door
                    if (block instanceof DoorBlock) {
                        Vec3d playerEyePos = player.getEyePos();
                        Vec3d doorCenterPos = Vec3d.ofCenter(currentPos);
                        Vec3d toDoor = doorCenterPos.subtract(playerEyePos).normalize();

                        // Player look direction
                        Vec3d lookVec = player.getRotationVec(1.0F).normalize();

                        // Dot product between look direction and vector to door
                        double dot = lookVec.dotProduct(toDoor);

                        // If player is looking at the door, skip it
                        if (dot > 0.2) {
                            continue;
                        }
                        BooleanProperty OPEN = DoorBlock.OPEN;

                        //Opens door
                        if (state.contains(DoorBlock.HALF) && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                            boolean isOpen = state.get(OPEN);
                            world.setBlockState(currentPos, state.with(OPEN, !isOpen), 3);
                            playDoorSound(player);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void playDoorSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_WOODEN_DOOR_OPEN, //Maybe will change to other doors sounds in future updates
                SoundCategory.BLOCKS,
                0.5f,
                1.0f
        );
        System.out.println("DEBUG: Opened door and played sound near: " + player.getName().getString());
    }
}
