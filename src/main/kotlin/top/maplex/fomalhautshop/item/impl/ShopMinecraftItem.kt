package top.maplex.fomalhautshop.item.impl

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.parseToItemStack
import top.maplex.fomalhautshop.item.ShopItem

object ShopMinecraftItem : ShopItem {

    override val source: String = "MC"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        registerItem()
    }

    override fun getItemList(): MutableList<ItemStack> {
        return Material.values().map { ItemStack(it) }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return try {
            id.parseToItemStack()
        } catch (e: Exception) {
            null
        }
    }

    override fun getItemId(itemStack: ItemStack): String {
        return itemStack.type.name
    }

}
