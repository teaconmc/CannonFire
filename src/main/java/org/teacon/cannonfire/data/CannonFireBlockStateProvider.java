package org.teacon.cannonfire.data;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.teacon.cannonfire.CannonFire;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonFireBlockStateProvider extends BlockStateProvider {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new CannonFireBlockStateProvider(generator, event.getExistingFileHelper()));
    }

    private CannonFireBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, CannonFire.ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        this.cannon(CannonFire.CANNON_0_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_1_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_2_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_3_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_4_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_5_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_BASE_MODEL_ID.getPath());
        this.cannon(CannonFire.CANNON_MOUNT_MODEL_ID.getPath());

        this.cannonBuiltinEntity(CannonFire.CANNON_BLOCK_ID.getPath());
        this.simpleBlock(CannonFire.CANNON_BLOCK.get(), this.models().getExistingFile(CannonFire.CANNON_BLOCK_ID));
        this.simpleBlockItem(CannonFire.CANNON_BLOCK.get(), this.models().getExistingFile(CannonFire.CANNON_BLOCK_ID));
    }

    private void cannonBuiltinEntity(String name) {
        this.models()
                .getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .texture("particle", CannonFire.CANNON_ZZBOOM_TEXTURE_ID)
                .transforms()
                .transform(ModelBuilder.Perspective.FIXED).scale(0.5F).end()
                .transform(ModelBuilder.Perspective.GROUND).translation(0F, 3F, 0F).scale(0.25F).end()
                .transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT).rotation(0F, 22.5F, 0F).scale(0.4F).end()
                .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT).rotation(0F, 22.5F, 0F).scale(0.4F).end()
                .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT).rotation(75F, 22.5F, 0F).scale(0.375F).end()
                .transform(ModelBuilder.Perspective.GUI).rotation(15F, 202.5F, 0F).translation(0F, -1F, 0F).scale(0.5625F).end()
                .end();
    }

    private void cannon(String name) {
        var modelLocation = this.modLoc("models/" + name + ".obj");
        this.models()
                .withExistingParent(name, this.mcLoc("block/block"))
                .texture("zzboom", CannonFire.CANNON_ZZBOOM_TEXTURE_ID)
                .customLoader(OBJLoaderBuilder::begin).flipV(true).modelLocation(modelLocation).end();
    }
}
