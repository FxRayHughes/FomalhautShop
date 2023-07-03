package top.maplex.fomalhautshop.ui.edit

import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem
import taboolib.platform.util.inputBook
import taboolib.platform.util.nextChat
import taboolib.platform.util.sendLang
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.reader.ShopReader
import top.maplex.fomalhautshop.ui.edit.UIGoodsEdit.itemA

object UIGoodsSellEdit {

    fun open(player: Player, goods: ShopGoodsBaseData) {
        player.openMenu<Basic>("编辑商品: ${goods.id} 收购模块") {
            map(
                "Q###A###Z",
                "#B#C#D#E#",
                "#F#G#H#I#",
                "#########",
            )
            itemA(player, goods, 'A')
            setMoney(player, goods, 'B')
            setMoneyType(player, goods, 'C')
            setPermission(player, goods, 'D')
            setLimit(player, goods, 'E')
            setScript(player, goods, 'F')

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

    private fun Basic.setScript(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.BOOK) {
            name = "&f设置收购后执行脚本"

            lore.add("&f当前脚本:")
            lore.addAll(goods.sell!!.script.map { "&f$it" })
            lore.add(" ")

            lore.add("&f左键编辑")
            lore.add("&f右键清空")
            colored()
        }) {
            player.closeInventory()
            if (clickEvent().isRightClick) {
                goods.sell!!.script.clear()
                submit(delay = 1) {
                    open(player, goods)
                }
            } else {
                player.inputBook("输入脚本", true, goods.sell!!.script) {
                    goods.sell!!.script = it.toMutableList()
                    submit(delay = 1) {
                        open(player, goods)
                    }
                }
            }
        }
    }


    private fun Basic.setPermission(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.DIAMOND) {
            name = "&f设置收购权限"
            lore.add("&f当前收购权限: &a${goods.sell!!.permission}")
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
                goods.sell!!.permission = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

    private fun Basic.setMoney(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.GOLD_INGOT) {
            name = "&f设置价格"
            lore.add("&f当前价格: &a${goods.sell!!.money}")
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
                goods.sell!!.money = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

    private fun Basic.setLimit(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.TORCH) {
            name = "&f设置限售"
            lore.add("&f当前限售: &a${goods.sell!!.limit}")
            lore.add("&f当前限售组: &c${goods.sell!!.limitId}")
            lore.add("&f左键输入限售数量")
            lore.add("&f右键输入限售组")
            colored()
        }) {
            player.closeInventory()
            if (clickEvent().isLeftClick) {
                player.sendMessage("请输入限售数量")
                player.nextChat {
                    val money = it.toIntOrNull()
                    if (money == null) {
                        player.sendMessage("输入错误退出捕获")
                        return@nextChat
                    }
                    goods.sell!!.limit = money
                    player.sendLang("chat-message-input-edit-set-good-success", money)
                    open(player, goods)
                }
            } else {
                player.sendMessage("请输入限售组")
                player.nextChat {
                    val money = it
                    if (money == "exit") {
                        player.sendMessage("输入错误退出捕获")
                        return@nextChat
                    }
                    goods.sell!!.limitId = money
                    player.sendLang("chat-message-input-edit-set-good-success", money)
                    open(player, goods)
                }
            }
        }
    }

    private fun Basic.setMoneyType(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.MAP) {
            name = "&f设置货币类型"
            lore.add("&f当前类型: &a${goods.sell!!.moneyType}")
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
                goods.sell!!.moneyType = money
                player.sendLang("chat-message-input-edit-set-good-success", money)
                open(player, goods)
            }
        }
    }

}
