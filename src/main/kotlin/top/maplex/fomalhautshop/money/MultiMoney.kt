package top.maplex.fomalhautshop.money

import org.bukkit.Bukkit
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.compat.replacePlaceholder
import top.maplex.fomalhautshop.money.MoneyAPI.replace
import top.maplex.multivault.MoneyType
import top.maplex.multivault.MultiVault
import java.math.BigDecimal
import java.util.*

object MultiMoney {

    @Config("money.yml")
    lateinit var moneyConfig: ConfigFile

    val array = mutableListOf<String>()

    fun hook() {
        moneyConfig.getKeys(false).forEach {
            if (it == "Vault") {
                return@forEach
            }
            val type = it
            MultiVault.registerMoneyType(object : MoneyType {
                override fun id(): String {
                    return it
                }

                override fun scale(): Int {
                    if (moneyConfig.getString("$it.type").equals("int", true)) {
                        return 0
                    }
                    return 2
                }

                override fun getName(): String {
                    return moneyConfig.getString("$it.name")!!
                }

                override fun getSymbol(): String {
                    return moneyConfig.getString("$it.symbol", "点")!!
                }

                override fun getBalance(p0: UUID): BigDecimal {
                    val player = Bukkit.getPlayer(p0) ?: return BigDecimal(0)
                    return BigDecimal(
                        MoneyAPI.moneyConfig.getString("${type}.get")
                            ?.replace("<player>", player.name)
                            ?.replacePlaceholder(player)
                            ?.toDoubleOrNull() ?: 0.0
                    )
                }

                override fun addBalance(p0: UUID?, amount: BigDecimal) {
                    val player = Bukkit.getPlayer(p0!!) ?: return
                    if (scale() <= 0) {
                        amount.setScale(0)
                    }
                    MoneyAPI.moneyConfig.getStringList("${type}.add")
                        .replace("<value>", amount.toString())
                        .replace("<player>", player.name).forEach {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                        }
                }

                override fun takeBalance(p0: UUID?, p1: BigDecimal?): Boolean {
                    val player = Bukkit.getPlayer(p0!!) ?: return false
                    if (scale() <= 0) {
                        p1!!.setScale(0)
                    }
                    return if (getBalance(p0) < p1) {
                        false
                    } else {
                        MoneyAPI.moneyConfig.getStringList("${type}.take")
                            .replace("<value>", p1.toString())
                            .replace("<player>", player.name).forEach {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                            }
                        true
                    }
                }

                override fun forceTakeBalance(p0: UUID?, p1: BigDecimal?): Boolean {
                    val player = Bukkit.getPlayer(p0!!) ?: return false
                    if (scale() <= 0) {
                        p1!!.setScale(0)
                    }
                    MoneyAPI.moneyConfig.getStringList("${type}.take")
                        .replace("<value>", p1.toString())
                        .replace("<player>", player.name).forEach {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                        }
                    return true
                }

                override fun setBalance(p0: UUID?, p1: BigDecimal?) {
                    val player = Bukkit.getPlayer(p0!!) ?: return
                    if (scale() <= 0) {
                        p1!!.setScale(0)
                    }
                    MoneyAPI.moneyConfig.getStringList("${type}.set")
                        .replace("<value>", p1.toString())
                        .replace("<player>", player.name).forEach {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replacePlaceholder(player))
                        }
                }

            }.apply {
                array.add(this.id())
            })
        }
    }

}
