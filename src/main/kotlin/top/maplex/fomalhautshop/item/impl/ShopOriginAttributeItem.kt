package top.maplex.fomalhautshop.item.impl

import ac.github.oa.internal.core.item.ItemInstance
import ac.github.oa.internal.core.item.ItemPlant
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import top.maplex.fomalhautshop.item.ShopItem

object ShopOriginAttributeItem : ShopItem {

    override val source: String = "OA"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("OriginAttribute") != null) {
            registerItem()
        }
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        if (!id.contains(":")) {
            return ItemPlant.build(player, id)
        }
        val args = id.split(":")
        val idz = args[0]
        val arg = args.toList().drop(1)
        return ItemPlant.build(player, idz, toMap(arg))
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
        return ItemInstance.get(itemStack)?.item?.id ?: "none"
    }
}
