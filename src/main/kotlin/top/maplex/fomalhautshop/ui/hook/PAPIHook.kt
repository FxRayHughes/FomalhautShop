package top.maplex.fomalhautshop.ui.hook

import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.utils.getString

object PAPIHook : PlaceholderExpansion {

    override val identifier: String = "fomalhautshop"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        player ?: return "player_null"
        val string = args.split("_")
        val goods = string.getOrNull(0) ?: return "goods_null"
        val goodsData = ShopManager.goods.firstOrNull { it.id == goods } ?: return "goods_null_2"
        val infos = string.getOrNull(1) ?: return ""
        goodsData.showItem(player).getString(infos).let {
            return if (it == "null") "" else it
        }
    }
}
