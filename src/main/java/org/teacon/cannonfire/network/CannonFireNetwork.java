package org.teacon.cannonfire.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.client.CannonBulletClientStatusHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonFireNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CannonFire.NETWORK_ID,
            () -> CannonFire.NETWORK_VERSION, CannonFire.NETWORK_VERSION::equals, CannonFire.NETWORK_VERSION::equals);

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(0, BulletStatusPacket.class,
                BulletStatusPacket::encode, BulletStatusPacket::decode,
                BulletStatusPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public record BulletStatusPacket(boolean isBullet, Vec3 additionalDeltaMovement) {
        public static BulletStatusPacket decode(FriendlyByteBuf buf) {
            return new BulletStatusPacket(buf.readBoolean(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(this.isBullet);
            buf.writeDouble(this.additionalDeltaMovement.x);
            buf.writeDouble(this.additionalDeltaMovement.y);
            buf.writeDouble(this.additionalDeltaMovement.z);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            supplier.get().enqueueWork(() -> DistExecutor.safeCallWhenOn(Dist.CLIENT,
                    () -> CannonBulletClientStatusHandler::markBulletStatus).accept(this));
            supplier.get().setPacketHandled(true);
        }
    }
}
