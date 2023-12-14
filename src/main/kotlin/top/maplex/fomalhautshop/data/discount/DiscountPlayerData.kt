package top.maplex.fomalhautshop.data.discount

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object DiscountPlayerData {


    @Config("player.yml")
    lateinit var config: ConfigFile

    fun get(player: String, type: String): Int {
        return config.getInt("${player}.${type}", 0)
    }

    fun set(player: String, type: String, value: Int) {
        config["${player}.${type}"] = value
    }

    fun add(player: String, type: String, value: Int) {
        config["${player}.${type}"] = get(player, type) + value
    }

}
