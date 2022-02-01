package org.teacon.cannonfire.data;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.teacon.cannonfire.CannonFire;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonFireLanguageProvider extends LanguageProvider {
    private static final String EN_US = "en_us";
    private static final String ZH_CN = "zh_cn";

    private final String locale;

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new CannonFireLanguageProvider(generator, EN_US));
        generator.addProvider(new CannonFireLanguageProvider(generator, ZH_CN));
    }

    private CannonFireLanguageProvider(DataGenerator gen, String locale) {
        super(gen, CannonFire.ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        this.add(CannonFire.CANNON_BLOCK.get(), switch (this.locale) {
            default -> throw new IllegalStateException();
            case EN_US -> "Cannon";
            case ZH_CN -> "火炮";
        });
    }
}
