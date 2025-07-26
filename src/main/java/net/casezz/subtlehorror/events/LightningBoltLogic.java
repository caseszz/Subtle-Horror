package net.casezz.subtlehorror.events;

import net.casezz.subtlehorror.util.ModUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class LightningBoltLogic {

    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 60 * 10; //Checks every 10 minutes
    private static final float CHANCE_PERCENT = 1.0f; //1% chance

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            BlockPos playerPos = player.getBlockPos();
            ServerWorld world = (ServerWorld) player.getWorld();
            if (world.isSkyVisible(playerPos.up())) {
                if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                    summonLightningBolt(world, player);
                }
            }
        });
    }
    private static void summonLightningBolt(ServerWorld world, PlayerEntity player){
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);

        BlockPos pos = player.getBlockPos();

        lightning.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 3); //Spawns near the player

        world.spawnEntity(lightning);

        System.out.println("DEBUG: Lightning Bolt summoned at X: " + pos.getX() + "Y: " + pos.getY() + "Z: " + pos.getZ());

        //Sets weather to thunder
        int rainDurationTicks = 20 * 60 * 5; //5 minute duration
        world.setWeather(0, rainDurationTicks, true, true);
    }
}
