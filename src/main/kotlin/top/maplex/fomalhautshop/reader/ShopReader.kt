package top.maplex.fomalhautshop.reader

import net.mamoe.yamlkt.Yaml
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.data.goods.ShopGoodsBaseData
import top.maplex.fomalhautshop.ui.UIReader
import java.io.File
import java.nio.charset.StandardCharsets

object ShopReader {

    fun loadData(file: File){
        file.readText(StandardCharsets.UTF_8).let { text ->
            ShopManager.goods.add(
                Yaml.decodeFromString(ShopGoodsBaseData.serializer(), text).apply {
                    this.path = file.path.replace("plugins\\FomalhautShop\\", "")
                }
            )
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun save() {
        ShopManager.goods.forEach {
            if (it.path != "" && it.path.endsWith(".yml")) {
                newFile(getDataFolder(), it.path, create = true).writeText(
                    Yaml.encodeToString(ShopGoodsBaseData.serializer(), it),
                    StandardCharsets.UTF_8
                )
            }
        }
    }

    fun saveOne(data: ShopGoodsBaseData) {
        if (data.path != "" && data.path.endsWith(".yml")) {
            newFile(getDataFolder(), data.path, create = true).writeText(
                Yaml.encodeToString(ShopGoodsBaseData.serializer(), data),
                StandardCharsets.UTF_8
            )
        }
    }

}
