package top.maplex.fomalhautshop.item.impl

import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.ItemsAdder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem

object ShopItemsAdderItem : ShopItem {

    override val source: String = "IA"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<ItemStack> {
        return ItemsAdder.getAllItems().map { it.itemStack }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        CustomStack.getInstance(id)?.let {
            return it.itemStack
        }
        return null
    }

    override fun getItemId(itemStack: ItemStack): String {
        CustomStack.byItemStack(itemStack)?.let {
            return it.namespacedID
        }
        return "none"
    }
}
