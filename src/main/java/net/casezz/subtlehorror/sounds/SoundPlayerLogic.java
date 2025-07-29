package net.casezz.subtlehorror.sounds;

import net.casezz.subtlehorror.util.ModUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import static net.casezz.subtlehorror.util.ModUtils.isPlayerInCave;

public class SoundPlayerLogic {


    private static final Random RANDOM = Random.create();
    private static final int CHECK_INTERVAL_TICKS = 20 * 20; //Checks every 20 seconds
    private static final float CHANCE_PERCENT = 0.5f; //5% chance

    public static void register() {
        ModUtils.playerTickHandler(CHECK_INTERVAL_TICKS, (server, player) -> {
            if (RANDOM.nextFloat() * 100 < CHANCE_PERCENT) {
                ServerWorld world = (ServerWorld) player.getWorld();
                Random RANDOM2 = Random.create();
                float roll = RANDOM2.nextFloat();

                if (roll < 0.33f) {
                    if (isPlayerInCave(player)) {
                        playCaveSound(player);
                    }
                } else if (roll < 0.66f) {
                    if (world.isNight()) {
                        playDistantScreamSound(player);
                    }
                } else {
                    playStepsSound(world, player);
                }
            }
        });
    }

    private static void playDistantScreamSound(PlayerEntity player){
        player.getWorld().playSound(
                null,
                player.getX() + 2,
                player.getY(),
                player.getZ() + 2,
                SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK,
                SoundCategory.AMBIENT,
                0.2f,
                0.1f
        );
        System.out.println("DEBUG: Played distant scream sound near: " + player.getName().getString());
    }

    private static void playCaveSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENTITY_PLAYER_BREATH,
                SoundCategory.AMBIENT,
                0.3f,
                1.0f
        );
        System.out.println("DEBUG: Played breathing sound near: " + player.getName().getString());
    }

    private static int stepTickCounter = -1;
    private static ServerWorld stepWorld;
    private static PlayerEntity stepPlayer;
    private static SoundEvent stepSound;
    private static float stepPitch;

    public static void playStepsSound(ServerWorld world, PlayerEntity player) {
        BlockPos pos = player.getBlockPos().down();
        BlockState state = world.getBlockState(pos);
        BlockSoundGroup soundGroup = state.getSoundGroup();

        stepWorld = world;
        stepPlayer = player;
        stepSound = soundGroup.getStepSound();
        stepPitch = soundGroup.getPitch();
        stepTickCounter = 0;

        System.out.println("DEBUG: Played steps sounds near: " + player.getName().getString());
    }


    //Defines ticks interval for each step sound
    public static void tickStepSequence() {
        if (stepTickCounter >= 0 && stepTickCounter < 20) {
            if (stepTickCounter % 4 == 0) {
                float volume = stepTickCounter / 20.0f + 0.1f;
                stepWorld.playSound(
                        null,
                        stepPlayer.getX(),
                        stepPlayer.getY(),
                        stepPlayer.getZ(),
                        stepSound,
                        SoundCategory.BLOCKS,
                        volume,
                        stepPitch
                );
            }
            stepTickCounter++;
        } else {
            stepTickCounter = -1;
        }
    }
}
