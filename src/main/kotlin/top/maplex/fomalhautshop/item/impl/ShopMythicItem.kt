package top.maplex.fomalhautshop.item.impl

import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem

object ShopMythicItem : ShopItem {

    override val source: String = "MM"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<ItemStack> {
        return Mythic.API.getItemList().map { it.generateItemStack(1) }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return Mythic.API.getItemStack(id)
    }

    override fun getItemId(itemStack: ItemStack): String {
        return Mythic.API.getItemId(itemStack) ?: "none"
    }
}
