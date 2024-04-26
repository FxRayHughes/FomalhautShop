package top.maplex.fomalhautshop

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.io.newFile
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.discount.DiscountPlayerData
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.money.MoneyAPI
import top.maplex.fomalhautshop.reader.ShopOldReader
import top.maplex.fomalhautshop.reader.ShopReader
import top.maplex.fomalhautshop.ui.UIReader
import top.maplex.fomalhautshop.ui.edit.UIGoodsEdit
import top.maplex.fomalhautshop.ui.main.UIShopInfo
import top.maplex.fomalhautshop.ui.main.UIShopSell
import top.maplex.fomalhautshop.utils.flattenList

@CommandHeader(name = "fomalhautShop", aliases = ["fs", "shop"], permission = "shop.use")
object ShopMainCommand {

    @CommandBody(permission = "shop.use")
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permission = "shop.open")
    val open = subCommand {
        dynamic("商店名") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                ShopManager.goods.map { it.group }.flattenList()
            }
            player("目标玩家") {
                execute<CommandSender> { sender, context, argument ->
                    Bukkit.getPlayer(context.player("目标玩家").uniqueId)
                        ?.let { UIShopInfo.open(it, context["商店名"]) }
                }
            }
            execute<Player> { sender, context, argument ->
                UIShopInfo.open(sender, context["商店名"])
            }
        }
    }

    @CommandBody(permission = "shop.open")
    val openQuery = subCommand {
        dynamic("商店名") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                ShopManager.goods.map { it.group }.flattenList()
            }
            dynamic("查询关键字") {
                player("目标玩家") {
                    execute<CommandSender> { sender, context, argument ->
                        Bukkit.getPlayer(context.player("目标玩家").uniqueId)
                            ?.let { UIShopInfo.open(it, context["商店名"], false, context["查询关键字"]) }
                    }
                }
                execute<Player> { sender, context, argument ->
                    UIShopInfo.open(sender, context["商店名"], false, context["查询关键字"])
                }
            }
        }
    }

    @CommandBody(permission = "shop.buy")
    val buy = subCommand {
        dynamic("商品ID") {
            suggestion<CommandSender> { sender, context ->
                ShopManager.goods.map { it.id }
            }
            int("数量") {
                player("目标玩家") {
                    execute<CommandSender> { sender, context, argument ->
                        val shop = ShopManager.goods.firstOrNull { it.id == context["商品ID"] } ?: return@execute
                        val player = Bukkit.getPlayer(context.player("目标玩家").uniqueId) ?: return@execute
                        shop.buy(player, context.int("数量"))
                    }
                }
                execute<Player> { sender, context, argument ->
                    val shop = ShopManager.goods.firstOrNull { it.id == context["商品ID"] } ?: return@execute
                    shop.buy(sender, context.int("数量"))
                }
            }
        }
    }

    @CommandBody(permission = "shop.sell")
    val sell = subCommand {
        dynamic("商品ID") {
            suggestion<CommandSender> { sender, context ->
                ShopManager.goods.map { it.id }
            }
            int("数量") {
                player("目标玩家") {
                    execute<CommandSender> { sender, context, argument ->
                        val shop = ShopManager.goods.firstOrNull { it.id == context["商品ID"] } ?: return@execute
                        val player = Bukkit.getPlayer(context.player("目标玩家").uniqueId) ?: return@execute
                        shop.sell(player, context.int("数量"))
                    }
                }
                execute<Player> { sender, context, argument ->
                    val shop = ShopManager.goods.firstOrNull { it.id == context["商品ID"] } ?: return@execute
                    shop.sell(sender, context.int("数量"))
                }
            }
        }
    }

    @CommandBody(permission = "shop.sellui")
    val sellUI = subCommand {
        dynamic("商店") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                ShopManager.goods.map { it.group }.flattenList()
            }
            player("目标玩家") {
                execute<CommandSender> { sender, context, argument ->
                    val group = context["商店"].split(",").toList()
                    val player = Bukkit.getPlayer(context.player("目标玩家").uniqueId) ?: return@execute
                    UIShopSell.open(player, group)
                }
            }
            execute<Player> { sender, context, argument ->
                val group = context["商店"].split(",").toList()
                UIShopSell.open(sender, group)
            }
        }
    }

    @CommandBody(permission = "shop.reload")
    val reload = subCommand {
        execute<CommandSender> { sender, context, argument ->
            UIReader.load()
            MoneyAPI.moneyConfig.reload()
            UIShopInfo.config.reload()
            FomalhautShop.config.reload()
            DiscountPlayerData.config.reload()
            sender.sendMessage("§a重载成功")
        }
    }

    @CommandBody(permission = "shop.edit")
    val edit = subCommand {
        dynamic("Shop") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                ShopManager.goods.map { it.id }
            }
            execute<Player> { sender, context, argument ->
                val shop = ShopManager.goods.firstOrNull { it.id == context["Shop"] }
                if (shop == null) {
                    ShopGoodsBaseData(context["Shop"], mutableListOf(), "", "[MC] apple => 1").let {
                        it.path = "shops/create/${it.id}.yml"
                        ShopManager.goods.add(it)
                        ShopReader.save()
                        UIGoodsEdit.open(sender, it)
                    }
                    return@execute
                }
                UIGoodsEdit.open(sender, shop)
            }
        }
    }

    @CommandBody(permission = "shop.edit")
    val editList = subCommand {
        dynamic("商店名") {
            suggestion<CommandSender>(uncheck = true) { sender, context ->
                ShopManager.goods.map { it.group }.flattenList()
            }
            execute<Player> { sender, context, argument ->
                UIGoodsEdit.openEditList(sender, context["商店名"])
            }
        }
        execute<Player> { sender, context, argument ->
            UIGoodsEdit.openEditList(sender)
        }
    }

    @CommandBody(permission = "shop.remove")
    val remove = subCommand {
        dynamic("Shop") {
            suggestion<CommandSender> { sender, context ->
                ShopManager.goods.map { it.id }
            }
            execute<CommandSender> { sender, context, argument ->
                val shop = ShopManager.goods.firstOrNull { it.id == context["Shop"] } ?: return@execute
                shop.delete()
                sender.sendMessage("删除完成 已移动到回收站")
            }
        }
    }

    @CommandBody(permission = "shop.remove")
    val clearDiscount = subCommand {
        execute<CommandSender> { sender, context, argument ->
            DiscountPlayerData.config.clear()
            DiscountPlayerData.config.saveToFile()
            sender.sendMessage("删除完成")
        }
    }

    @CommandBody(permission = "shop.get")
    val get = subCommand {
        dynamic("Shop") {
            suggestion<CommandSender> { sender, context ->
                ShopManager.goods.map { it.id }
            }
            execute<CommandSender> { sender, context, argument ->
                val shop = ShopManager.goods.firstOrNull { it.id == context["Shop"] } ?: return@execute
                sender.sendMessage(newFile(shop.path).name)
                sender.sendMessage(shop.toString())
            }
        }
    }

    @CommandBody(permission = "shop.old")
    val fromOld = subCommand {
        execute<CommandSender> { sender, context, argument ->
            ShopOldReader.eval()
            ShopReader.save()
            sender.sendMessage("§a转换成功")
        }
    }

}
