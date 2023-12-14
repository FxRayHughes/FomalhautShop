package top.maplex.fomalhautshop.item.impl

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import pers.neige.easyitem.manager.ItemManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem

object ShopEasyItem : ShopItem {

    override val source: String = "EI"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("EasyItem") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<ItemStack> {
        return mutableListOf()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return ItemManager.getItemStack(id)
    }

    override fun getItemId(itemStack: ItemStack): String {
        return ItemManager.items.values.firstOrNull { it.getItemStack()?.isSimilar(itemStack) ?: false }?.id ?: "none"
    }
}
