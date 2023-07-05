package top.maplex.fomalhautshop.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import top.maplex.abolethcore.AbolethUtils
import top.maplex.fomalhautshop.FomalhautShop
import top.maplex.fomalhautshop.data.discount.DiscountPlayerData

fun String.asChar(): Char {
    return this.toCharArray()[0]
}


fun List<MutableList<String>>.flattenList(): List<String> {
    val set = HashSet<String>()
    val result = mutableListOf<String>()
    for (sublist in this) {
        for (str in sublist) {
            if (!set.contains(str)) {
                set.add(str)
                result.add(str)
            }
        }
    }
    return result
}

fun getAboData(player: Player, key: String, default: String): String {

    if (FomalhautShop.config.getString("Discount") == "local") {
        return DiscountPlayerData.get(player.uniqueId.toString(), key).toString()
    } else {
        if (Bukkit.getPluginManager().getPlugin("AbolethCore") == null) {
            return default
        }
        return AbolethUtils.get(player.uniqueId, key, default)
    }
}

fun editAboData(player: Player, key: String, action: String, value: Any) {

    if (FomalhautShop.config.getString("Discount") == "local") {
        DiscountPlayerData.add(player.uniqueId.toString(), key, value.toString().toInt())
    }else{
        if (Bukkit.getPluginManager().getPlugin("AbolethCore") == null) {
            return
        }
        AbolethUtils.edit(player.uniqueId, key, action, value)
    }
}
