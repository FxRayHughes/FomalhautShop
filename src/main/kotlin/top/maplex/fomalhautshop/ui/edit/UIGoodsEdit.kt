package top.maplex.fomalhautshop.ui.edit

import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.util.*
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.data.goods.ShopGoodsBuyData
import top.maplex.fomalhautshop.data.goods.ShopGoodsSellData
import top.maplex.fomalhautshop.item.ShopItem
import top.maplex.fomalhautshop.item.ShopItemManager
import top.maplex.fomalhautshop.reader.ShopReader
import top.maplex.fomalhautshop.utils.papi

object UIGoodsEdit {

    fun openEditList(player: Player, good: String? = null) {
        submit {
            player.openMenu<Linked<ShopGoodsBaseData>> {
                elements {
                    if (good == null) {
                        ShopManager.goods
                    } else {
                        ShopManager.goods.filter { it.group.contains(good) }
                    }
                }

                onGenerate { player, element, index, slot ->
                    element.showItem(player, false)
                }

                onClick { event, element ->
                    player.closeInventory()
                    open(player, element)
                }
                rows(6)
                slots(Slots.CENTER)
                setNextPage(51) { page, hasNextPage ->
                    if (hasNextPage) {
                        buildItem(XMaterial.SPECTRAL_ARROW) {
                            name = "§f下一页"
                        }
                    } else {
                        buildItem(XMaterial.ARROW) {
                            name = "§7下一页"
                        }
                    }
                }
                setPreviousPage(47) { page, hasPreviousPage ->
                    if (hasPreviousPage) {
                        buildItem(XMaterial.SPECTRAL_ARROW) {
                            name = "§f上一页"
                        }
                    } else {
                        buildItem(XMaterial.ARROW) {
                            name = "§7上一页"
                        }
                    }
                }
            }
        }
    }

    fun open(player: Player, goods: ShopGoodsBaseData) {
        submit {
            player.closeInventory()
            player.openMenu<Basic>("编辑模式: ${goods.id}") {
                map(
                    "Q###A###Z",
                    "#B#C#D#E#",
                    "#F#G#H#I#",
                    "#########",
                )
                itemA(player, goods, 'A')
                itemGroup(player, goods, 'B')
                itemName(player, goods, 'C')
                itemWeight(player, goods, 'D')
                itemInfo(player, goods, 'E')
                itemBuy(player, goods, 'F')
                itemSell(player, goods, 'G')
                setShiny(player, goods, 'H')
                remove(player, goods, 'I')
                onClose {
                    ShopReader.saveOne(goods)
                }
            }
        }

    }

    private fun Basic.remove(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.BARRIER) {
            name = "&f删除商店"
            lore.add("&f点击删除")
            colored()
        }) {
            player.closeInventory()
            goods.delete()
            submit(delay = 1) {
                openEditList(player)
            }
        }
    }

    private fun Basic.setShiny(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(if (goods.shiny) XMaterial.TORCH else XMaterial.REDSTONE_TORCH) {
            name = "&f是否高亮显示"
            lore.add("&f当前状态: ${if (goods.shiny) "&a是" else "&c否"}")
            lore.add("&f左键切换状态")
            colored()
        }) {
            player.closeInventory()
            goods.shiny = !goods.shiny
            submit(delay = 1) {
                open(player, goods)
            }
        }
    }

    private fun Basic.itemBuy(player: Player, goods: ShopGoodsBaseData, c: Char) {
        set(c, buildItem(XMaterial.CHEST) {
            name = "&f购买原件"
            lore.add("&f当前状态: ${if (goods.buy == null || !goods.buy!!.enable) "&c未启用" else "&a已启用"}")
            lore.add("&f左键切换状态")
            lore.add("&f右键编辑购买逻辑")
            colored()
        }) {
            player.closeInventory()
            if (clickEvent().isLeftClick) {
                if (goods.buy == null) {
                    goods.buy = ShopGoodsBuyData(false)
                }
                goods.buy!!.enable = !goods.buy!!.enable
                submit(delay = 1) {
                    open(player, goods)
                }
                return@set
            } else {
                submit(delay = 1) {
                    if (goods.buy == null) {
                        goods.buy = ShopGoodsBuyData(false)
                    }
                    UIGoodsBuyEdit.open(player, goods)
                }
            }
        }
    }

    private fun Basic.itemSell(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.CHEST) {
            name = "&f出售原件"
            lore.add("&f当前状态: ${if (goods.sell == null || !goods.sell!!.enable) "&c未启用" else "&a已启用"}")
            lore.add("&f左键切换状态")
            lore.add("&f右键编辑出售逻辑")
            colored()
        }) {
            player.closeInventory()
            if (clickEvent().isLeftClick) {
                if (goods.sell == null) {
                    goods.sell = ShopGoodsSellData()
                }
                goods.sell!!.enable = !goods.sell!!.enable
                submit(delay = 1) {
                    open(player, goods)
                }
                return@set
            } else {
                submit(delay = 1) {
                    if (goods.sell == null) {
                        goods.sell = ShopGoodsSellData()
                    }
                    UIGoodsSellEdit.open(player, goods)
                }
            }
        }
    }

    private fun Basic.itemInfo(player: Player, goods: ShopGoodsBaseData, c: Char) {
        set(c, buildItem(XMaterial.BOOK) {
            name = "&f描述: &e${goods.info.size}行"
            lore.add("&f当前额外描述:")
            lore.addAll(goods.info.map { "&f- &e$it" })
            colored()
        }) {
            player.closeInventory()
            player.inputBook("输入描述", true, goods.info) {
                goods.info = it.map { "&f${it}".colored() }.toMutableList()
                submit(delay = 1) {
                    open(player, goods)
                }
            }
        }
    }


    private fun Basic.itemWeight(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.LADDER) {
            name = "&f优先级: &e${goods.weight}"
            colored()
        }) {
            player.closeInventory()
            player.sendLang("chat-message-input-weight")
            player.nextChat {
                val weight = it.toIntOrNull()
                if (weight == null) {
                    player.sendLang("chat-message-input-error")
                    submit(delay = 1) {
                        open(player, goods)
                    }
                } else {
                    goods.weight = weight
                    player.sendLang("chat-message-input-edit-set-good-success", weight)
                    submit(delay = 1) {
                        open(player, goods)
                    }
                }
            }
        }
    }


    fun Basic.itemA(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, goods.showItem(player, false).apply {
            modifyLore {

                add("&f${goods.goods}")
                add(" ")
                add("&f左键设置来源 (UI选择)")
                add("&f右键放入物品自动识别")
                add("")
            }
        }.papi(player)!!) {
            player.closeInventory()
            if (clickEvent().isRightClick) {
                inputItem(player, goods)
            }
            submit {
                setGoods(player, goods)
            }
        }
    }

    private fun Basic.inputItem(player: Player, goods: ShopGoodsBaseData) {
        player.closeInventory()
        submit(delay = 1) {
            player.openMenu<Basic>("放入物品自动识别") {
                map("####@####")
                handLocked(false)
                set('#', buildItem(XMaterial.BLACK_STAINED_GLASS_PANE) {
                    name = " "
                    colored()
                }) {
                    isCancelled = true
                }
                onClick(lock = false)
                onClose {
                    val item = it.inventory.getItem(4) ?: return@onClose kotlin.run {
                        player.sendLang("chat-message-input-error")
                        submit(delay = 1) {
                            open(player, goods)
                        }
                    }
                    if (item.isAir) {
                        player.sendLang("chat-message-input-error")
                        submit(delay = 1) {
                            open(player, goods)
                        }
                        return@onClose
                    }
                    ShopItemManager.toString(item).let { it1 ->
                        goods.goods = it1
                    }
                    player.sendLang("chat-message-input-edit-set-good-success", goods.goods)
                    submit(delay = 1) {
                        open(player, goods)
                    }
                }
            }
        }
    }

    private fun Basic.itemName(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.NAME_TAG) {
            name = "&f设置名称: ${goods.name}"
            lore.add(" ")
            lore.add("&f点击设置名称")
            colored()
        }) {
            player.closeInventory()
            player.sendLang("chat-message-input-edit-set-name")
            player.nextChat { ins ->
                if (ins == "exit") {
                    player.sendLang("chat-message-input-error")
                    return@nextChat open(player, goods)
                }
                goods.name = ins
                player.sendLang("chat-message-input-edit-set-name-success", goods.name)
                open(player, goods)
            }
        }
    }

    private fun Basic.itemGroup(player: Player, goods: ShopGoodsBaseData, char: Char) {
        set(char, buildItem(XMaterial.BEETROOT_SEEDS) {
            name = "&f设置组:"
            lore.add(" ")
            lore.add("&f当前组:")
            lore.addAll(goods.group.map { "&7- &f$it" })
            lore.add(" ")
            lore.add("&f点击设置组")
            lore.add("&f一个商品允许多个组")
            colored()
        }) {
            player.closeInventory()
            player.title("编辑书本进行设置", "换行视为一个新的组")
            player.inputBook("设置组", disposable = true, goods.group) { book ->
                goods.group = book.toMutableList()
                open(player, goods)
            }
        }
    }


    fun setGoods(player: Player, goods: ShopGoodsBaseData) {
        player.openMenu<Linked<ShopItem>>("设置商品来源: ${goods.id}") {
            map(
                "#########",
                "#@@@@@@@#",
                "#@@@@@@@#",
                "#@@@@@@@#",
                "#@@@@@@@#",
                "##A###B##",
            )
            slotsBy('@')
            elements {
                ShopItemManager.items.values.toList()
            }
            onGenerate { player, element, index, slot ->
                buildItem(Material.PAPER) {
                    name = "&f源头: ${element.source}"
                    lore.add(" ")
                    lore.add("&f 设置为本源的物品 ")
                    colored()
                }
            }

            onClick { event, element ->
                player.closeInventory()
                player.sendLang("chat-message-input-edit-set-good", element.source)
                player.nextChat { id ->
                    if (id == "exit") {
                        return@nextChat run {
                            submit {
                                open(player, goods)
                            }
                        }
                    }
                    player.sendLang("chat-message-input-edit-set-good-amount")
                    player.nextChat z@{ amount ->
                        val am = amount.toIntOrNull() ?: 0
                        if (am <= 0) {
                            player.sendLang("chat-message-input-error")
                            return@z run {
                                submit {
                                    open(player, goods)
                                }
                            }
                        }
                        goods.goods = "[${element.source}] ${id} => ${am}"
                        player.sendLang("chat-message-input-edit-set-good-success", goods.goods)
                        open(player, goods)
                    }
                }
            }

        }
    }

}
