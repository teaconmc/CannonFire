package org.teacon.cannonfire.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.CannonFire;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Locale;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);

    private static final VoxelShape COLLISION_SHAPE = box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    @SubscribeEvent
    public static void onRegisterBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new CannonBlock(BlockBehaviour.Properties.of(
                Material.METAL).noOcclusion()).setRegistryName(CannonFire.CANNON_BLOCK_ID));
    }

    private CannonBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATUS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, Arrays.stream(context.getNearestLookingDirections()).filter(
                        direction -> direction.getAxis().isHorizontal()).findFirst().orElse(Direction.NORTH))
                .setValue(STATUS, Status.fromBelowBlock(
                        context.getLevel().getBlockState(context.getClickedPos().below())));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction,
                                  BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN ? state.setValue(STATUS, Status.fromBelowBlock(neighborState)) : state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world,
                                                                  BlockState state, BlockEntityType<T> type) {
        return CannonFire.CANNON_BLOCK_ENTITY.get().equals(type) ? CannonBlockEntity::tick : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity.level.getBlockEntity(pos) instanceof CannonBlockEntity cannon) {
            if (entity instanceof LivingEntity livingEntity && cannon.isBullet(livingEntity)) {
                livingEntity.makeStuckInBlock(state, new Vec3(0.0, 1.0, 0.0));
            }
        }
    }

    public void tryLightCannon(Player player, BlockPos pos, InteractionHand hand, ItemStack item) {
        var belowPos = pos.below();
        var belowState = player.level.getBlockState(belowPos);
        if (belowState.hasProperty(CampfireBlock.LIT) && isCampFire(belowState)) {
            if (!belowState.getValue(CampfireBlock.LIT)) {
                var sound = SoundEvents.FLINTANDSTEEL_USE;
                var soundPitch = player.level.getRandom().nextFloat() * 0.4F + 0.8F;
                player.level.playSound(player, belowPos, sound, SoundSource.BLOCKS, 1.0F, soundPitch);
                player.level.setBlock(belowPos, belowState.setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL);
                player.level.gameEvent(player, GameEvent.BLOCK_PLACE, belowPos);
                item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            var text = new TranslatableComponent(CannonFire.TEXT_NEEDS_CAMPFIRE);
            serverPlayer.sendMessage(text, ChatType.GAME_INFO, Util.NIL_UUID);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        var item = player.getItemInHand(hand);
        if (world.getBlockEntity(pos) instanceof CannonBlockEntity cannon) {
            if (item.is(Items.FLINT_AND_STEEL)) {
                this.tryLightCannon(player, pos, hand, item);
            } else {
                cannon.fillBullet(player);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean isCampFire(BlockState state) {
        return state.m_204336_(BlockTags.CAMPFIRES);
    }

    public enum Status implements StringRepresentable {
        DECORATION, UNLIT, LIT;

        public static Status fromBelowBlock(BlockState state) {
            if (!state.hasProperty(CampfireBlock.LIT) || !isCampFire(state)) {
                return Status.DECORATION;
            } else if (!state.getValue(CampfireBlock.LIT)) {
                return Status.UNLIT;
            } else {
                return Status.LIT;
            }
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
