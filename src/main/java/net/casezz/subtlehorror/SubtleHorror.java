package net.casezz.subtlehorror;

import net.casezz.subtlehorror.command.ModCommands;
import net.casezz.subtlehorror.events.*;
import net.casezz.subtlehorror.messages.*;
import net.casezz.subtlehorror.sounds.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubtleHorror implements ModInitializer {
	public static final String MOD_ID = "subtlehorror";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		//Events
		BlockBreakSoundLogic.register();
		SoundPlayerLogic.register();
		TorchPlaceLogic.register();
		TorchBreakLogic.register();
		FakeLanMessageLogic.register();
		FakeSleepMessageLogic.register();
		LightningBoltLogic.register();
		DoorOpeningLogic.register();
		ChestOpenSoundLogic.register();
		LootDropEventLogic.register();

		//Commands
		ModCommands.register();

		//Tick Tracker
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			SoundPlayerLogic.tickStepSequence();
		});
	}
}