package top.maplex.fomalhautshop.reader

import org.bukkit.Material
import ray.mintcat.shop.Shop
import ray.mintcat.shop.data.ShopCommodityData
import ray.mintcat.shop.data.materials.ShopMaterialData
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.data.goods.ShopGoodsBuyData
import top.maplex.fomalhautshop.data.goods.ShopGoodsSellData
import java.util.*

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

        val goods = "[${newId(this.item.form, this.item.id)}] ${this.item.id} => ${this.item.amount}"

        return ShopGoodsBaseData(
            this.id ?: UUID.randomUUID().toString(),
            mutableListOf(), this.showName,
            goods, 0, true, this.info.toMutableList(),
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

    fun ShopMaterialData.newItem(): String {
        return "[${newId(this.form, this.id)}] ${this.id} => ${this.amount}"
    }

    fun newId(string: String, value: String): String {
        return when (string) {
            "Minecraft" -> {
                if (Material.getMaterial(value) != null) {
                    "MC"
                } else {
                    "B64"
                }
            }

            "ItemsAdder" -> "IA"
            "ItemSystem" -> "IS"
            "MMOItems" -> "MI"
            "MythicMobs", "Mythic" -> "MM"
            "SXItem" -> "SI"
            else -> string
        }
    }

}
