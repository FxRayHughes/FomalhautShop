package top.maplex.fomalhautshop.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.colored
import taboolib.module.nms.getName

data class ShopItemData(
    var source: String,
    var type: String,
    var id: String,
    var amount: Int
) {

    constructor(info: String) : this(source = info, "", "", 0) {
        val args = info.split(" ")
        type = args[0].replace("[", "").replace("]", "")
        id = args[1]
        amount = args[3].toIntOrNull() ?: 1
    }

    private val item by lazy {
        ShopItemManager.getData(type)
    }

    fun getItem(player: Player): ItemStack {
        return item.getItem(player, id) ?: error("物品不存在 ${source}")
    }

    fun getShowName(player: Player): String {
        return getItem(player).getName(player)
    }

    fun getShowString(player: Player): String {
        return "&f${getItem(player).getName(player)} &fX ${amount}".colored()
    }

    fun isMeet(player: Player): Boolean {
        return item.isMeet(player, id, amount)
    }

    fun take(player: Player): Boolean {
        return item.takeItem(player, id, amount)
    }

    fun getNumber(player: Player): Int {
        return item.getNumber(player, id)
    }
}
