package org.teacon.cannonfire.critereon;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.teacon.cannonfire.CannonFire;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonLiftoffCriterionTrigger extends SimpleCriterionTrigger<CannonLiftoffCriterionTrigger.Instance> {
    public static final CannonLiftoffCriterionTrigger INSTANCE = new CannonLiftoffCriterionTrigger();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CriteriaTriggers.register(INSTANCE));
    }

    @Override
    protected Instance createInstance(JsonObject json,
                                      EntityPredicate.Composite player, DeserializationContext context) {
        var location = LocationPredicate.fromJson(json.get("location"));
        return new Instance(player, location);
    }

    @Override
    public ResourceLocation getId() {
        return CannonFire.CANNON_LIFTOFF_ADVANCEMENT_ID;
    }

    public void trigger(ServerPlayer player, BlockPos pos) {
        this.trigger(player, instance -> instance.matches(player.getLevel(), pos));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;

        public Instance() {
            super(CannonFire.CANNON_LIFTOFF_ADVANCEMENT_ID, EntityPredicate.Composite.ANY);
            this.location = LocationPredicate.ANY;
        }

        public Instance(EntityPredicate.Composite player, LocationPredicate location) {
            super(CannonFire.CANNON_LIFTOFF_ADVANCEMENT_ID, player);
            this.location = location;
        }

        public boolean matches(ServerLevel world, BlockPos pos) {
            return this.location.matches(world, pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            return Util.make(super.serializeToJson(context), o -> o.add("location", this.location.serializeToJson()));
        }
    }
}
