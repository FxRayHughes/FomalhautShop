package top.maplex.fomalhautshop.data.goods

import de.tr7zw.nbtapi.NBT
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.colored
import taboolib.module.nms.getItemTag
import taboolib.module.nms.getName
import taboolib.platform.util.asLangText
import taboolib.platform.util.asLangTextList
import taboolib.platform.util.giveItem
import taboolib.platform.util.sendLang
import top.maplex.fomalhautshop.data.discount.DiscountManager
import top.maplex.fomalhautshop.item.ShopItemData
import top.maplex.fomalhautshop.item.ShopItemManager
import top.maplex.fomalhautshop.money.MoneyAPI
import top.maplex.fomalhautshop.money.MoneyAPI.replace
import top.maplex.fomalhautshop.ui.eval
import top.maplex.fomalhautshop.utils.editAboData
import top.maplex.fomalhautshop.utils.getAboData
import top.maplex.fomalhautshop.utils.set

@Serializable
data class ShopGoodsBuyData(
    @Comment("是否开启购买(玩家购买商品)")
    var enable: Boolean = false,
    @Comment("商品购买时候的价格")
    var money: Double = 0.0,
    @Comment("商品购买时候的货币类型")
    var moneyType: String = "Vault",
    @Comment("购买物品是否给予物品")
    var give: Boolean = true,
    @Comment("商品购买时候的折扣组")
    var discount: String = "NONE",
    @Comment("商品购买时候的权限组")
    var permission: String = "shop.buy.default",
    @Comment("物品要求 请遵守格式 详见文档")
    var items: MutableList<String> = mutableListOf(),
    @Comment("购买后执行的脚本(kether)")
    var script: MutableList<String> = mutableListOf(),
    @Comment("限购数量 需要 Aboleth 插件")
    var limit: Int = -1,
    @Comment("限购识别关键字")
    var limitId: String = "公共限购"
) {

    fun getBuyLore(player: Player): List<String> {
        val lore = mutableListOf<String>()
        if (items.isNotEmpty()) {
            lore.addAll(player.asLangTextList("shop-ui-buyItem-title"))
            items.forEach {
                lore.add(
                    player.asLangText(
                        "shop-ui-buyItem-info",
                        ShopItemManager.getItem(it).getShowName(player),
                        ShopItemManager.getItem(it).amount
                    )
                )
            }
        }
        if (discount != "Null" && getMoney(player) != money) {
            lore.addAll(
                player.asLangTextList(
                    "shop-ui-buy-discount",
                    money,
                    MoneyAPI.getName(moneyType)
                )
            )
        }
        lore.addAll(
            player.asLangTextList(
                "shop-ui-buy",
                getMoney(player),
                MoneyAPI.getName(moneyType)
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
        if (discount == "Null") {
            return money
        }
        return DiscountManager.get(player, discount, money, moneyType)
    }

    fun checkBuy(player: Player, amount: Int, eval: Boolean = false): Boolean {
        //判断权限
        if (permission != "shop.buy.default") {
            player.sendLang("system-message-buy-not-permission", permission)
            return false
        }
        //判断限购状态
        if (limit != -1) {
            val buy = getAboData(player, "FShop::limit::${limitId}", "0.0").toDouble().toInt()
            if (buy + amount > limit) {
                val can = limit - buy
                if (eval) {
                    player.sendLang("system-message-buy-not-limit", limit, buy, can)
                }
                return false
            }
        }

        //判断钱是否满足 并且判断玩家钱够不够
        if (money > 0.0) {
            val has = MoneyAPI.getMoney(player, moneyType)
            val needMoney = getMoney(player) * amount
            if (needMoney > 0.0 && has < needMoney) {
                val need = needMoney - has
                if (eval) {
                    player.sendLang("system-message-buy-not-money", need, MoneyAPI.getName(moneyType))
                }
                return false
            }
        }

        //判断物品是否满足
        if (items.isNotEmpty()) {
            if (!ShopItemManager.check(player, items)) {
                val names = items.map {
                    val data = ShopItemManager.getData(it)
                    val sup = it.split(" ")
                    val color = if (data.isMeet(player, sup[1], sup[3].toIntOrNull() ?: 1)) "§a" else "§c"
                    color + data.getItem(player, sup[1])
                        ?.getName(player) + "" + "X ${sup[3].toIntOrNull() ?: 1}"
                }
                if (eval) {
                    player.sendLang("system-message-buy-not-item", names.joinToString("\n"))
                }
                return false
            }
        }
        return true
    }

    fun evalBuy(player: Player, amount: Int, goodsItem: ShopItemData, shopGoodsBaseData: ShopGoodsBaseData): Boolean {
        if (!checkBuy(player, amount, true)) {
            return false
        }

        if (limit != -1) {
            editAboData(player, "FShop::limit::${limitId}", "+", amount)
        }

        if (money > 0.0) {
            val needMoney = getMoney(player) * amount
            MoneyAPI.takeMoney(player, needMoney, moneyType)
        }

        if (items.isNotEmpty()) {
            ShopItemManager.take(player, items)
        }

        if (give) {
            player.giveItem(goodsItem.getItem(player), amount)
        }
        script.apply {
            replace("{action.amount}", amount.toString())
            replace("{data.goods}", shopGoodsBaseData.name)
            replace("{data.money}", (getMoney(player) * amount).toString())
        }.eval(player)


        return true
    }

    fun sendBuyMessage(player: Player, amount: Int, goods: String) {
        if (getMoney(player) > 0.0 && items.isEmpty()) {
            player.sendLang(
                "system-message-buy-success-money",
                goods,
                amount,
                getMoney(player) * amount,
                MoneyAPI.getName(moneyType)
            )
            return
        }
        val itemTable = items.map {
            val sup = it.split(" ")
            "${ShopItemManager.getItem(it).getShowName(player)} X ${sup[3]}"
        }
        if (money <= 0.0 && items.isNotEmpty()) {
            player.sendLang(
                "system-message-buy-success-item", goods,
                amount, itemTable.joinToString("\n")
            )
            return
        }
        player.sendLang(
            "system-message-buy-success-money-item",
            goods,
            amount,
            getMoney(player) * amount,
            MoneyAPI.getName(moneyType),
            itemTable.joinToString("\n")
        )
    }

    fun setNBT(player: Player, itemStack: ItemStack, goodsItem: ShopItemData) {
        itemStack.set("shop.buy.enable", enable)
        itemStack.set("shop.buy.money", money)
        itemStack.set("shop.buy.moneyGet", getMoney(player))
        itemStack.set("shop.buy.moneyType", moneyType)
        itemStack.set("shop.buy.moneyTypeShow", MoneyAPI.getName(moneyType))
        itemStack.set("shop.buy.discount", discount)
        itemStack.set("shop.buy.permission", permission)
        itemStack.set("shop.buy.items", items.joinToString(",") { ShopItemManager.getItem(it).getShowString(player) })
        itemStack.set("shop.buy.script", script.joinToString(","))
        itemStack.set("shop.buy.limit", limit)
        itemStack.set("shop.buy.limitId", limitId)
        itemStack.set(
            "shop.buy.limitUse",
            getAboData(player, "FShop::limit::${limitId}", "0.0").toDouble().toInt()
        )
    }

}
