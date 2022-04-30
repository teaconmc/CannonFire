package org.teacon.cannonfire.client;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.block.CannonBlockEntity;
import org.teacon.cannonfire.entity.CannonBulletCommonStatusHandler;
import org.teacon.cannonfire.network.CannonFireNetwork;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CannonBulletClientStatusHandler {
    public static Consumer<CannonFireNetwork.BulletStatusPacket> markBulletStatus() {
        return status -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                CannonBulletCommonStatusHandler.markBulletStatus(player).accept(status);
            }
        };
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        var entity = Minecraft.getInstance().getCameraEntity();
        if (event.phase == TickEvent.Phase.START && entity instanceof LivingEntity livingEntity) {
            var pos = livingEntity.blockPosition();
            if (livingEntity.level.getBlockEntity(pos) instanceof CannonBlockEntity cannon) {
                if (cannon.isBullet(livingEntity)) {
                    livingEntity.setYBodyRot(livingEntity.getYRot());
                    livingEntity.setXRot(0.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        var pos = event.getEntity().blockPosition();
        if (event.getEntity().level.getBlockEntity(pos) instanceof CannonBlockEntity cannon) {
            if (cannon.isBullet(event.getEntity())) {
                event.getEntity().setXRot(Math.max(event.getEntity().getXRot(), 0.0F));
                var poseStack = event.getPoseStack();
                var direction = Vec3.atCenterOf(pos).vectorTo(event.getEntity().position());
                poseStack.translate(-direction.x, -direction.y, -direction.z);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - cannon.getYaw()));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F - cannon.getPitch()));
                poseStack.mulPose(Vector3f.YN.rotationDegrees(180.0F - cannon.getYaw()));
                poseStack.translate(direction.x, direction.y, direction.z);
            }
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        var pos = event.getCamera().getEntity().blockPosition();
        if (event.getCamera().getEntity().level.getBlockEntity(pos) instanceof CannonBlockEntity cannon) {
            if (event.getCamera().getEntity() instanceof LivingEntity livingEntity && cannon.isBullet(livingEntity)) {
                if (!event.getCamera().isDetached()) {
                    var direction = new Vector3f(Vec3.atCenterOf(pos).vectorTo(event.getCamera().getPosition()));
                    direction.transform(Vector3f.XP.rotationDegrees(90.0F - cannon.getPitch()));
                    direction.transform(Vector3f.YP.rotationDegrees(180.0F - cannon.getYaw()));
                    event.getCamera().setPosition(Vec3.atCenterOf(pos).add(new Vec3(direction)));
                    var rotation = Quaternion.ONE.copy();
                    rotation.mul(Vector3f.YN.rotationDegrees(180.0F - cannon.getYaw()));
                    rotation.mul(Vector3f.XN.rotationDegrees(90.0F - cannon.getPitch()));
                    rotation.mul(Vector3f.YP.rotationDegrees(180.0F - cannon.getYaw()));
                    rotation.mul(Quaternion.fromYXZ(
                            (event.getYaw() + 180.0F) * Mth.DEG_TO_RAD,
                            event.getPitch() * Mth.DEG_TO_RAD,
                            event.getRoll() * Mth.DEG_TO_RAD));
                    var cameraYawPitchRoll = rotation.toYXZ();
                    event.setYaw(cameraYawPitchRoll.y() * Mth.RAD_TO_DEG - 180.0F);
                    event.setPitch(cameraYawPitchRoll.x() * Mth.RAD_TO_DEG);
                    event.setRoll(cameraYawPitchRoll.z() * Mth.RAD_TO_DEG);
                }
            }
        }
    }
}
