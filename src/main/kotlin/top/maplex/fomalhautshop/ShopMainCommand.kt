package top.maplex.fomalhautshop

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.io.newFile
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.reader.ShopOldReader
import top.maplex.fomalhautshop.reader.ShopReader
import top.maplex.fomalhautshop.ui.UIReader
import top.maplex.fomalhautshop.ui.edit.UIGoodsBuyEdit
import top.maplex.fomalhautshop.ui.edit.UIGoodsEdit
import top.maplex.fomalhautshop.ui.main.UIShopInfo
import top.maplex.fomalhautshop.utils.flattenList

@CommandHeader(name = "fomalhautShop", aliases = ["fs", "shop"], permission = "shop.use")
object ShopMainCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
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

    @CommandBody(permission = "shop.reload")
    val reload = subCommand {
        execute<Player> { sender, context, argument ->
            UIReader.load()
            sender.sendMessage("§a重载成功")
        }
    }

    @CommandBody
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

    @CommandBody
    val editList = subCommand {
        execute<Player> { sender, context, argument ->
            UIGoodsEdit.openEditList(sender)
        }
    }

    @CommandBody
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

    @CommandBody
    val fromOld = subCommand {
        execute<CommandSender> { sender, context, argument ->
            ShopOldReader.eval()
            ShopReader.save()
            sender.sendMessage("§a转换成功")
        }
    }

}

