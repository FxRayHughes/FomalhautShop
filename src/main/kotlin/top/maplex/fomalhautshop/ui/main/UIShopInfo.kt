package top.maplex.fomalhautshop.ui.main

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.nextChat
import taboolib.platform.util.sendLang
import top.maplex.fomalhautshop.FomalhautShop
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.ui.UIReader
import top.maplex.fomalhautshop.ui.eval
import top.maplex.fomalhautshop.ui.inits

object UIShopInfo {

    //显示shop的页面
    fun open(player: Player, shopManagerData: String, editor: Boolean = false) {
        UIReader.scriptConfig["open-${shopManagerData}"]?.eval(player)
        submit {
            player.openMenu<Linked<ShopGoodsBaseData>>(shopManagerData.colored()) {
                init(shopManagerData, player, editor)
            }
        }
    }

    private fun Linked<ShopGoodsBaseData>.init(data: String, player: Player, editor: Boolean) {
        inits(data, player, editor)
        //
        elements {
            ShopManager.goods.filter { it.group.contains(data) }.sortedBy { it.weight }
        }
        onGenerate { looker, element, index, slot ->
            return@onGenerate buildItem(element.showItem(looker)) {

                if (element.shiny) {
                    if (element.buy?.checkBuy(player, 1, false) == true || element.sell!!.checkSell(
                            player,
                            1,
                            element.goodsItem
                        )
                    ) {
                        shiny()
                    }
                }
                colored()
            }
        }
        onClick { event, element ->
            //购买
            player.closeInventory()
            if (event.clickEvent().isLeftClick && event.clickEvent().isShiftClick) {
                player.sendLang("chat-message-input-buy", element.name)
                player.nextChat {
                    if (it.contains("exit")) {
                        open(player, data, editor)
                        return@nextChat
                    }
                    val amount = it.toIntOrNull()
                    if (amount == null) {
                        player.sendLang("chat-message-input-error")
                        open(player, data, editor)
                        return@nextChat
                    }
                    element.buy(player, amount)
                    open(player, data, editor)
                }
                return@onClick
            }
            if (event.clickEvent().isLeftClick) {
                element.buy(player, 1)
                open(player, data, editor)
                return@onClick
            }

            if (event.clickEvent().isRightClick && event.clickEvent().isShiftClick) {
                player.sendLang("chat-message-input-sell", element.name)
                player.nextChat {
                    if (it.contains("exit")) {
                        open(player, data, editor)
                        return@nextChat
                    }
                    val amount = it.toIntOrNull()
                    if (amount == null) {
                        player.sendLang("chat-message-input-error")
                        open(player, data, editor)
                        return@nextChat
                    }
                    element.sell(player, amount)
                    open(player, data, editor)
                }
                return@onClick
            }
            if (event.clickEvent().isRightClick) {
                element.sell(player, 1)
                open(player, data, editor)
                return@onClick
            }
        }

    }

}
