package top.maplex.fomalhautshop.item.itemlib

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.platform.util.giveItem
import taboolib.platform.util.isAir

@CommandHeader("itemsave", aliases = ["sis"], permission = "itemsave.use")
object ItemSaveCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val save = subCommand {
        dynamic("物品名") {
            execute<Player> { sender, context, argument ->
                val item = sender.inventory.itemInMainHand
                if (item.isAir()) {
                    sender.sendMessage("§c请手持物品")
                    return@execute
                }
                val name = context["物品名"]
                ItemSaveLib.addItem(name, item)
                sender.sendMessage("§a成功存储物品 $name")
            }
        }
    }

    @CommandBody
    val get = subCommand {
        dynamic("物品名") {
            suggestion<CommandSender>(uncheck = false) { sender, context ->
                ItemSaveLib.items.keys.toList()
            }
            execute<Player> { sender, context, argument ->
                val name = context["物品名"]
                val item = ItemSaveLib.getItem(name)
                if (item == null) {
                    sender.sendMessage("§c未找到物品 $name")
                    return@execute
                }
                sender.inventory.addItem(item)
                sender.sendMessage("§a成功获取物品 $name")
            }
        }
    }

    // si give <物品名> <数量> [玩家名]
    @CommandBody
    val give = subCommand {
        dynamic("物品名") {
            suggestion<CommandSender>(uncheck = false) { sender, context ->
                ItemSaveLib.items.keys.toList()
            }
            dynamic("数量") {
                player("玩家名") {
                    execute<CommandSender> { sender, context, argument ->
                        val name = context["物品名"]
                        val amount = context["数量"].toIntOrNull() ?: 1
                        val player = context["玩家名"]
                        val item = ItemSaveLib.getItem(name)
                        if (item == null) {
                            sender.sendMessage("§c未找到物品 $name")
                            return@execute
                        }
                        val target = Bukkit.getPlayer(context.player("玩家名").uniqueId)
                        if (target == null) {
                            sender.sendMessage("§c未找到玩家 $player")
                            return@execute
                        }
                        target.giveItem(item, amount)
                        sender.sendMessage("§a成功给予玩家 ${target.name} 物品 $name * $amount")
                    }
                }
            }
        }
    }

    //删除
    @CommandBody
    val delete = subCommand {
        dynamic("物品名") {
            suggestion<CommandSender>(uncheck = false) { sender, context ->
                ItemSaveLib.items.keys.toList()
            }
            execute<CommandSender> { sender, context, argument ->
                val name = context["物品名"]
                ItemSaveLib.deleteItem(name)
                sender.sendMessage("§a成功删除物品 $name")
            }
        }
    }

}
