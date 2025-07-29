package net.casezz.subtlehorror.util;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModUtils {

    @FunctionalInterface
    public interface BlockAction {
        boolean apply(ServerWorld world, BlockPos chunkOrigin, PlayerEntity player);
    }

    @FunctionalInterface
    public interface EventAction {
        void apply(ServerWorld world, PlayerEntity player);
    }

    //Handles ticks checks for every player
    public static void playerTickHandler(int CHECK_INTERVAL_TICKS, BiConsumer<MinecraftServer, PlayerEntity> onTick) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
                //Iterates over every connected player
                for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    onTick.accept(server, player);
                }
            }
        });
    }

    //Command logic for complex events
    public static int complexEvent(
            CommandContext<ServerCommandSource> context,
            BlockAction actionFunction,
            BiFunction<PlayerEntity, Boolean, Text> feedbackFunction
    ){
        try {
            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            BlockPos chunkOrigin = player.getChunkPos().getStartPos();
            ServerWorld world = (ServerWorld) player.getWorld();

            boolean result = actionFunction.apply(world, chunkOrigin, player);

            Text feedback = feedbackFunction.apply(player, result);
            context.getSource().sendFeedback(() -> feedback, false);

            return 1;
        }
        catch (Exception e){
            context.getSource().sendError(Text.literal("Error executing command: ").append(Text.literal(e.getMessage())));
            return 0;
        }
    }

    //Command logic for simple events
    public static int simpleEvent(
            CommandContext<ServerCommandSource> context,
            EventAction actionFunction,
            Function<PlayerEntity, Text> feedbackFunction
    ){
        try {
            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            ServerWorld world = (ServerWorld) player.getWorld();

            actionFunction.apply(world, player);

            Text feedback = feedbackFunction.apply(player);
            context.getSource().sendFeedback(() -> feedback, false);

            return 1;
        }

        catch (Exception e){
            context.getSource().sendError(Text.literal("Error executing command: ").append(Text.literal(e.getMessage())));
            return 0;
        }
    }

    //Check if player it's in cave
    public static boolean isPlayerInCave(PlayerEntity player) {
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
}
