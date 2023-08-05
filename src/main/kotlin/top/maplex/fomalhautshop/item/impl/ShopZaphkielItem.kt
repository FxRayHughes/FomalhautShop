package top.maplex.fomalhautshop.item.impl

import ink.ptms.zaphkiel.Zaphkiel
import ink.ptms.zaphkiel.impl.item.toExtensionStreamOrNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem

object ShopZaphkielItem : ShopItem {

    override val source: String = "ZAP"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("Zaphkiel") != null) {
            registerItem()
        }
    }

    private val itemManager by lazy {
        Zaphkiel.api().getItemManager()
    }

    override fun getItemList(): MutableList<ItemStack> {
        return mutableListOf()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return itemManager.getItem(id)?.buildItemStack(player)
    }

    override fun getItemId(itemStack: ItemStack): String {
        return itemStack.toExtensionStreamOrNull()?.getZaphkielId() ?: "none"
    }
}
