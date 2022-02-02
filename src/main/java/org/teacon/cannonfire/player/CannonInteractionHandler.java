package org.teacon.cannonfire.player;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.block.CannonBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CannonInteractionHandler {
    public static final String DISCARD_FRICTION = "CannonDiscardFriction";

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        var entity = event.getEntityLiving();
        var entityBlockPos = entity.blockPosition();
        var blockEntity = entity.level.getBlockEntity(entityBlockPos);
        if (blockEntity instanceof CannonBlockEntity cannon && cannon.isBullet(entity)) {
            if (entity.isShiftKeyDown()) {
                entity.moveTo(Vec3.atCenterOf(entityBlockPos));
                cannon.releaseBullet();
            } else {
                entity.setYBodyRot(entity.getYRot());
                entity.setXRot(0.0F);
                cannon.tryShootBullet(entity);
            }
        }
        if (entity.getPersistentData().getBoolean(DISCARD_FRICTION) && entity.getDeltaMovement().y <= 0) {
            entity.getPersistentData().remove(DISCARD_FRICTION);
            entity.setDiscardFriction(false);
        }
    }

    @SubscribeEvent
    public static void onClickItem(PlayerInteractEvent.RightClickItem event) {
        var player = event.getPlayer();
        var pos = player.blockPosition();
        var blockEntity = player.level.getBlockEntity(pos);
        if (blockEntity instanceof CannonBlockEntity cannon && cannon.isBullet(player)) {
            var item = event.getItemStack();
            if (item.is(Items.FLINT_AND_STEEL)) {
                CannonFire.CANNON_BLOCK.get().tryLightCannon(player, pos, event.getHand(), item);
                event.setCancellationResult(InteractionResult.sidedSuccess(event.getSide().isClient()));
                event.setCanceled(true);
            }
        }
    }
}
