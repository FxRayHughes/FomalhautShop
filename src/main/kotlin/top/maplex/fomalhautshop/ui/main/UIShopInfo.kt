package top.maplex.fomalhautshop.ui.main

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.util.getStringColored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem
import taboolib.platform.util.nextChat
import taboolib.platform.util.sendLang
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.ui.UIReader
import top.maplex.fomalhautshop.ui.eval
import top.maplex.fomalhautshop.ui.inits
import top.maplex.fomalhautshop.utils.check

object UIShopInfo {

    @Config(value = "shops.yml")
    lateinit var config: ConfigFile

    //显示shop的页面
    fun open(player: Player, shopManagerData: String, editor: Boolean = false, query: String = "") {
        UIReader.scriptConfig["open-${shopManagerData}"]?.eval(player)
        submit {
            val name = config.getStringColored(shopManagerData)?.replacePlaceholder(player) ?: shopManagerData
            player.openMenu<Linked<ShopGoodsBaseData>>(name.colored()) {
                init(shopManagerData, player, editor, query)
            }
        }
    }

    private fun Linked<ShopGoodsBaseData>.init(data: String, player: Player, editor: Boolean, query: String = "") {
        inits(data, player, editor)
        //
        elements {
            if (query.isNotEmpty()) {
                ShopManager.goods.filter {
                    (it.group.contains(data) && (it.id.contains(query)
                        || it.getGoodsItem().getShowName(player).contains(query)))
                        && it.show.check(player).get() == true
                }.sortedByDescending { it.weight }
            } else {
                ShopManager.goods.filter { it.group.contains(data) && it.show.check(player).get() == true }.sortedByDescending { it.weight }
            }
        }
        onGenerate { looker, element, index, slot ->
            return@onGenerate buildItem(element.showItem(looker).clone()) {

                if (element.shiny) {
                    if ((element.buy?.enable == true && element.buy?.checkBuy(player, 1,element.getGoodsItem(), false) == true)
                        || (element.sell?.enable == true && element.sell?.checkSell(
                            player,
                            1,
                            element.getGoodsItem()
                        ) == true)
                    ) {
                        shiny()
                    }
                }
                colored()

            }
        }
        onClick { event, element ->

            if (event.clickEvent().click == ClickType.DROP) {
                submit {
                    player.closeInventory()
                    UIShopGoodInfo.openInteractiveUI(player, data, element, false, query)
                }
                return@onClick
            }
            //购买
            if (element.buy?.enable == true) {
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
            }
            if (element.sell?.enable == true) {
                player.closeInventory()
                if (event.clickEvent().isRightClick && event.clickEvent().isShiftClick) {
                    player.sendLang("chat-message-input-sell", element.name)
                    player.nextChat {
                        if (it.contains("exit")) {
                            open(player, data, editor)
                            return@nextChat
                        }
                        var amount = it.toIntOrNull()
                        if (it == "all") {
                            amount = element.sell!!.getAmount(element.getGoodsItem(), player)
                        }
                        if (amount == null || amount == 0) {
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

}
