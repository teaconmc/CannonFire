package org.teacon.cannonfire;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.teacon.cannonfire.block.CannonBlock;
import org.teacon.cannonfire.block.CannonBlockEntity;
import org.teacon.cannonfire.item.CannonItem;

@Mod(CannonFire.ID)
public class CannonFire {
    public static final String ID = "cannon_fire";

    public static final String TEXT_NEEDS_CAMPFIRE = "chat." + ID + ".needs_campfire";
    public static final String TEXT_SHIFT_OUT = "chat." + ID + ".shift_out";

    public static final String CANNON_MODEL_ID_PREFIX = "block/cannon_";

    public static final ResourceLocation CANNON_BASE_MODEL_ID = new ResourceLocation(ID, "block/cannon_base");
    public static final ResourceLocation CANNON_MOUNT_MODEL_ID = new ResourceLocation(ID, "block/cannon_mount");

    public static final ResourceLocation CANNON_ZZBOOM_TEXTURE_ID = new ResourceLocation(ID, "block/cannon_zzboom");

    public static final ResourceLocation CANNON_BLOCK_ID = new ResourceLocation(ID, "cannon_block");

    public static final RegistryObject<CannonItem> CANNON_ITEM = RegistryObject.of(CANNON_BLOCK_ID, ForgeRegistries.ITEMS);
    public static final RegistryObject<CannonBlock> CANNON_BLOCK = RegistryObject.of(CANNON_BLOCK_ID, ForgeRegistries.BLOCKS);
    public static final RegistryObject<BlockEntityType<CannonBlockEntity>> CANNON_BLOCK_ENTITY = RegistryObject.of(CANNON_BLOCK_ID, ForgeRegistries.BLOCK_ENTITIES);
}
