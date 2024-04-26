package top.maplex.fomalhautshop.data.goods

import de.tr7zw.nbtapi.NBT
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.nms.ItemTag
import taboolib.platform.util.asLangText
import taboolib.platform.util.asLangTextList
import taboolib.platform.util.giveItem
import taboolib.platform.util.sendLang
import top.maplex.fomalhautshop.FomalhautShop
import top.maplex.fomalhautshop.data.discount.DiscountManager
import top.maplex.fomalhautshop.item.ShopItemData
import top.maplex.fomalhautshop.item.ShopItemManager
import top.maplex.fomalhautshop.money.MoneyAPI
import top.maplex.fomalhautshop.ui.eval
import top.maplex.fomalhautshop.utils.*

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
    var discount: String = "NoUse",
    @Comment("商品购买时候的权限组")
    var permission: String = "shop.buy.default",
    @Comment("物品要求 请遵守格式 详见文档")
    var items: MutableList<String> = mutableListOf(),
    @Comment("购买后执行的脚本(kether)")
    var script: MutableList<String> = mutableListOf(),
    @Comment("限购数量 需要 Aboleth 插件")
    var limit: Int = -1,
    @Comment("限购识别关键字")
    var limitId: String = "公共限购",
    @Comment("商品占用槽位数量 实际槽位为 购买数量X(占用数量)X(如果是give模式 物品源物品的数量)")
    var needSlotAmount: Int = 1,
    @Comment("购买限制 (Kether)")
    var check: MutableList<String> = mutableListOf(),
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
        if (discount != "NoUse" && getMoney(player) != money) {
            lore.addAll(
                player.asLangTextList(
                    "shop-ui-buy-discount",
                    money,
                    MoneyAPI.getName(moneyType)
                )
            )
        }
        if (money > 0.0) {
            lore.addAll(
                player.asLangTextList(
                    "shop-ui-buy-money",
                    getMoney(player),
                    MoneyAPI.getName(moneyType)
                )
            )
        }
        lore.addAll(
            player.asLangTextList(
                "shop-ui-buy-action",
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
        if (discount == "NoUse") {
            return money
        }
        return DiscountManager.get(player, discount, money, moneyType)
    }

    fun checkBuy(player: Player, amount: Int, goodsItem: ShopItemData, eval: Boolean = false): Boolean {
        //判断权限
        if (permission != "shop.buy.default") {
            player.sendLang("system-message-buy-not-permission", permission)
            return false
        }

        if (check.check(player).get() == false) {
            return false
        }

        if (amount > FomalhautShop.config.getInt("MaxAmount", 64)) {
            player.sendLang("system-message-buy-not-maxAmount", FomalhautShop.config.getInt("MaxAmount", 64))
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
            if (needMoney >= 100000000.0) {
                player.sendLang("system-message-buy-not-money", needMoney, MoneyAPI.getName(moneyType))
                return false
            }
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
            val itemData = items.map {
                ShopItemData(it).apply {
                    this.amount = this.amount * amount
                }
            }
            var can = true
            val names = StringBuilder()
            itemData.forEach {
                if (!it.isMeet(player)) {
                    can = false
                    names.append("&c")
                        .append(it.getShowName(player))
                        .append("&f X ")
                        .append(it.amount)
                        .append("\n")
                }
            }
            if (!can) {
                if (eval) {
                    player.sendLang("system-message-buy-not-item", names.toString().colored())
                }
                return false
            }
        }


        val itemAmount = if (give){
            goodsItem.amount * amount * needSlotAmount
        }else{
            amount * needSlotAmount
        }
        val canAmount = player.tryGiveItem(goodsItem.getItem(player), itemAmount)
        if (!canAmount ) {
            if (eval){
                player.sendLang("system-message-buy-not-inventory", itemAmount)
            }
            return false
        }
        return true
    }

    fun evalBuy(player: Player, amount: Int, goodsItem: ShopItemData, shopGoodsBaseData: ShopGoodsBaseData): Boolean {
        if (amount <= 0) {
            return false
        }
        if (!checkBuy(player, amount, goodsItem, true)) {
            return false
        }

        if (limit != -1) {
            editAboData(player, "FShop::limit::${limitId}", "+", amount)
        }

        if (money > 0.0) {
            val needMoney = getMoney(player) * amount.toDouble()
            MoneyAPI.takeMoney(player, needMoney, moneyType)
        }

        if (items.isNotEmpty()) {
            items.map {
                ShopItemData(it).apply {
                    this.amount = this.amount * amount
                }
            }.forEach {
                it.take(player)
            }
        }

        if (give) {
            submit {
                val itemAmount = goodsItem.amount * amount
                player.giveItem(goodsItem.getItem(player), itemAmount)
            }
        }
        script.map {
            it.replace("<action_amount>", amount.toString())
                .replace("{data.goods}", shopGoodsBaseData.name)
                .replace("{data.money}", (getMoney(player) * amount).toString())
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
            "${ShopItemManager.getItem(it).getShowName(player)} X ${(sup[3].toInt() * amount)}"
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

    fun setNBT(player: Player, itemStack: ItemStack, goodsItem: ItemTag) {
        goodsItem.reader {
            set("shop.buy.enable", enable)
            set("shop.buy.money", money)
            set("shop.buy.moneyGet", getMoney(player))
            set("shop.buy.moneyType", moneyType)
            set("shop.buy.moneyTypeShow", MoneyAPI.getName(moneyType))
            set("shop.buy.moneyTypePapi", MoneyAPI.moneyConfig.getString("${moneyType}.get", "none")!!)
            set("shop.buy.moneyHas", MoneyAPI.getMoney(player, moneyType))
            set("shop.buy.discount", discount)
            set("shop.buy.permission", permission)
            set("shop.buy.items", items.joinToString(",") { ShopItemManager.getItem(it).getShowString(player) })
            set("shop.buy.script", script.joinToString(","))
            set("shop.buy.limit", limit)
            set("shop.buy.limitId", limitId)
            set("shop.buy.limitUse", getAboData(player, "FShop::limit::${limitId}", "0.0").toDouble().toInt())
            if (items.isNotEmpty()) {
                val builder = StringBuilder()
                items.forEach {
                    NBT.itemStackToNBT(ShopItemManager.getItem(it).getItem(player)).toString().let { z ->
                        builder.append(z)
                        builder.append("[FShop]")
                    }
                }
                set("shop.buy.itemsJson", builder.toString())
            } else {
                set("shop.buy.itemsJson", "none")
            }
        }
    }

}
