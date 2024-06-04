/*
 * File updated ~ 5 - 6 - 2024 ~ Leaf
 */

package leaf.cosmere.example;

import leaf.cosmere.example.advancements.ExampleAdvancementGen;
import leaf.cosmere.example.common.Example;
import leaf.cosmere.example.loottables.ExampleLootTableGen;
import leaf.cosmere.example.patchouli.ExamplePatchouliGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Example.MODID, bus = Bus.MOD)
public class ExampleDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

		generator.addProvider(true, new ExampleEngLangGen(output));
		generator.addProvider(true, new ExampleTagProvider(generator, existingFileHelper));
		generator.addProvider(true, new ExampleLootTableGen(generator));
		generator.addProvider(true, new ExampleItemModelsGen(generator, existingFileHelper));
		generator.addProvider(true, new ExampleRecipeGen(output, existingFileHelper));
		generator.addProvider(true, new ExamplePatchouliGen(generator));
		generator.addProvider(true, new ExampleAdvancementGen(generator));
	}

}