package net.casezz.subtlehorror.sounds;

import net.casezz.subtlehorror.util.ModUtils;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class BlockBreakSoundLogic {
    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 10; //Checks every 10 seconds
    private static final float CHANCE_PERCENT = 0.5f; //0.5% chance

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server,player) -> {
            if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                playBlockBreakSound((ServerWorld) player.getWorld(), player);
            }
        });
    }

    public static void playBlockBreakSound(ServerWorld world, PlayerEntity player) {
        BlockPos blockUnderPlayerPos = player.getBlockPos().down();

        BlockState blockState = world.getBlockState(blockUnderPlayerPos);

        BlockSoundGroup soundGroup = blockState.getSoundGroup();

        SoundEvent breakSound = soundGroup.getBreakSound();

        if (breakSound != null) {
            world.playSound(
                    null,
                    player.getX() + 4,
                    player.getY(),
                    player.getZ() + 4,
                    breakSound,
                    SoundCategory.BLOCKS,
                    soundGroup.getVolume(),
                    soundGroup.getPitch()
            );
            System.out.println("DEBUG: " + blockState.getBlock().getName().getString() + " sound played on player position.");
        }
    }
}