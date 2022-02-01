package org.teacon.cannonfire.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.cannonfire.CannonFire;
import org.teacon.cannonfire.client.CannonFireBlockEntityWithoutLevelRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CannonItem extends BlockItem {
    @SubscribeEvent
    public static void onRegisterItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new CannonItem(
                new Item.Properties().tab(CreativeModeTab.TAB_MISC)).setRegistryName(CannonFire.CANNON_BLOCK_ID));
    }

    private CannonItem(Properties properties) {
        super(CannonFire.CANNON_BLOCK.get(), properties);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return CannonFireBlockEntityWithoutLevelRenderer.INSTANCE;
            }
        });
    }
}
