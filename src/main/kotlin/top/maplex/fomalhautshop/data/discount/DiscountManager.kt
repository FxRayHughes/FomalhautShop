package top.maplex.fomalhautshop.data.discount

import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.util.getMap

object DiscountManager {

    val datas = ArrayList<DiscountData>()

    @Config(value = "discount.yml")
    lateinit var config: ConfigFile
        private set

    @Awake(LifeCycle.ENABLE)
    fun load() {
        datas.clear()
        config.getKeys(false).forEach {
            datas.add(
                DiscountData(
                    it,
                    config.getString("${it}.permissions") ?: return@forEach,
                    config.getStringList("${it}.shop"),
                    config.getMap("${it}.data")
                )
            )
        }
    }

    fun get(player: Player, grouper: String, price: Double, moneyType: String): Double {
        return datas.filter {
            player.hasPermission(it.permission) && (it.shop.isEmpty() || it.shop.contains("all") || it.shop.contains(
                grouper
            ))
        }.minByOrNull {
            it.data.getOrDefault(moneyType, 1.0)
        }?.let {
            it.data.getOrDefault(moneyType, 1.0) * price
        } ?: price
    }

}
