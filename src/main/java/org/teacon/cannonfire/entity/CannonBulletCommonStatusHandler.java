package org.teacon.cannonfire.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.block.CannonBlockEntity;
import org.teacon.cannonfire.network.CannonFireNetwork;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CannonBulletCommonStatusHandler {
    public static final String BULLET_FLYING = "CannonFireBulletFlying";
    public static final String BULLET_ON_GROUND = "CannonFireBulletOnGround";

    public static Consumer<CannonFireNetwork.BulletStatusPacket> markBulletStatus(LivingEntity entity) {
        return status -> {
            entity.setDiscardFriction(status.isBullet());
            entity.makeStuckInBlock(entity.getFeetBlockState(), Vec3.ZERO);
            entity.getPersistentData().putBoolean(BULLET_FLYING, status.isBullet());
            entity.setDeltaMovement(entity.getDeltaMovement().add(status.additionalDeltaMovement()));
            if (entity instanceof ServerPlayer player) {
                if (status.isBullet()) {
                    player.getAbilities().mayfly = true;
                } else {
                    player.gameMode.getGameModeForPlayer().updatePlayerAbilities(player.getAbilities());
                }
                CannonFireNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), status);
            }
        };
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        var entity = event.getEntityLiving();
        var blockEntity = entity.level.getBlockEntity(entity.blockPosition());
        if (blockEntity instanceof CannonBlockEntity cannon && cannon.isBullet(entity)) {
            if (entity.isShiftKeyDown()) {
                entity.moveTo(Vec3.atCenterOf(entity.blockPosition()));
                cannon.releaseBullet();
            } else {
                entity.setXRot(0.0F);
                entity.setYBodyRot(entity.getYRot());
                cannon.tryShootBullet(entity);
            }
        }
        if (entity.getPersistentData().getBoolean(BULLET_ON_GROUND)) {
            entity.getPersistentData().remove(BULLET_ON_GROUND);
            if (entity.isOnGround() || entity.isFallFlying()) {
                markBulletStatus(entity).accept(new CannonFireNetwork.BulletStatusPacket(false, Vec3.ZERO));
            }
        }
        if (entity.getPersistentData().getBoolean(BULLET_FLYING)) {
            entity.getPersistentData().putBoolean(BULLET_ON_GROUND, entity.isOnGround() || entity.isFallFlying());
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        var entity = event.getEntityLiving();
        if (entity.getPersistentData().getBoolean(BULLET_FLYING)) {
            event.setDamageMultiplier(0.0F);
        }
    }

    @SubscribeEvent
    public static void onClickItem(PlayerInteractEvent.RightClickItem event) {
        var player = event.getPlayer();
        var blockEntity = player.level.getBlockEntity(player.blockPosition());
        if (blockEntity instanceof CannonBlockEntity cannon && cannon.isBullet(player)) {
            var item = event.getItemStack();
            if (item.is(Items.FLINT_AND_STEEL)) {
                CannonFire.CANNON_BLOCK.get().tryLightCannon(player, player.blockPosition(), event.getHand(), item);
                event.setCancellationResult(InteractionResult.sidedSuccess(event.getSide().isClient()));
                event.setCanceled(true);
            }
        }
    }
}
