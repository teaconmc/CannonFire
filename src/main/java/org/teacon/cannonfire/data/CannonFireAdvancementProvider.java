package org.teacon.cannonfire.data;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.critereon.CannonLiftoffCriterionTrigger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonFireAdvancementProvider extends AdvancementProvider {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new CannonFireAdvancementProvider(generator, event.getExistingFileHelper()));
    }

    private CannonFireAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        consumer.accept(Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        CannonFire.CANNON_ITEM.get().getDefaultInstance(),
                        new TranslatableComponent(CannonFire.TEXT_LIFTOFF_TITLE),
                        new TranslatableComponent(CannonFire.TEXT_LIFTOFF_DESC), null, FrameType.CHALLENGE, true, true, false))
                .addCriterion(CannonFire.CANNON_LIFTOFF_ADVANCEMENT_ID.getPath(),
                        new CannonLiftoffCriterionTrigger.Instance()).build(CannonFire.CANNON_LIFTOFF_ADVANCEMENT_ID));
    }
}
