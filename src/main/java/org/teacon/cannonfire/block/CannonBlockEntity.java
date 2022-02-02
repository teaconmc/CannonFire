package org.teacon.cannonfire.block;

import com.mojang.datafixers.DSL;
import com.mojang.math.Vector3f;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
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
import org.teacon.cannonfire.player.CannonInteractionHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonBlockEntity extends BlockEntity {
    private static final String BULLET_UUID = "BulletUUID";
    private static final String YAW_OFFSET = "CannonYawOffset";
    private static final String PITCH_OFFSET = "CannonPitchOffset";
    private static final String BULLET_SHOT_SPEED = "BulletShotSpeed";
    private static final String PREPARATION_TICK = "PreparationTick";

    private float localYaw = 0F;
    private float localPitch = 30F;
    private float bulletShotSpeed = 4F;

    private int preparationTick = 0;
    private @Nullable UUID bulletEntity;

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
        if (clientTick < 32) {
            var index = Math.min(Mth.floor(clientTick / 2), 10);
            return new ResourceLocation(CannonFire.ID, CannonFire.CANNON_MODEL_ID_PREFIX + index);
        }
        if (clientTick < 50) {
            var index = Mth.floor(clientTick / 2) - 5;
            return new ResourceLocation(CannonFire.ID, CannonFire.CANNON_MODEL_ID_PREFIX + index);
        }
        return new ResourceLocation(CannonFire.ID, CannonFire.CANNON_MODEL_ID_PREFIX + 0);
    }

    public float getYaw() {
        return Mth.wrapDegrees(this.localYaw + 180 + this.getBlockState().getValue(CannonBlock.FACING).toYRot());
    }

    public float getPitch() {
        return this.localPitch;
    }

    public void setRotation(float pitch, float yaw) {
        var face = Direction.from2DDataValue(Mth.floor(Mth.wrapDegrees(yaw + 45) / 90 + 2));
        if (face != this.getBlockState().getValue(CannonBlock.FACING)) {
            var state = this.getBlockState().setValue(CannonBlock.FACING, face);
            Objects.requireNonNull(this.level).setBlock(this.getBlockPos(), state, Block.UPDATE_ALL);
        }
        var localYaw = Mth.wrapDegrees(yaw - 180 - face.toYRot());
        if (localYaw != this.localYaw) {
            this.localYaw = localYaw;
            this.markUpdated();
        }
        var localPitch = Mth.clamp(pitch, 0, 90);
        if (localPitch != this.localPitch) {
            this.localPitch = localPitch;
            this.markUpdated();
        }
    }

    public void fillBullet(LivingEntity entity) {
        if (!entity.level.isClientSide) {
            if (entity.level.equals(this.level)) {
                entity.moveTo(Vec3.atCenterOf(this.getBlockPos()));
            }
            if (entity instanceof ServerPlayer serverPlayer) {
                var text = new TranslatableComponent(CannonFire.TEXT_SHIFT_OUT);
                serverPlayer.sendMessage(text, ChatType.GAME_INFO, Util.NIL_UUID);
            }
            this.bulletEntity = entity.getUUID();
            this.markUpdated();
        }
    }

    public void releaseBullet() {
        this.bulletEntity = null;
        this.markUpdated();
    }

    public void tryShootBullet(LivingEntity entity) {
        if (entity.getUUID().equals(this.bulletEntity) && this.preparationTick >= 30) {
            var direction = new Vector3f(0, this.bulletShotSpeed, 0);
            entity.getPersistentData().putBoolean(CannonInteractionHandler.DISCARD_FRICTION, true);
            direction.transform(Vector3f.XP.rotationDegrees(90 - this.getPitch()));
            direction.transform(Vector3f.YP.rotationDegrees(180 - this.getYaw()));
            entity.makeStuckInBlock(this.getBlockState(), Vec3.ZERO);
            entity.setDeltaMovement(new Vec3(direction));
            entity.setDiscardFriction(true);
            this.releaseBullet();
        }
    }

    public boolean isBullet(LivingEntity entity) {
        return entity.level.equals(this.level) && entity.getUUID().equals(this.bulletEntity);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.bulletEntity != null) {
            tag.putUUID(BULLET_UUID, this.bulletEntity);
        }
        tag.putFloat(YAW_OFFSET, this.localYaw);
        tag.putFloat(PITCH_OFFSET, this.localPitch);
        tag.putFloat(BULLET_SHOT_SPEED, this.bulletShotSpeed);
        tag.putFloat(PREPARATION_TICK, this.preparationTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.bulletEntity = tag.hasUUID(BULLET_UUID) ? tag.getUUID(BULLET_UUID) : null;
        this.localYaw = Mth.positiveModulo(tag.getFloat(YAW_OFFSET) + 45, 90) - 45;
        this.localPitch = Mth.clamp(tag.getFloat(PITCH_OFFSET), 0, 90);
        this.bulletShotSpeed = tag.getInt(BULLET_SHOT_SPEED);
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
            tag.putFloat(BULLET_SHOT_SPEED, this.bulletShotSpeed);
            if (this.bulletEntity != null) {
                tag.putUUID(BULLET_UUID, this.bulletEntity);
            }
        });
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        Optional.ofNullable(packet.getTag()).ifPresent(this::handleUpdateTag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.localYaw = tag.getFloat(YAW_OFFSET);
        this.localPitch = tag.getFloat(PITCH_OFFSET);
        this.bulletShotSpeed = tag.getFloat(BULLET_SHOT_SPEED);
        this.bulletEntity = tag.hasUUID(BULLET_UUID) ? tag.getUUID(BULLET_UUID) : null;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(1.0);
    }

    private void markUpdated() {
        this.setChanged();
        var state = this.getBlockState();
        Objects.requireNonNull(this.level).sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_ALL);
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
                var bullet = cannon.bulletEntity;
                var entity = bullet == null ? null : serverWorld.getEntity(bullet);
                if (entity != null && !entity.blockPosition().equals(pos)) {
                    cannon.releaseBullet();
                }
            }
        }
    }
}
