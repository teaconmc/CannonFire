package org.teacon.cannonfire.block;

import com.mojang.datafixers.DSL;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.CannonFire;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonBlockEntity extends BlockEntity {
    private static final String YAW_OFFSET = "CannonYawOffset";
    private static final String PITCH_OFFSET = "CannonPitchOffset";
    private static final String PREPARATION_TICK = "PreparationTick";

    private float localYaw = 0F;
    private float localPitch = 30F;

    private int preparationTick = 0;

    @SubscribeEvent
    public static void onRegisterBlockEntityType(RegistryEvent.Register<BlockEntityType<?>> event) {
        event.getRegistry().register(BlockEntityType.Builder.of(CannonBlockEntity::new,
                CannonFire.CANNON_BLOCK.get()).build(DSL.remainderType()).setRegistryName(CannonFire.CANNON_BLOCK_ID));
    }

    public CannonBlockEntity(BlockPos pos, BlockState state) {
        super(CannonFire.CANNON_BLOCK_ENTITY.get(), pos, state);
    }

    public ResourceLocation getCannonModel(float partialTick) {
        var clientTick = this.preparationTick + partialTick;
        if (clientTick < 10) {
            return CannonFire.CANNON_0_MODEL_ID;
        }
        if (clientTick < 20) {
            return CannonFire.CANNON_1_MODEL_ID;
        }
        if (clientTick < 35) {
            return CannonFire.CANNON_2_MODEL_ID;
        }
        if (clientTick < 40) {
            return CannonFire.CANNON_3_MODEL_ID;
        }
        if (clientTick < 42) {
            return CannonFire.CANNON_0_MODEL_ID;
        }
        if (clientTick < 44) {
            return CannonFire.CANNON_4_MODEL_ID;
        }
        if (clientTick < 46) {
            return CannonFire.CANNON_0_MODEL_ID;
        }
        if (clientTick < 48) {
            return CannonFire.CANNON_5_MODEL_ID;
        }
        return CannonFire.CANNON_0_MODEL_ID;
    }

    public float getYaw() {
        return Mth.wrapDegrees(this.localYaw + this.getBlockState().getValue(CannonBlock.FACING).toYRot());
    }

    public float getPitch() {
        return this.localPitch;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat(YAW_OFFSET, this.localYaw);
        tag.putFloat(PITCH_OFFSET, this.localPitch);
        tag.putFloat(PREPARATION_TICK, this.preparationTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.localYaw = Mth.positiveModulo(tag.getFloat(YAW_OFFSET) + 45, 90) - 45;
        this.localPitch = Mth.clamp(tag.getFloat(PITCH_OFFSET), 0, 90);
        this.preparationTick = tag.getInt(PREPARATION_TICK);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return Util.make(new CompoundTag(), tag -> {
            tag.putFloat(YAW_OFFSET, this.localYaw);
            tag.putFloat(PITCH_OFFSET, this.localPitch);
        });
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.localYaw = tag.getFloat(YAW_OFFSET);
        this.localPitch = tag.getFloat(PITCH_OFFSET);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(1.0);
    }

    public static <T extends BlockEntity> void tick(Level world, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof CannonBlockEntity cannon) {
            var isLit = state.getValue(CannonBlock.STATUS) == CannonBlock.Status.LIT;
            cannon.preparationTick = isLit || cannon.preparationTick >= 30 ? (cannon.preparationTick + 1) % 50 : 0;
            if (world instanceof ServerLevel serverWorld) {
                if (cannon.preparationTick == 30) {
                    var belowPos = pos.below();
                    var belowState = serverWorld.getBlockState(belowPos);
                    if (belowState.hasProperty(CampfireBlock.LIT) && belowState.is(BlockTags.CAMPFIRES)) {
                        serverWorld.setBlock(belowPos, belowState.setValue(CampfireBlock.LIT, false), Block.UPDATE_ALL);
                    }
                    var center = Vec3.atCenterOf(pos);
                    var particle = ParticleTypes.EXPLOSION_EMITTER;
                    serverWorld.sendParticles(particle, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }
}
