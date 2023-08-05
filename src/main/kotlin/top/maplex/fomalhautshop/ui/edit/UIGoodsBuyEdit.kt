package top.maplex.fomalhautshop.ui.edit

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.*
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.item.ShopItemData
import top.maplex.fomalhautshop.item.ShopItemManager
import top.maplex.fomalhautshop.reader.ShopReader
import top.maplex.fomalhautshop.ui.edit.UIGoodsEdit.itemA

object UIGoodsBuyEdit {

    fun open(player: Player, goods: ShopGoodsBaseData) {
        submit {
            player.openMenu<Basic>("编辑商品: ${goods.id} 购买模块") {
                map(
                    "Q###A###Z",
                    "#B#C#D#E#",
                    "#F#G#H#I#",
                    "#########",
                )
                itemA(player, goods, 'A')
                setMoney(player, goods, 'B')
                setItem(player, goods, 'D')
                setMoneyType(player, goods, 'C')
                setGive(player, goods, 'E')
                setDiscount(player, goods, 'F')
                setPermission(player, goods, 'G')
                setLimit(player, goods, 'H')
                setScript(player, goods, 'I')

                set('Q', buildItem(XMaterial.OAK_DOOR) {
                    name = "&f返回"
                    colored()
                }) {
                    player.closeInventory()
                    UIGoodsEdit.open(player, goods)
                }

                onClose {
                    ShopReader.saveOne(goods)
                }
            }
        }
    }

    private fun Basic.setScript(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.BOOK) {
            name = "&f设置购买后执行脚本"

            lore.add("&f当前脚本:")
            lore.addAll(goods.buy!!.script.map { "&f$it" })
            lore.add(" ")

            lore.add("&f左键编辑")
            lore.add("&f右键清空")
            colored()
        }) {
            player.closeInventory()
            if (clickEvent().isRightClick) {
                goods.buy!!.script.clear()
                submit(delay = 1) {
                    open(player, goods)
                }
            } else {
                player.inputBook("输入脚本", true, goods.buy!!.script) {
                    goods.buy!!.script = it.toMutableList()
                    submit(delay = 1) {
                        open(player, goods)
                    }
                }
            }
        }
    }


    private fun Basic.setGive(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.DROPPER) {
            name = "&f是否给予物品"
            lore.add("&f当前状态: ${if (goods.buy!!.give) "&a是" else "&c否"}")
            lore.add("&f左键切换状态")
            colored()
        }) {
            player.closeInventory()
            goods.buy!!.give = !goods.buy!!.give
            submit(delay = 1) {
                open(player, goods)
            }
        }
    }

    private fun Basic.setPermission(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.DIAMOND) {
            name = "&f设置购买权限"
            lore.add("&f当前购买权限: &a${goods.buy!!.permission}")
            lore.add("&f点击输入")
            colored()
        }) {
            player.closeInventory()
            player.sendMessage("请输入 (exit退出)")
            player.nextChat {
                val money = it
                if (money == "exit") {
                    player.sendMessage("输入错误退出捕获")
                    return@nextChat
                }
                goods.buy!!.permission = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

    private fun Basic.setDiscount(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.PAPER) {
            name = "&f设置折扣组"
            lore.add("&f当前折扣组: &a${goods.buy!!.discount}")
            lore.add("&f点击输入")
            colored()
        }) {
            player.closeInventory()
            player.sendMessage("请输入 (exit退出)")
            player.nextChat {
                val money = it
                if (money == "exit") {
                    player.sendMessage("输入错误退出捕获")
                    return@nextChat
                }
                goods.buy!!.discount = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

    private fun Basic.setItem(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.CHEST) {
            name = "&f设置物品"
            lore.add("&f当前物品: ")
            lore.addAll(goods.buy!!.items.map { "&7-&f $it" })
            colored()
        }) {
            player.closeInventory()
            player.openMenu<Basic>("物品捕获") {
                rows(6)
                onBuild { player, inventory ->
                    goods.buy!!.items.forEach {
                        val item = ShopItemManager.getItem(it)
                        repeat(item.amount) {
                            inventory.addItem(item.getItem(player))
                        }
                    }
                }
                onClose {
                    val inv = it.inventory
//                    {
//                        val items = mutableListOf<String>()
//                        for (i in 0 until inv.size) {
//                            val item = inv.getItem(i) ?: continue
//                            items.add(ShopItemManager.toString(item))
//                        }
//                        goods.buy!!.items = items
//                    }
                    val items = mutableMapOf<String, ShopItemData>()
                    for (i in 0 until inv.size) {
                        val item = inv.getItem(i) ?: continue

                        val shopItemData = ShopItemData(ShopItemManager.toString(item))
                        val mapId = "${shopItemData.type}__${shopItemData.id}}"
                        if (items.containsKey(mapId)) {
                            items[mapId]!!.amount += shopItemData.amount
                        } else {
                            items[mapId] = shopItemData
                        }
                    }
                    goods.buy!!.items = items.values.map {z-> z.toStringValue() }.toMutableList()
                    submit(delay = 2) {
                        open(player, goods)
                    }
                }
            }
        }
    }

    private fun Basic.setMoney(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.GOLD_INGOT) {
            name = "&f设置价格"
            lore.add("&f当前价格: &a${goods.buy!!.money}")
            lore.add("&f点击输入价格")
            colored()
        }) {
            player.closeInventory()
            player.sendMessage("请输入价格")
            player.nextChat {
                val money = it.toDoubleOrNull()
                if (money == null) {
                    player.sendMessage("输入错误退出捕获")
                    return@nextChat
                }
                goods.buy!!.money = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

    private fun Basic.setLimit(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.TORCH) {
            name = "&f设置限购"
            lore.add("&f当前限购: &a${goods.buy!!.limit}")
            lore.add("&f当前限购组: &c${goods.buy!!.limitId}")
            lore.add("&f左键输入限购数量")
            lore.add("&f右键输入限购组")
            colored()
        }) {
            player.closeInventory()
            if (clickEvent().isLeftClick) {
                player.sendMessage("请输入限购数量")
                player.nextChat {
                    val money = it.toIntOrNull()
                    if (money == null) {
                        player.sendMessage("输入错误退出捕获")
                        return@nextChat
                    }
                    goods.buy!!.limit = money
                    player.sendLang("chat-message-input-edit-set-good-success", money)
                    open(player, goods)
                }
            } else {
                player.sendMessage("请输入限购组")
                player.nextChat {
                    val money = it
                    if (money == "exit") {
                        player.sendMessage("输入错误退出捕获")
                        return@nextChat
                    }
                    goods.buy!!.limitId = money
                    player.sendLang("chat-message-input-edit-set-good-success", money)
                    open(player, goods)
                }
            }
        }
    }

    private fun Basic.setMoneyType(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.MAP) {
            name = "&f设置货币类型"
            lore.add("&f当前类型: &a${goods.buy!!.moneyType}")
            lore.add("&f点击输入货币类型")
            colored()
        }) {
            player.closeInventory()
            player.sendMessage("请输入货币类型 (exit退出)")
            player.nextChat {
                val money = it
                if (money == "exit") {
                    player.sendMessage("输入错误退出捕获")
                    return@nextChat
                }
                goods.buy!!.moneyType = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

}
