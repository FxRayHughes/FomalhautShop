package top.maplex.fomalhautshop.ui.main

import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.getItemStack
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.*
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.item.ShopItemData
import top.maplex.fomalhautshop.ui.UIReader
import top.maplex.fomalhautshop.ui.eval
import top.maplex.fomalhautshop.utils.asChar
import top.maplex.fomalhautshop.utils.papi

object UIShopGoodInfo {

    fun openInteractiveUI(player: Player, ui: String, element: ShopGoodsBaseData, edit: Boolean, query: String) {
        val config = UIReader.getUIConfig(ui)
        val uiName = config.getString("InteractiveMode.ChestName")!!
            .replace("{name}", element.getShowName(player))
        player.openMenu<Linked<ShopItemData>>(uiName) {
            map(*config.getStringList("InteractiveMode.Layout").toTypedArray())
            slotsBy(config.getString("InteractiveMode.Material", "@")!!.asChar())
            elements {
                element.buy?.items?.map {
                    ShopItemData(it)
                } ?: mutableListOf()
            }
            onGenerate { player, element, index, slot ->
                element.getItemAmount(player)
            }
            onClick { event, element ->
                event.isCancelled = true
            }
            config.getString("InteractiveMode.NextItem.slot", "G")?.asChar()?.let { nextChar ->
                this.setNextPage(getFirstSlot(nextChar)) { page, hasNextPage ->
                    if (hasNextPage) {
                        config.getItemStack("InteractiveMode.NextItem.has").papi(player,element)
                            ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                                name = "§f下一页"
                            }
                    } else {
                        config.getItemStack("InteractiveMode.NextItem.normal").papi(player,element)
                            ?: buildItem(XMaterial.ARROW) {
                                name = "§7下一页"
                            }
                    }
                }
            }
            config.getString("InteractiveMode.PreviousItem.slot")?.asChar()?.let { previoustChar ->
                this.setPreviousPage(getFirstSlot(previoustChar)) { page, hasPreviousPage ->
                    if (hasPreviousPage) {
                        config.getItemStack("InteractiveMode.PreviousItem.has").papi(player,element)
                            ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                                name = "§f上一页"
                            }
                    } else {
                        config.getItemStack("InteractiveMode.PreviousItem.normal").papi(player,element)
                            ?: buildItem(XMaterial.ARROW) {
                                name = "§7上一页"
                            }
                    }
                }

            }
            config.getConfigurationSection("InteractiveMode.OtherItem")?.getKeys(false)?.forEach { key ->
                config.getItemStack("InteractiveMode.OtherItem.${key}.item")?.papi(player,element)?.let {
                    set(key.asChar(), it) {
                        isCancelled = true
                        if (clickEvent().isLeftClick) {
                            if (clickEvent().isShiftClick) {
                                config.getStringList("InteractiveMode.OtherItem.${key}.action.left_shift").eval(player)
                                return@set
                            }
                            config.getStringList("InteractiveMode.OtherItem.${key}.action.left").eval(player)
                            return@set
                        }
                        if (clickEvent().isRightClick) {
                            if (clickEvent().isShiftClick) {
                                config.getStringList("InteractiveMode.OtherItem.${key}.action.right_shift").eval(player)
                                return@set
                            }
                            config.getStringList("InteractiveMode.OtherItem.${key}.action.right").eval(player)
                            return@set
                        }
                    }
                }
            }

            getSlots(config.getString("InteractiveMode.Commodity")!!.asChar()).forEach {
                set(it, element.goodsItem.getItem(player).clone()) {
                    isCancelled = true
                }
            }

            getSlots(config.getString("InteractiveMode.Back.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("InteractiveMode.Back.item")?.papi(player,element) ?: return@forEach) {
                    player.closeInventory()
                    submit(delay = 1) {
                        UIShopInfo.open(player, ui, edit, query)
                    }
                }
            }

            getSlots(config.getString("InteractiveMode.Quit.slot")!!.asChar()).forEach {
                set(it, config.getItemStack("InteractiveMode.Quit.item")?.papi(player,element) ?: return@forEach) {
                    player.closeInventory()
                }
            }


            if (element.buy?.enable == true || !config.getBoolean("InteractiveMode.Buy.hide")) {
                getSlots(config.getString("InteractiveMode.Buy.slot")!!.asChar()).forEach {
                    set(it, config.getItemStack("InteractiveMode.Buy.item")?.apply {
                        modifyLore {
                            element.buy?.getBuyLore(player)?.let { it1 -> addAll(it1) }
                        }
                    }?.papi(player,element) ?: return@forEach) {
                        player.closeInventory()
                        if (clickEvent().isLeftClick && clickEvent().isShiftClick) {
                            player.sendLang("chat-message-input-buy", element.name)
                            player.nextChat { z ->
                                if (z.contains("exit")) {
                                    openInteractiveUI(player, ui, element, edit, query)
                                    return@nextChat
                                }
                                val amount = z.toIntOrNull()
                                if (amount == null) {
                                    player.sendLang("chat-message-input-error")
                                    openInteractiveUI(player, ui, element, edit, query)
                                    return@nextChat
                                }
                                element.buy(player, amount)
                                openInteractiveUI(player, ui, element, edit, query)
                            }
                            return@set
                        }
                        if (clickEvent().isLeftClick) {
                            element.buy(player, 1)
                            openInteractiveUI(player, ui, element, edit, query)
                            return@set
                        }
                    }
                }
            }

            if (element.sell?.enable == true || !config.getBoolean("InteractiveMode.Sell.hide")) {
                getSlots(config.getString("InteractiveMode.Sell.slot")!!.asChar()).forEach {
                    set(it, config.getItemStack("InteractiveMode.Sell.item")?.apply {
                        modifyLore {
                            element.sell?.getSellLore(player, element.goodsItem)?.let { it1 -> addAll(it1) }
                        }
                    }?.papi(player,element) ?: return@forEach) {
                        player.closeInventory()
                        if (clickEvent().isRightClick && clickEvent().isShiftClick) {
                            player.sendLang("chat-message-input-sell", element.name)
                            player.nextChat { z ->
                                if (z.contains("exit")) {
                                    openInteractiveUI(player, ui, element, edit, query)
                                    return@nextChat
                                }
                                val amount = z.toIntOrNull()
                                if (amount == null) {
                                    player.sendLang("chat-message-input-error")
                                    openInteractiveUI(player, ui, element, edit, query)
                                    return@nextChat
                                }
                                element.sell(player, amount)
                                openInteractiveUI(player, ui, element, edit, query)
                            }
                            return@set
                        }
                        if (clickEvent().isRightClick) {
                            element.sell(player, 1)
                            openInteractiveUI(player, ui, element, edit, query)
                            return@set
                        }
                    }
                }
            }
        }
    }


}
