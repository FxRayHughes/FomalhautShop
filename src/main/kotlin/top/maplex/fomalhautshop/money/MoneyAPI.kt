package top.maplex.fomalhautshop.money

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.util.getStringColored
import taboolib.platform.compat.replacePlaceholder

object MoneyAPI {

    @Config("money.yml")
    lateinit var moneyConfig: ConfigFile

    private val economy by lazy {
        Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
    }

    fun takeMoney(player: Player, amount: Double, type: String): Boolean {
        return if (getMoney(player, type) < amount) {
            false
        } else {
            if (type == "Vault") {
                val eco = economy ?: return false
                eco.withdrawPlayer(player, amount)
            } else {
                when (moneyConfig.getString("${type}.type")) {
                    "Int", "int", "INT" -> {
                        moneyConfig.getStringList("${type}.take")
                            .replace("<value>", amount.toInt().toString())
                            .replace("<player>", player.name).forEach {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                            }
                    }

                    else -> {
                        moneyConfig.getStringList("${type}.take")
                            .replace("<value>", amount.toString())
                            .replace("<player>", player.name).forEach {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                            }
                    }
                }
            }
            true
        }
    }

    fun addMoney(player: Player, amount: Double, type: String) {
        if (type == "Vault") {
            val eco = economy ?: return
            eco.depositPlayer(player, amount)
        } else {
            when (moneyConfig.getString("${type}.type")) {
                "Int", "int", "INT" -> {
                    moneyConfig.getStringList("${type}.add")
                        .replace("<value>", amount.toInt().toString())
                        .replace("<player>", player.name).forEach {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                        }
                }

                else -> {
                    moneyConfig.getStringList("${type}.add")
                        .replace("<value>", amount.toString())
                        .replace("<player>", player.name).forEach {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                        }
                }
            }
        }
    }

    fun getMoney(player: Player, type: String): Double {
        return if (type == "Vault") {
            val eco = economy ?: return 0.0
            eco.getBalance(player)
        } else {
            moneyConfig.getString("${type}.get")?.replace("<player>", player.name)
                ?.replacePlaceholder(player)
                ?.toDoubleOrNull() ?: 0.0
        }
    }

    fun getName(type: String): String {
        return moneyConfig.getStringColored("${type}.name")?.let {
            "${it}&f".colored()
        } ?: type
    }

    fun Collection<String>.replace(old: String, new: String): Collection<String> {
        return this.map { it.replace(old, new) }
    }
}
