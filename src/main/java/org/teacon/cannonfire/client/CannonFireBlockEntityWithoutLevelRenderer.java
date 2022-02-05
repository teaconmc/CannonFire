package org.teacon.cannonfire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.block.CannonBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CannonFireBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    public static CannonFireBlockEntityWithoutLevelRenderer INSTANCE;

    private final BlockEntityRenderDispatcher dispatcher;

    @SubscribeEvent
    public static void onAddReloadListener(RegisterClientReloadListenersEvent event) {
        INSTANCE = new CannonFireBlockEntityWithoutLevelRenderer(Minecraft.getInstance());
        event.registerReloadListener(INSTANCE);
    }

    private CannonFireBlockEntityWithoutLevelRenderer(Minecraft mc) {
        super(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        this.dispatcher = mc.getBlockEntityRenderDispatcher();
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var item = itemStack.getItem();
        if (CannonFire.CANNON_ITEM.get().equals(item)) {
            var blockEntity = new CannonBlockEntity(BlockPos.ZERO, CannonFire.CANNON_BLOCK.get().defaultBlockState());
            this.dispatcher.renderItem(blockEntity, poseStack, buffer, packedLight, packedOverlay);
        }
        super.renderByItem(itemStack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }
}
