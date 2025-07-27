package net.casezz.subtlehorror.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casezz.subtlehorror.events.DoorOpeningLogic;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ModCommands {

    public static void register(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("subtlehorror")
                    .then(CommandManager.literal("open_door")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(ModCommands::executeOpenDoor)))
            );
        });
    }


    //Door opening command
    private static int executeOpenDoor(CommandContext<ServerCommandSource> context) {
        try {

            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            BlockPos chunkOrigin = player.getChunkPos().getStartPos();
            ServerWorld world = (ServerWorld) player.getWorld();

            boolean result = DoorOpeningLogic.openDoorInArea(world, chunkOrigin, player);

            if (result) {
                context.getSource().sendFeedback(() -> Text.literal("Opened door near ").append(Text.literal(player.getName().getString())), false);
            }
            else{
                context.getSource().sendFeedback(() -> Text.literal("No doors found near ").append(Text.literal(player.getName().getString())), false);
            }
            return 1;
        }
        catch (Exception e){
            context.getSource().sendError(Text.literal("Error executing command: ").append(Text.literal(e.getMessage())));
            return 0;
        }
    }
}

