package top.maplex.fomalhautshop.ui.main

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.util.getStringColored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.giveItem
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.ui.UIReader
import top.maplex.fomalhautshop.ui.eval
import top.maplex.fomalhautshop.utils.asChar
import top.maplex.fomalhautshop.utils.ifAir

/**
 * 批量回收UI
 */
object UIShopSell {

    fun open(player: Player, shopGroups: List<String>) {
        val uiConfig = UIReader.getUIConfig("sell_ui")
        val goods = ShopManager.goods.filter { it.group.any { b -> shopGroups.contains(b) } && it.sell != null && it.sell!!.enable }
        player.openMenu<Basic>(uiConfig.getStringColored("Title") ?: "§f出售物品") {
            map(*uiConfig.getStringList("Layout").toTypedArray())
            uiConfig.getConfigurationSection("OtherItem")?.getKeys(false)?.forEach { key ->
                uiConfig.getItemStack("OtherItem.${key}.item")?.let {
                    set(key.asChar(), it) {
                        isCancelled = true
                        if (clickEvent().isLeftClick) {
                            if (clickEvent().isShiftClick) {
                                uiConfig.getStringList("OtherItem.${key}.action.left_shift").eval(player)
                                return@set
                            }
                            uiConfig.getStringList("OtherItem.${key}.action.left").eval(player)
                            return@set
                        }
                        if (clickEvent().isRightClick) {
                            if (clickEvent().isShiftClick) {
                                uiConfig.getStringList("OtherItem.${key}.action.right_shift").eval(player)
                                return@set
                            }
                            uiConfig.getStringList("OtherItem.${key}.action.right").eval(player)
                            return@set
                        }
                    }
                }
            }
            val slots = getSlots('@')
            onClose { event ->
                val items = slots.mapNotNull { event.inventory.getItem(it).ifAir() }.toMutableList()
                val back = mutableListOf<ItemStack>()
                items.forEach { itemStack ->
                    val goodsData = goods.firstOrNull { it.getGoodsItem().isSame(itemStack) }
                    if (goodsData == null) {
                        back.add(itemStack)
                        return@forEach
                    }
                    val sell = goodsData.sell ?: return@forEach
                    if (!sell.evalSellNotTake(player, itemStack.amount, goodsData.getGoodsItem(), goodsData)) {
                        back.add(itemStack)
                        return@forEach
                    }
                }

                if (back.isNotEmpty()) {
                    player.giveItem(back)
                }
            }

        }
    }

}
