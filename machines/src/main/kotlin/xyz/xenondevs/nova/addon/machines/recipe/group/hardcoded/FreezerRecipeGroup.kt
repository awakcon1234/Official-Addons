package xyz.xenondevs.nova.addon.machines.recipe.group.hardcoded

import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.nova.addon.machines.registry.GuiTextures
import xyz.xenondevs.nova.addon.machines.registry.Items
import xyz.xenondevs.nova.tileentity.network.type.fluid.FluidType
import xyz.xenondevs.nova.ui.menu.StaticFluidBar
import xyz.xenondevs.nova.ui.menu.explorer.recipes.createRecipeChoiceItem
import xyz.xenondevs.nova.ui.menu.explorer.recipes.group.RecipeGroup

object FreezerRecipeGroup : RecipeGroup<FreezerRecipe>() {
    
    override val priority = 8
    override val texture = GuiTextures.RECIPE_FREEZER
    override val icon = Items.FREEZER.model.clientsideProvider
    
    override fun createGui(recipe: FreezerRecipe): Gui {
        return Gui.normal()
            .setStructure(
                ". w . . . . . . .",
                ". w . . . . r . .",
                ". w . . . . . . .")
            .addIngredient('r', createRecipeChoiceItem(listOf(recipe.result)))
            .addIngredient('w', StaticFluidBar(3, 100_000, FluidType.WATER, 1000L * recipe.mode.maxCostMultiplier))
            .build()
    }
    
}