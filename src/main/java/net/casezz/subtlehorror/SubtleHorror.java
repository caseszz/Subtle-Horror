package net.casezz.subtlehorror;

import net.casezz.subtlehorror.events.*;
import net.casezz.subtlehorror.messages.FakeLanMessageLogic;
import net.casezz.subtlehorror.messages.FakeSleepMessageLogic;
import net.casezz.subtlehorror.sounds.BlockBreakSoundLogic;
import net.casezz.subtlehorror.sounds.SoundPlayerLogic;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubtleHorror implements ModInitializer {
	public static final String MOD_ID = "subtlehorror";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BlockBreakSoundLogic.register();
		SoundPlayerLogic.register();
		TorchPlaceLogic.register();
		TorchBreakLogic.register();
		FakeLanMessageLogic.register();
		FakeSleepMessageLogic.register();
		LightningBoltLogic.register();
		DoorOpeningLogic.register();
	}
}