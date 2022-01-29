package org.teacon.cannonfire;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CannonFire.ID)
public class CannonFire {
    public static final String ID = "cannon_fire";
    public static final Logger LOGGER = LogManager.getLogger("CannonFire");

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonListener {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            var modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
            LOGGER.info("Initializing CannonFire mod ({}) ...", modInfo.getDescription());
        }
    }
}
