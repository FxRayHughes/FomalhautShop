package top.maplex.fomalhautshop.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.platform.util.hasLore
import taboolib.platform.util.hasName
import top.maplex.fomalhautshop.item.impl.ShopMinecraftItem
import top.maplex.fomalhautshop.item.itemlib.ItemSaveLib
import java.util.concurrent.ConcurrentHashMap

object ShopItemManager {

    val items = ConcurrentHashMap<String, ShopItem>()

    val cache = ConcurrentHashMap<String, ShopItemData>()


    fun toString(item: ItemStack): String {
        items.forEach { (t, u) ->
            if (t == ShopMinecraftItem.source) {
                if (!item.hasLore() && !item.hasName()) {
                    return "[$t] ${item.type} => ${item.amount}"
                }
            } else {
                val id = u.getItemId(item)
                if (id != "none") {
                    return "[$t] $id => ${item.amount}"
                }
            }
        }
        val saveId = System.currentTimeMillis().toString()
        ItemSaveLib.addItem(saveId, item)
        return "[FS] ${saveId} => ${item.amount}}"
    }

    fun registerItem(item: ShopItem) {
        items[item.source] = item
        info("注册物品来源 ${item.source}")
    }

    fun getData(i: String): ShopItem {
        val info = i.split(" ")
        val source = info[0].replace("[", "").replace("]", "")
        return this.items[source] ?: error("§c物品来源 $source 不存在")
    }

    fun getItem(string: String): ShopItemData {
        return cache.getOrPut(string) {
            ShopItemData(string)
        }
    }

    fun check(player: Player, items: List<String>): Boolean {
        val itemData = items.map { getItem(it) }
        for (i in itemData) {
            if (!i.isMeet(player)) {
                return false
            }
        }
        return true
    }

    fun take(player: Player, items: List<String>): Boolean {
        val itemData = items.map { getItem(it) }
        for (i in itemData) {
            i.take(player)
        }
        return true
    }

}
