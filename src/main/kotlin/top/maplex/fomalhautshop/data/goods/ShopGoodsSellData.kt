package top.maplex.fomalhautshop.data.goods

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.colored
import taboolib.platform.util.asLangTextList
import taboolib.platform.util.sendLang
import top.maplex.fomalhautshop.item.ShopItemData
import top.maplex.fomalhautshop.money.MoneyAPI
import top.maplex.fomalhautshop.utils.editAboData
import top.maplex.fomalhautshop.utils.getAboData
import top.maplex.fomalhautshop.utils.set

@Serializable
data class ShopGoodsSellData(
    @Comment("是否开启回收(玩家出售商品)")
    var enable: Boolean = false,
    @Comment("商品回收时候的价格")
    var money: Double = 0.0,
    @Comment("商品回收时候的货币类型")
    var moneyType: String = "Vault",
    @Comment("商品回收时候的权限组")
    var permission: String = "shop.sell.default",
    @Comment("回收后执行的脚本(kether)")
    var script: MutableList<String> = mutableListOf(),
    @Comment("限回收数量 需要 Aboleth 插件")
    var limit: Int = -1,
    @Comment("限回收识别关键字")
    var limitId: String = "公共限购"
) {

    fun getSellLore(player: Player, itemData: ShopItemData): List<String> {
        val lore = mutableListOf<String>()
        lore.addAll(
            player.asLangTextList(
                "shop-ui-sell",
                getMoney(player),
                MoneyAPI.getName(moneyType),
                itemData.getNumber(player),
            )
        )
        if (limit != -1) {
            val buy = getAboData(player, "FShop::limit::${limitId}", "0.0").toDouble().toInt()
            val can = limit - buy
            lore.addAll(
                player.asLangTextList(
                    "shop-ui-buy-limit",
                    limit,
                    buy,
                    can
                )
            )
        }
        return lore.colored()
    }

    fun getMoney(player: Player): Double {
        return money
    }

    fun hasNumber(player: Player, itemData: ShopItemData): Int {
        return itemData.getNumber(player)
    }

    fun checkSell(player: Player, amount: Int, itemData: ShopItemData): Boolean {
        //判断权限
        if (permission != "shop.sell.default") {
            player.sendLang("system-message-sell-not-permission", permission)
            return false
        }
        //判断限购状态
        if (limit != -1) {
            val buy = getAboData(player, "FShop::limit::${limitId}", "0.0").toDouble().toInt()
            if (buy >= limit) {
                val can = limit - buy
                player.sendLang("system-message-sell-not-limit", limit, buy, can)
                return false
            }
        }

        //判断物品数量
        hasNumber(player, itemData).let {
            if (it < amount * itemData.amount) {
                player.sendLang(
                    "system-message-sell-not-number",
                    itemData.getShowName(player),
                    amount * itemData.amount,
                    it
                )
                return false
            }
        }

        return true
    }

    fun evalSell(player: Player, amount: Int, itemData: ShopItemData, shopGoodsBaseData: ShopGoodsBaseData): Boolean {
        if (!checkSell(player, amount, itemData)) {
            return false
        }

        if (limit != -1) {
            editAboData(player, "FShop::limit::${limitId}", "+", amount)
        }

        (1..amount).forEach {
            itemData.take(player)
        }

        if (money > 0.0) {
            val needMoney = getMoney(player) * amount
            MoneyAPI.addMoney(player, needMoney, moneyType)
        }
        return true
    }

    fun sendSellMessage(player: Player, amount: Int, goods: String) {
        //成功出售 {0} X{1} 获得了 {2}{3}
        player.sendLang(
            "system-message-sell-success",
            goods,
            amount,
            getMoney(player) * amount,
            MoneyAPI.getName(moneyType),
        )
    }

    fun setNBT(player: Player, itemStack: ItemStack, goodsItem: ShopItemData) {
        itemStack.set("shop.sell.enable", enable)
        itemStack.set("shop.sell.money", money)
        itemStack.set("shop.sell.moneyGet", getMoney(player))
        itemStack.set("shop.sell.moneyType", moneyType)
        itemStack.set("shop.sell.moneyTypeShow", MoneyAPI.getName(moneyType))
        itemStack.set("shop.sell.permission", permission)
        itemStack.set("shop.sell.script", script.joinToString(","))
        itemStack.set("shop.sell.limit", limit)
        itemStack.set("shop.sell.limitId", limitId)
        itemStack.set(
            "shop.sell.limitUse",
            getAboData(player, "FShop::limit::${limitId}", "0.0").toDouble().toInt()
        )

    }

}
