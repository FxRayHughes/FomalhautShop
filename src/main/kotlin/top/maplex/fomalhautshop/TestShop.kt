package top.maplex.fomalhautshop

import net.mamoe.yamlkt.Yaml
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.io.runningClasses
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.data.goods.ShopGoodsBuyData
import top.maplex.fomalhautshop.data.goods.ShopGoodsSellData
import java.nio.charset.StandardCharsets

object TestShop {

    fun test() {
        runningClasses
        newFile(getDataFolder(), "testB.yml", create = true).writeText(
            Yaml.encodeToString(
                ShopGoodsBaseData.serializer(),
                ShopGoodsBaseData(
                    "testB",
                    mutableListOf("测试商店", "海外代购"),
                    "测试商品",
                    "[MC] Apple => 10",
                    50,
                    mutableListOf("测试商品描述", "第二行")
                ).apply {
                    buy = ShopGoodsBuyData(
                        true,
                        100.0,
                        "Vault",
                        true,
                        "常规折扣",
                        "shop.buy.default",
                        items = mutableListOf("[MM] BanditTunic => 5"),
                        limit = 50
                    )
                    sell = ShopGoodsSellData(
                        enable = true,
                        100.0, "星球比", limit = 50
                    )
                    ShopManager.goods.add(this)
                }
            ),
            StandardCharsets.UTF_8
        )
    }

}
