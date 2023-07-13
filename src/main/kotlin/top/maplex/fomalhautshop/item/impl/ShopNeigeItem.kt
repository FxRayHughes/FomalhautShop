package top.maplex.fomalhautshop.item.impl

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.manager.ItemManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem

object ShopNeigeItem : ShopItem {

    override val source: String = "NI"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("NeigeItems") != null) {
            registerItem()
        }
    }

    private val itemManager by lazy {
        ItemManager
    }

    override fun getItemList(): MutableList<ItemStack> {
        val player = Bukkit.getOnlinePlayers().first() ?: return mutableListOf()
        return itemManager.items.mapNotNull { it.value.getItemStack(player, "") }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        if (!id.contains(":")) {
            return itemManager.getItemStack(id, player)
        }
        val args = id.split(":")
        val idz = args[0]
        val arg = args.toList().drop(1)
        return itemManager.getItemStack(idz, player, toMap(arg))
    }

    fun toMap(list: List<String>): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        list.forEachIndexed { index, value ->
            if (index % 2 == 0) {
                map[value] = list[index + 1]
            } else {
                map[value] = ""
            }
        }
        return map
    }

    override fun getItemId(itemStack: ItemStack): String {
        return itemManager.isNiItem(itemStack)?.id ?: "none"
    }
}
