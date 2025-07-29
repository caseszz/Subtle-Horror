package net.casezz.subtlehorror.events;

import net.casezz.subtlehorror.util.ModUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

import static net.casezz.subtlehorror.util.ModUtils.isPlayerInCave;

public class LootDropEventLogic {
    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 20; //Checks every 20 seconds
    private static final float CHANCE_PER_CHECK = 0.5f; //0.5% chance
    private static final int VERTICAL_SEARCH_RADIUS = 5;

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            if (RANDOM.nextFloat() * 100 < CHANCE_PER_CHECK) {
                ServerWorld world = (ServerWorld) player.getWorld();
                BlockPos playerPos = player.getBlockPos();
                triggerLoopDropEvent(world, playerPos, player);
            }
        });
    }

    public static boolean triggerLoopDropEvent(ServerWorld world, BlockPos playerPos ,PlayerEntity player){
        List<ItemStack> itemsToDrop = new ArrayList<>();
        
        if (isPlayerInCave(player) || world.isNight()){
            itemsToDrop = getRandomHostileLootGroup();
        }
        else {
            itemsToDrop = getRandomNormalLootGroup();
        }

        if (!itemsToDrop.isEmpty()){
            BlockPos dropPos = findSafeDropPos(world, player, playerPos);
            if (dropPos != null){
                for(ItemStack itemStack : itemsToDrop) {
                    spawnItemEntity(world, dropPos, itemStack);
                }
                System.out.println("DEBUG: Loot dropped at " + dropPos.toShortString() + ".");
                return true;

            }
            System.out.println("DEBUG: No positions found for loot drop");
            return false;

        }
        return false;
    }

    @FunctionalInterface
    private interface LootGroupGenerator {
        List<ItemStack> generate();
    }

    //Hostile Mobs item drops
    private static List<ItemStack> getRandomHostileLootGroup() {
        LootGroupGenerator[] hostileLootGenerators = {
                //Zombie
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.ROTTEN_FLESH, RANDOM.nextInt(3) + 1));
                    return loot;
                },
                //Skeleton
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.BONE, RANDOM.nextInt(2) + 1));
                    if (RANDOM.nextBoolean()) {
                        loot.add(new ItemStack(Items.ARROW, RANDOM.nextInt(2) + 1));
                    }
                    return loot;
                },
                //Spider
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.STRING, RANDOM.nextInt(2) + 1));
                    if (RANDOM.nextBoolean()) {
                        loot.add(new ItemStack(Items.SPIDER_EYE));
                    }
                    return loot;
                },
                //Creeper
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.GUNPOWDER, RANDOM.nextInt(3) + 1));
                    return loot;
                }
        };
        return hostileLootGenerators[RANDOM.nextInt(hostileLootGenerators.length)].generate();
    }

    //Normal Mobs item drops
    private static List<ItemStack> getRandomNormalLootGroup() {
        LootGroupGenerator[] normalLootGenerators = {
                //Chicken
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.FEATHER,RANDOM.nextInt(2) + 1));
                    if (RANDOM.nextBoolean()) {
                        loot.add(new ItemStack(Items.CHICKEN));
                    }
                    return loot;
                },
                //Pig
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.PORKCHOP, RANDOM.nextInt(3) + 1));
                    return loot;
                },
                //Cow
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    loot.add(new ItemStack(Items.BEEF, RANDOM.nextInt(3) + 1));
                    if (RANDOM.nextBoolean()) {
                        loot.add(new ItemStack(Items.LEATHER));
                    }
                    return loot;
                },
                //Sheep
                () -> {
                    List<ItemStack> loot = new ArrayList<>();
                    Item[] woolColors = {Items.WHITE_WOOL, Items.GRAY_WOOL, Items.BLACK_WOOL};
                    loot.add(new ItemStack(woolColors[RANDOM.nextInt(woolColors.length)]));
                    if (RANDOM.nextBoolean()) {
                        loot.add(new ItemStack(Items.MUTTON));
                    }
                    return loot;
                }
        };
        return normalLootGenerators[RANDOM.nextInt(normalLootGenerators.length)].generate();
    }

    //Searches for a valid loot drop position
    private static BlockPos findSafeDropPos(ServerWorld world, PlayerEntity player, BlockPos playerChunkOrigin) {
        List<BlockPos> validDropPositions = new ArrayList<>();

        int playerY = player.getBlockPos().getY();
        int minYSearch = playerY - VERTICAL_SEARCH_RADIUS;
        int maxYSearch = playerY + VERTICAL_SEARCH_RADIUS;

        minYSearch = Math.max(minYSearch, world.getBottomY());
        maxYSearch = Math.min(maxYSearch, world.getTopY());


        //Iterates over blocks of the chunk
        for (int y = minYSearch; y <= maxYSearch; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos currentPos = new BlockPos(playerChunkOrigin.getX() + x, y, playerChunkOrigin.getZ() + z);

                    //Verifies if chunk is loaded
                    if (!world.isChunkLoaded(currentPos)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(currentPos);
                    if (state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA)) {
                        BlockState blockBelow = world.getBlockState(currentPos.down());
                        if (blockBelow.isSolidBlock(world, currentPos.down())) {
                            if (!isLineOfSightClear(world, player, currentPos)) {
                                validDropPositions.add(currentPos);
                            }
                        }
                    }
                }
            }
        }

        if (!validDropPositions.isEmpty()) {
            return validDropPositions.get(RANDOM.nextInt(validDropPositions.size()));
        } else {
            return null;
        }
    }


    //Checks if potential loot spawn coordinate is on the player's vision
    private static boolean isLineOfSightClear(ServerWorld world, PlayerEntity player, BlockPos targetPos) {
        Vec3d eyePos = player.getEyePos();
        Vec3d targetCenter = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5);

        RaycastContext raycastContext = new RaycastContext(
                eyePos,
                targetCenter,
                RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,
                player
        );

        BlockHitResult hitResult = world.raycast(raycastContext);

        return hitResult.getType() == HitResult.Type.MISS || hitResult.getBlockPos().equals(targetPos);
    }

    //Spawns items
    public static void spawnItemEntity(ServerWorld world, BlockPos pos, ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);

        //Varies position a little
        itemEntity.setVelocity(
                (RANDOM.nextDouble() * 0.2 - 0.1),
                (RANDOM.nextDouble() * 0.2 + 0.1),
                (RANDOM.nextDouble() * 0.2 - 0.1)
        );

        world.spawnEntity(itemEntity);

        System.out.println("DEBUG LOOT: Item '" + itemStack.getItem().getName().getString() + "' spawned on X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + " (quantity: " + itemStack.getCount() + ").");
    }
}
