package top.maplex.fomalhautshop.data.discount

import taboolib.module.configuration.createLocal

object DiscountPlayerData {

    val config by lazy {
        createLocal("player.yml")
    }

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
