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
        this.add(CannonFire.TEXT_LIFTOFF_DESC, switch (this.locale) {
            default -> throw new IllegalStateException();
            case EN_US -> "Shoot yourself by a cannon.";
            case ZH_CN -> "通过一个火炮把自己发射出去。";
        });
        this.add(CannonFire.TEXT_LIFTOFF_TITLE, switch (this.locale) {
            default -> throw new IllegalStateException();
            case EN_US -> "Three, Two, One ... Liftoff!";
            case ZH_CN -> "三，二，一……发射！";
        });
        this.add(CannonFire.TEXT_NEEDS_CAMPFIRE, switch (this.locale) {
            default -> throw new IllegalStateException();
            case EN_US -> "A campfire block is required below for lighting the cannon";
            case ZH_CN -> "点燃火炮需要下方有营火方块存在";
        });
        this.add(CannonFire.TEXT_SHIFT_OUT, switch (this.locale) {
            default -> throw new IllegalStateException();
            case EN_US -> "Press shift key to get out of the cannon";
            case ZH_CN -> "按下 shift 键离开火炮";
        });
    }
}
