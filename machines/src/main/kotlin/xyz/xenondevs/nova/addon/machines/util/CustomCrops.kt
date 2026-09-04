package xyz.xenondevs.nova.addon.machines.util

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.nova.config.Configs
import xyz.xenondevs.nova.util.item.novaItem
import xyz.xenondevs.nova.util.novaBlock
import xyz.xenondevs.nova.util.novaBlockState
import xyz.xenondevs.nova.world.block.NovaBlock
import xyz.xenondevs.nova.world.block.state.NovaBlockState

private const val FARMLAND = "minecraft:farmland"

/**
 * One entry of the custom crop table.
 *
 * @param seed The id of the item that plants this crop.
 * @param crop The id of the block that item plants.
 * @param age The id of the block state property holding the crop's growth stage.
 * @param maxAge The id of the property holding the ripe stage, when it is not a fixed [ripeAge].
 * @param ripeAge The stage at which the crop is ripe, when the config states it outright.
 * @param soil The block ids the crop may stand on, vanilla materials and addon blocks alike.
 */
private class CustomCrop(
    val seed: String,
    val crop: String,
    val age: String,
    val maxAge: String?,
    val ripeAge: Int?,
    val soil: Set<String>
)

/**
 * The crops of other addons that the planter can sow and the harvester can reap.
 *
 * Nothing here names an addon. The table is read from `configs/agriculture.yml` and matched on
 * ids, so this addon neither compiles nor links against whichever addon provides a crop, and an
 * entry naming something that is not installed simply never matches. Adding another addon's crop
 * is a config entry and nothing else.
 */
object CustomCrops {
    
    private val crops: List<CustomCrop> by lazy { parse(Configs["machines:agriculture"].get()) }
    private val bySeed: Map<String, CustomCrop> by lazy { crops.associateBy(CustomCrop::seed) }
    private val byCrop: Map<String, CustomCrop> by lazy { crops.associateBy(CustomCrop::crop) }
    
    /**
     * Whether [item] plants one of the crops in the table.
     */
    fun isSeed(item: ItemStack): Boolean =
        seedEntry(item) != null
    
    /**
     * Whether the crop [item] plants may stand on [soil].
     */
    fun canBePlacedOn(item: ItemStack, soil: Block): Boolean {
        val entry = seedEntry(item) ?: return false
        return idOf(soil) in entry.soil
    }
    
    /**
     * Whether the crop [item] plants needs tilled farmland under it.
     */
    fun requiresFarmland(item: ItemStack): Boolean {
        val entry = seedEntry(item) ?: return false
        return FARMLAND in entry.soil
    }
    
    /**
     * The block [item] plants, or null when [item] is not a seed in the table.
     *
     * The block is taken from the item itself rather than looked up by the configured id, so a
     * mistyped `crop` can only ever cost a match, never plant the wrong thing.
     */
    fun cropOf(item: ItemStack): NovaBlock? {
        val entry = seedEntry(item) ?: return null
        val block = item.novaItem?.block ?: return null
        return block.takeIf { it.id.asString() == entry.crop }
    }
    
    /**
     * Whether [block] is a crop from the table, at any growth stage.
     */
    fun isCrop(block: Block): Boolean =
        block.novaBlock?.id?.asString() in byCrop
    
    /**
     * Whether [block] is a crop from the table that has finished growing.
     */
    fun isRipe(block: Block): Boolean {
        val state = block.novaBlockState ?: return false
        val entry = byCrop[state.block.id.asString()] ?: return false
        
        val age = property(state, entry.age) ?: return false
        val ripeAge = entry.ripeAge ?: entry.maxAge?.let { property(state, it) } ?: return false
        
        return age >= ripeAge
    }
    
    private fun parse(root: ConfigurationNode): List<CustomCrop> =
        root.node("custom_crops").childrenList().mapNotNull { node ->
            val seed = node.node("seed").string ?: return@mapNotNull null
            val crop = node.node("crop").string ?: return@mapNotNull null
            val age = node.node("age").string ?: return@mapNotNull null
            
            // max_age either names a property or states the ripe stage as a plain number.
            val maxAge = node.node("max_age").string
            val ripeAge = maxAge?.toIntOrNull()
            
            CustomCrop(
                seed,
                crop,
                age,
                if (ripeAge == null) maxAge else null,
                ripeAge,
                node.node("soil").childrenList().mapNotNullTo(HashSet()) { it.string }
            )
        }
    
    private fun seedEntry(item: ItemStack): CustomCrop? {
        val id = item.novaItem?.id?.asString() ?: return null
        return bySeed[id]
    }
    
    private fun idOf(block: Block): String =
        block.novaBlock?.id?.asString() ?: block.type.key().asString()
    
    private fun property(state: NovaBlockState, id: String): Int? =
        state.values.entries
            .firstOrNull { it.key.id.asString() == id }
            ?.value as? Int
    
}
