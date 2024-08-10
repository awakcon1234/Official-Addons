package xyz.xenondevs.nova.addon.machines.recipe

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.potion.PotionEffectType
import xyz.xenondevs.commons.gson.getDoubleOrNull
import xyz.xenondevs.commons.gson.getInt
import xyz.xenondevs.commons.gson.getIntOrNull
import xyz.xenondevs.commons.gson.getLong
import xyz.xenondevs.commons.gson.getString
import xyz.xenondevs.nova.serialization.json.getDeserialized
import xyz.xenondevs.nova.serialization.json.serializer.ConversionRecipeDeserializer
import xyz.xenondevs.nova.serialization.json.serializer.RecipeDeserializer
import xyz.xenondevs.nova.serialization.json.serializer.RecipeDeserializer.Companion.getRecipeId
import xyz.xenondevs.nova.serialization.json.serializer.RecipeDeserializer.Companion.parseRecipeChoice
import xyz.xenondevs.nova.util.data.getInputStacks
import xyz.xenondevs.nova.util.item.ItemUtils
import xyz.xenondevs.nova.world.block.tileentity.network.type.fluid.FluidType
import java.io.File

object PulverizerRecipeDeserializer : ConversionRecipeDeserializer<PulverizerRecipe>() {
    override fun createRecipe(json: JsonObject, id: ResourceLocation, input: RecipeChoice, result: ItemStack, time: Int) =
        PulverizerRecipe(id, input, result, time)
}

object PlatePressRecipeDeserializer : ConversionRecipeDeserializer<PlatePressRecipe>() {
    override fun createRecipe(json: JsonObject, id: ResourceLocation, input: RecipeChoice, result: ItemStack, time: Int) =
        PlatePressRecipe(id, input, result, time)
}

object GearPressRecipeDeserializer : ConversionRecipeDeserializer<GearPressRecipe>() {
    override fun createRecipe(json: JsonObject, id: ResourceLocation, input: RecipeChoice, result: ItemStack, time: Int) =
        GearPressRecipe(id, input, result, time)
}

object FluidInfuserRecipeDeserializer : RecipeDeserializer<FluidInfuserRecipe> {
    
    override fun deserialize(json: JsonObject, file: File): FluidInfuserRecipe {
        val mode = json.getDeserialized<FluidInfuserRecipe.InfuserMode>("mode")
        val fluidType = json.getDeserialized<FluidType>("fluid_type")
        val fluidAmount = json.getLong("fluid_amount")
        val input = parseRecipeChoice(json.get("input"))
        val time = json.getInt("time")
        val result = ItemUtils.getItemBuilder(json.getString("result")).get()
        
        return FluidInfuserRecipe(getRecipeId(file), mode, fluidType, fluidAmount, input, result, time)
    }
    
}

object ElectricBrewingStandRecipeDeserializer : RecipeDeserializer<ElectricBrewingStandRecipe> {
    
    override fun deserialize(json: JsonObject, file: File): ElectricBrewingStandRecipe {
        val inputs = json.getAsJsonArray("inputs").map { ItemUtils.getRecipeChoice(listOf(it.asString)) }
        require(inputs.all { it.getInputStacks().size == 1 })
        
        val resultName = json.getString("result")
        val result = PotionEffectType.getByKey(NamespacedKey.fromString(resultName))
            ?: throw IllegalArgumentException("Invalid result")
        
        val defaultTime = json.getIntOrNull("default_time") ?: 0
        val redstoneMultiplier = json.getDoubleOrNull("redstone_multiplier") ?: 0.0
        val glowstoneMultiplier = json.getDoubleOrNull("glowstone_multiplier") ?: 0.0
        val maxDurationLevel = json.getIntOrNull("max_duration_level") ?: 0
        val maxAmplifierLevel = json.getIntOrNull("max_amplifier_level") ?: 0
        
        return ElectricBrewingStandRecipe(
            getRecipeId(file),
            inputs, result,
            defaultTime,
            redstoneMultiplier, glowstoneMultiplier,
            maxDurationLevel, maxAmplifierLevel
        )
    }
    
}

object CrystallizerRecipeDeserializer : ConversionRecipeDeserializer<CrystallizerRecipe>() {
    
    override fun createRecipe(json: JsonObject, id: ResourceLocation, input: RecipeChoice, result: ItemStack, time: Int): CrystallizerRecipe {
        return CrystallizerRecipe(id, input, result, time)
    }
    
}