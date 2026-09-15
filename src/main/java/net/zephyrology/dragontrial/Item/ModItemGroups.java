package net.zephyrology.dragontrial.Item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.zephyrology.dragontrial.DragonTrial1;

public class ModItemGroups {

    public static final ItemGroup PINK_GARNET_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(DragonTrial1.MOD_ID, "dragons"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.DRAGON_SPAWN_EGG))
                    .displayName(Text.translatable("itemgroup.dragontrial.dragons"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.DRAGON_SPAWN_EGG);

                    }).build());


    public static void registerItemGroups (){
        DragonTrial1.LOGGER.info("Registering Item Groups for " + DragonTrial1.MOD_ID);
    }

}
