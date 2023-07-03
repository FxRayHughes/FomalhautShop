package top.maplex.fomalhautshop.data.goods

import de.tr7zw.nbtapi.NBT
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.*
import top.maplex.fomalhautshop.item.ShopItemManager
import top.maplex.fomalhautshop.utils.set

@Serializable
data class ShopGoodsBaseData(
    @Comment("这代表商品的索引ID")
    var id: String,
    @Comment("属于那个商店的商品")
    var group: MutableList<String> = mutableListOf(),
    @Comment("这代表商品的显示名称")
    var name: String,
    @Comment("商品本体 遵守物品书写规范")
    var goods: String,
    @Comment("显示优先级 数字越大排行越前")
    var weight: Int = 0,
    @Comment("这代表商品的描述 会在购买页面单独展示")
    var info: MutableList<String> = mutableListOf(),
    @Comment("物品购买策略")
    var buy: ShopGoodsBuyData? = null,
    @Comment("物品出售策略")
    var sell: ShopGoodsSellData? = null,
    @Comment("这是用来序列化保存文件时文件位置，一般来说不需要修改,读取时会设置")
    var path: String = ""
) {

    private val goodsItem by lazy { ShopItemManager.getItem(goods) }

    fun showItem(player: Player, editor: Boolean = false): ItemStack {
        val item = ShopItemManager.getItem(goods).getItem(player)
        item.set("shop.id", id)
        item.set("shop.group", group.joinToString(","))
        item.set("shop.name", name)
        item.set("shop.weight", weight)
        item.set("shop.info", info.joinToString(","))
        item.set("shop.goods.data", goods)
        item.set("shop.goods.show", ShopItemManager.getItem(goods).getShowString(player))
        val sellNbt = sell?.setNBT(player, item, goodsItem)
        if (sellNbt == null) {
            item.set("shop.sell.enable", false)
        }
        val buyNbt = buy?.setNBT(player, item, goodsItem)
        if (buyNbt == null) {
            item.set("shop.buy.enable", false)
        }
        return item.apply {
            //增加Info
            modifyLore {
                addAll(info.map { "&f${it}".replacePlaceholder(player) })
                add(" ")
                if (buy != null && buy!!.enable) {
                    addAll(buy!!.getBuyLore(player))
                }
                add(" ")
                if (sell != null && sell!!.enable) {
                    addAll(sell!!.getSellLore(player, goodsItem))
                }
                if (editor) {
                    addAll(player.asLangTextList("shop-ui-edit-normal", id))
                }
            }
        }
    }

    fun buy(player: Player, amount: Int) {
        if (buy != null && buy!!.checkBuy(player, amount)) {
            buy!!.evalBuy(player, amount, goodsItem, this)
            buy!!.sendBuyMessage(player, amount, name)
        }
    }

    fun sell(player: Player, amount: Int) {
        if (sell != null && sell!!.checkSell(player, amount, goodsItem)) {
            sell!!.evalSell(player, amount, goodsItem, this)
            sell!!.sendSellMessage(player, amount, name)
        }
    }

    fun checkShow(player: Player): Boolean {

        if (buy != null && !buy!!.checkBuy(player, 1)) {
            return false
        }

        if (sell != null && !sell!!.checkSell(player, 1, goodsItem)) {
            return false
        }

        return true
    }

}
