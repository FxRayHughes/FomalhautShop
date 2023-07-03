package top.maplex.fomalhautshop.reader

import ray.mintcat.shop.Shop
import ray.mintcat.shop.data.ShopCommodityData
import ray.mintcat.shop.data.materials.ShopMaterialData
import taboolib.common.platform.function.getDataFolder
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.data.goods.ShopGoodsBuyData
import top.maplex.fomalhautshop.data.goods.ShopGoodsSellData
import top.maplex.fomalhautshop.reader.ShopOldReader.toNew
import java.io.File
import java.util.UUID

object ShopOldReader {

    fun eval() {
        Shop.getListData().forEach {
            it.commodity.forEach { z ->
                ShopManager.goods.add(z.toNew().apply {
                    this.path = "/shops/old/${this.id}.yml"
                    this.group.add(it.name)
                })
            }
        }
        ShopReader.save()
    }

    private fun ShopCommodityData.toNew(): ShopGoodsBaseData {

        val goods = "[${newId(this.item.form)}] ${this.item.id} => ${this.item.amount}"

        return ShopGoodsBaseData(
            this.id ?: UUID.randomUUID().toString(),
            mutableListOf(), this.showName,
            goods, 0, this.info.toMutableList(),
            ShopGoodsBuyData(
                true, this.price, runCatching { this.moneyType }.getOrNull() ?: "Vault",
                items = this.buyItems?.map { it.newItem() }?.toMutableList() ?: mutableListOf(),
                script = this.actionBuy.toMutableList(),
                give = this.give
            ),
            ShopGoodsSellData(
                true, this.buy, runCatching { this.moneyType }.getOrNull() ?: "Vault",
                script = this.actionSell.toMutableList(),
            )
        )
    }

    fun getMoneyType(string: String): String {
        return when (string) {
            "MONEY" -> "MONEY"
            "POINT" -> "POINT"
            "ITEM" -> "ITEM"
            else -> "MONEY"
        }
    }

    fun ShopMaterialData.newItem(): String {
        return "[${newId(this.form)}] ${this.id} => ${this.amount}"
    }

    fun newId(string: String): String {
        return when (string) {
            "Minecraft" -> "MC"
            "ItemsAdder" -> "IA"
            "ItemSystem" -> "IS"
            "MMOItems" -> "MI"
            "MythicMobs" -> "MM"
            "SXItem" -> "SI"
            else -> string
        }
    }

}
