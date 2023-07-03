package top.maplex.fomalhautshop.item.impl

import ink.ptms.um.Mythic
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem
import top.maplex.fomalhautshop.utils.getString

object ShopMMOItem : ShopItem {

    override val source: String = "MI"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<ItemStack> {
        return mutableListOf()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        val sub = id.split("::")
        val type = sub.getOrNull(0) ?: return null
        val item = sub.getOrNull(1) ?: return null
        val mmiType = MMOItems.plugin.types.get(type) ?: return null
        val mmoitem = MMOItems.plugin.getItem(mmiType, item)
        return mmoitem?.apply {
            this.amount = amount
        }
    }

    override fun getItemId(itemStack: ItemStack): String {
        return if (itemStack.getString("MMOITEMS_ITEM_ID") == "null") {
            "none"
        } else {
            "${itemStack.getString("MMOITEMS_ITEM_TYPE")}::${itemStack.getString("MMOITEMS_ITEM_ID")}"
        }
    }
}
