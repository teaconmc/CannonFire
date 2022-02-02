package org.teacon.cannonfire.player;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
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
