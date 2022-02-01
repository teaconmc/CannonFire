package org.teacon.cannonfire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.block.CannonBlock;
import org.teacon.cannonfire.block.CannonBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CannonBlockEntityRenderer implements BlockEntityRenderer<CannonBlockEntity> {
    private final ModelManager modelManager;
    private final ModelBlockRenderer modelRenderer;

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_0_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_1_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_2_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_3_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_4_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_5_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_BASE_MODEL_ID);
        ForgeModelBakery.addSpecialModel(CannonFire.CANNON_MOUNT_MODEL_ID);
    }

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CannonFire.CANNON_BLOCK_ENTITY.get(), CannonBlockEntityRenderer::new);
    }

    private CannonBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.modelManager = context.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.modelRenderer = context.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(CannonBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var source = buffer.getBuffer(RenderType.solid());

        var baseModel = this.modelManager.getModel(CannonFire.CANNON_BASE_MODEL_ID);
        var mountModel = this.modelManager.getModel(CannonFire.CANNON_MOUNT_MODEL_ID);
        var cannonModel = this.modelManager.getModel(blockEntity.getCannonModel(partialTick));

        poseStack.pushPose();

        poseStack.translate(0.5F, -1.0F, 0.5F);

        if (blockEntity.getBlockState().getValue(CannonBlock.STATUS) != CannonBlock.Status.DECORATION) {
            this.modelRenderer.renderModel(poseStack.last(), source, blockEntity.getBlockState(),
                    baseModel, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay, EmptyModelData.INSTANCE);
        }

        poseStack.translate(0.0F, 1.0F, 0.0F);
        poseStack.mulPose(Vector3f.YN.rotationDegrees(90.0F + blockEntity.getYaw()));

        this.modelRenderer.renderModel(poseStack.last(), source, blockEntity.getBlockState(),
                mountModel, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay, EmptyModelData.INSTANCE);

        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F - blockEntity.getPitch()));

        this.modelRenderer.renderModel(poseStack.last(), source, blockEntity.getBlockState(),
                cannonModel, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay, EmptyModelData.INSTANCE);

        poseStack.popPose();
    }
}
