package top.maplex.fomalhautshop.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.countItem
import taboolib.platform.util.giveItem
import taboolib.platform.util.takeItem

interface ShopItem {

    /**
     * 物品来源的索引ID
     */
    val source: String

    /**
     * 物品列表
     */
    fun getItemList(): MutableList<ItemStack>

    /**
     * 获取物品
     * @param player 玩家
     * @param id 物品ID
     * @param amount 物品数量
     * @return 物品
     */
    fun getItem(player: Player, id: String): ItemStack?

    /**
     * 给予玩家物品
     * @param player 玩家
     * @param id 物品ID
     * @param amount 物品数量
     * @return 是否成功
     */
    fun giveItem(player: Player, id: String, amount: Int): Boolean {
        return getItem(player, id)?.let {
            player.giveItem(it, amount)
            true
        } ?: false
    }

    /**
     * 是否有物品
     * @param player 玩家
     * @param id 物品ID
     * @param amount 物品数量
     * @return 是否有物品
     */
    fun isMeet(player: Player, id: String, amount: Int): Boolean {
        return getNumber(player, id) >= amount
    }

    /**
     * 获取物品数量
     * @param player 玩家
     * @param id 物品ID
     * @param amount 物品数量
     * @return 是否有物品
     */
    fun getNumber(player: Player, id: String): Int {
        return player.inventory.countItem { getItemId(it) == id }
    }

    /**
     * 扣除物品
     * @param player 玩家
     * @param id 物品ID
     * @param amount 物品数量
     * @return 是否成功
     */
    fun takeItem(player: Player, id: String, amount: Int): Boolean {
        if (isMeet(player, id, amount)) {
            player.inventory.takeItem(amount) {
                getItemId(it) == id
            }
        }
        return false
    }

    /**
     * 获取物品的ID
     * @param itemStack 物品
     * @return 物品ID
     * @throws Exception 物品不是该来源的物品
     */
    fun getItemId(itemStack: ItemStack): String

    fun registerItem() {
        ShopItemManager.registerItem(this)
    }
}
