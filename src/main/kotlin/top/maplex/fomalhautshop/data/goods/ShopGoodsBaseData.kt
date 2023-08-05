package top.maplex.fomalhautshop.data.goods


import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.asLangTextList
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.item.ShopItemManager
import top.maplex.fomalhautshop.utils.set
import java.io.File
import java.nio.charset.StandardCharsets

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
    @Comment("在可购买/可出售时是否显示附魔效果")
    var shiny: Boolean = true,
    @Comment("这代表商品的描述 会在购买页面单独展示")
    var info: MutableList<String> = mutableListOf(),
    @Comment("物品购买策略")
    var buy: ShopGoodsBuyData? = null,
    @Comment("物品出售策略")
    var sell: ShopGoodsSellData? = null,
    @Comment("这是用来序列化保存文件时文件位置，一般来说不需要修改,读取时会设置")
    var path: String = ""
) {

    val goodsItem by lazy { ShopItemManager.getItem(goods) }

    fun showItem(player: Player, editor: Boolean = false): ItemStack {
        val item = ShopItemManager.getItem(goods).getItemAmount(player).clone()
        val itemTag = item.getItemTag()
        itemTag.putDeep("shop.id", id)
        itemTag.putDeep("shop.group", group.joinToString(","))
        itemTag.putDeep("shop.name", name)
        itemTag.putDeep("shop.weight", weight)
        itemTag.putDeep("shop.info", info.joinToString(","))
        itemTag.putDeep("shop.goods.data", goods)
        itemTag.putDeep("shop.goods.show", ShopItemManager.getItem(goods).getShowString(player))
        val sellNbt = sell?.setNBT(player, item, itemTag)
        if (sellNbt == null) {
            itemTag.putDeep("shop.sell.enable", false)
        }
        val buyNbt = buy?.setNBT(player, item, itemTag)
        if (buyNbt == null) {
            itemTag.putDeep("shop.buy.enable", false)
        }
        itemTag.saveTo(item)
        item.setItemTag(itemTag)
        return item.apply {
            item.modifyMeta<ItemMeta> {
                setDisplayName(getShowName(player))
            }
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

                addAll(player.asLangTextList("shop-ui-goods-info"))

                if (editor) {
                    addAll(player.asLangTextList("shop-ui-edit-normal", id))
                }
            }
        }
    }

    fun getShowName(player: Player): String {
        return if (name.isEmpty()) {
            goodsItem.getShowName(player)
        } else {
            name.colored()
        }
    }

    fun buy(player: Player, amount: Int) {
        if (buy != null && buy!!.enable && buy!!.checkBuy(player, amount, true)) {
            buy!!.evalBuy(player, amount, goodsItem, this)
            buy!!.sendBuyMessage(player, amount, getShowName(player))
        }
    }

    fun sell(player: Player, amount: Int) {
        if (sell != null && sell!!.enable && sell!!.checkSell(player, amount, goodsItem, true)) {
            sell!!.evalSell(player, amount, goodsItem, this)
            sell!!.sendSellMessage(player, amount, getShowName(player))
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

    fun delete() {
        submit {
            ShopManager.goods.remove(this@ShopGoodsBaseData)

            newFile(getDataFolder(), path, create = true).let {
                if (it.exists()) {
                    //YYYY-MM-DD HH:mm:ss
                    val time: String = it.lastModified().let { time ->
                        val date = java.util.Date(time)
                        val sdf = java.text.SimpleDateFormat("yyyy年MM月dd日 H-mm-ss")
                        sdf.format(date)
                    }
                    File(getDataFolder(), "shops/noLoad/delete").let { f ->
                        if (!f.exists()) {
                            f.mkdirs()
                        }
                        File(f, "${time}-${id}.yml").apply {
                            if (!exists()) {
                                createNewFile()
                            }
                            writeText(
                                it.readText(StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8
                            )
                        }
                    }
                    it.delete()
                }
            }
        }
    }

}
