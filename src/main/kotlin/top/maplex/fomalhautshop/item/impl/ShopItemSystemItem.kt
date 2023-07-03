package top.maplex.fomalhautshop.item.impl

import com.skillw.itemsystem.api.ItemAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem
import top.maplex.fomalhautshop.utils.getString

object ShopItemSystemItem : ShopItem {

    override val source: String = "IS"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("ItemSystem") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<ItemStack> {
        return mutableListOf()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return ItemAPI.productItem(id, player)?.apply {
            this.amount = amount
        }
    }

    override fun getItemId(itemStack: ItemStack): String {
        return if (itemStack.getString("ITEM_SYSTEM.key") == "null") {
            "none"
        } else {
            itemStack.getString("ITEM_SYSTEM.key")
        }
    }
}
