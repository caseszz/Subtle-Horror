package net.casezz.subtlehorror.command;

import com.mojang.brigadier.context.CommandContext;
import net.casezz.subtlehorror.events.*;
import net.casezz.subtlehorror.messages.FakeLanMessageLogic;
import net.casezz.subtlehorror.sounds.*;
import net.casezz.subtlehorror.util.ModUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ModCommands {

    public static void register(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("subtlehorror")
                    .then(CommandManager.literal("help")
                            .executes(ModCommands::showHelp))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.literal("open_door")
                                    .executes(ModCommands::executeOpenDoor))
                            .then(CommandManager.literal("break_torch")
                                    .executes(ModCommands::executeTorchBreak))
                            .then(CommandManager.literal("lightning_bolt")
                                    .executes(ModCommands::executeLightingBolt))
                            .then(CommandManager.literal("open_chest")
                                    .executes(ModCommands::executeChestOpen))
                            .then(CommandManager.literal("break_block")
                                    .executes(ModCommands::executeBreakingBlock))
                            .then(CommandManager.literal("fake_lan")
                                    .executes(ModCommands::executeLANMessage))
                            .then(CommandManager.literal("steps")
                                    .executes(ModCommands::executeSteps)))
            );
        });
    }


    //Door opening command
    private static int executeOpenDoor(CommandContext<ServerCommandSource> context) {
        return ModUtils.complexEvent(
                context,
                DoorOpeningLogic::openDoorInArea,
                (player, result) -> {
                    String name = player.getName().getString();
                    return result
                            ? Text.literal("Opened door near ").append(Text.literal(name))
                            : Text.literal("No doors available near ").append(Text.literal(name));
                }
        );
    }

    //Torch breaking command
    private static int executeTorchBreak(CommandContext<ServerCommandSource> context) {
        return ModUtils.complexEvent(
                context,
                TorchBreakLogic::removeTorchesInArea,
                (player, result) -> {
                    String name = player.getName().getString();
                    return result
                            ? Text.literal("Broke torches near ").append(Text.literal(name))
                            : Text.literal("No torches available near ").append(Text.literal(name));
                }
        );
    }

    //Chest open command
    private static int executeChestOpen(CommandContext<ServerCommandSource> context) {
        return ModUtils.complexEvent(
                context,
                ChestOpenSoundLogic::chestOpen,
                (player, result) -> {
                    String name = player.getName().getString();
                    return result
                            ? Text.literal("Opened chest near ").append(Text.literal(name))
                            : Text.literal("No chest available near ").append(Text.literal(name));
                }
        );
    }

    //Lighting Bolt command
    private static int executeLightingBolt(CommandContext<ServerCommandSource> context){
        return ModUtils.simpleEvent(
                context,
                LightningBoltLogic::summonLightningBolt,
                player -> buildFeedback("Summoned lightning bolt near ", player)
        );
    }

    //Block Breaking Sound command
    private static int executeBreakingBlock(CommandContext<ServerCommandSource> context){
        return ModUtils.simpleEvent(
                context,
                BlockBreakSoundLogic::playBlockBreakSound,
                player -> buildFeedback("Played breaking block sound near ", player)
        );
    }

    //Fake LAN Message Command
    private static int executeLANMessage(CommandContext<ServerCommandSource> context){
        return ModUtils.simpleEvent(
                context,
                FakeLanMessageLogic::sendFakeLanMessage,
                player -> buildFeedback("Sent fake LAN message to ", player)
        );
    }

    private static int executeSteps(CommandContext<ServerCommandSource> context){
        return ModUtils.simpleEvent(
                context,
                SoundPlayerLogic::playStepsSound,
                player -> buildFeedback("Played steps sounds to ", player)
        );
    }

    //Help command
    private static int showHelp(CommandContext<ServerCommandSource> context){
        context.getSource().sendFeedback(()-> Text.literal("Command usage: /subtlehorror <player> <event>"), false);
        return 1;
    }

    //Feedback returning
    public static Text buildFeedback(String action, PlayerEntity player) {
        return Text.literal(action).append(Text.literal(player.getName().getString()));
    }
}

