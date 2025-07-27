package net.casezz.subtlehorror.sounds;

import net.casezz.subtlehorror.util.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ChestOpenSoundLogic {

    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 20; //Checks every 20 seconds
    private static final float CHANCE_PER_CHECK = 0.5f; //0.5% chance

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
                BlockPos playerChunkOrigin = player.getChunkPos().getStartPos();
                ServerWorld world = (ServerWorld) player.getWorld();
                if (RANDOM.nextFloat() * 100 < CHANCE_PER_CHECK) {
                    chestOpen(world, playerChunkOrigin, player);
                }
            }
        });
    }

    private static boolean chestOpen(ServerWorld world, BlockPos chunkOrigin, PlayerEntity player){
        //Iterates over all nearest blocks on the chunk
        int baseY = player.getBlockY() - 4;
        for (int y = baseY; y < baseY + 8; y++){
            for (int x = 0; x < 16; x++){
                for (int z = 0; z < 16; z++){
                    BlockPos currentPos = new BlockPos(chunkOrigin.getX() + x, chunkOrigin.getY() + y, chunkOrigin.getZ() + z);
                    BlockState state = world.getBlockState(currentPos);
                    Block block = state.getBlock();

                    //Check if block is a chest
                    if (block instanceof ChestBlock) {
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

                        playChestSound(player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void playChestSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_CHEST_OPEN,
                SoundCategory.BLOCKS,
                0.5f,
                1.0f
        );
        System.out.println("DEBUG: Played chest sound near: " + player.getName().getString());
    }
}
