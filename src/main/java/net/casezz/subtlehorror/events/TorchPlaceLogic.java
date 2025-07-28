package net.casezz.subtlehorror.events;

import net.casezz.subtlehorror.util.ModUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class TorchPlaceLogic {

    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 60 * 5; //Checks every 5 minutes
    private static final float CHANCE_PER_CHECK = 30.0f; //30% chance
    private static final int CHUNK_RADIUS = 5;
    private static final int MAX_TORCHES_PER_AREA = 5;
    private static final int SEARCH_RANGE_HORIZONTAL = 10;
    private static final int SEARCH_RANGE_VERTICAL = 104;

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
                BlockPos playerChunkOrigin = player.getChunkPos().getStartPos();
                ServerWorld world = (ServerWorld) player.getWorld();
                for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
                    for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
                        // Attempts to put torches on the chunk
                        if (RANDOM.nextFloat() * 100 < CHANCE_PER_CHECK) {
                            BlockPos targetChunkOrigin = playerChunkOrigin.add(x * 16, 0, z * 16);
                            placeTorchesInArea(world, targetChunkOrigin);
                        }
                    }
                }
            }
        });
    }

    private static void placeTorchesInArea(ServerWorld world, BlockPos chunkOrigin) {
        int torchesPlaced = 0;
        for (int i = 0; i < MAX_TORCHES_PER_AREA * 2; i++) {
            if (torchesPlaced >= MAX_TORCHES_PER_AREA) break;

            int xOffset = RANDOM.nextInt(SEARCH_RANGE_HORIZONTAL * 2) - SEARCH_RANGE_HORIZONTAL;
            int zOffset = RANDOM.nextInt(SEARCH_RANGE_HORIZONTAL * 2) - SEARCH_RANGE_HORIZONTAL;
            int yOffset = RANDOM.nextInt(SEARCH_RANGE_VERTICAL) + world.getBottomY();

            BlockPos randomPos = new BlockPos(chunkOrigin.getX() + xOffset, chunkOrigin.getY() + yOffset, chunkOrigin.getZ() + zOffset);

            if (tryPlaceTorchAt(world, randomPos)) {
                torchesPlaced++;
            }
        }
    }

    private static boolean tryPlaceTorchAt(ServerWorld world, BlockPos pos) {
        // Checks if current block is empty
        BlockState currentState = world.getBlockState(pos);
        if (!currentState.isAir() && !currentState.isOf(Blocks.WATER) && !currentState.isOf(Blocks.LAVA)) {
            return false;
        }

        // Attempts to put torch on the ground
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);
        if (belowState.isSolidBlock(world, belowPos) && belowState.isOpaqueFullCube(world, belowPos)) {
            world.setBlockState(pos, Blocks.TORCH.getDefaultState(), 3);
            System.out.println("DEBUG: Floor torch put at X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ());
            return true;
        }

        // Attempts to put on a wall
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos wallPos = pos.offset(direction.getOpposite());
            BlockState wallState = world.getBlockState(wallPos);
            if (wallState.isSolidBlock(world, wallPos) && wallState.isOpaqueFullCube(world, wallPos)) {
                world.setBlockState(pos, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, direction), 3);
                System.out.println("DEBUG: Wall torch put at X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + " (Direção: " + direction.getName() + ")");
                return true;
            }
        }
        return false;
    }
}