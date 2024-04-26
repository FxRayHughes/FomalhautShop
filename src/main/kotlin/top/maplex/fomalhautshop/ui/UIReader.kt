package top.maplex.fomalhautshop.ui

import taboolib.common.LifeCycle
import taboolib.common.io.newFolder
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.resettableLazy
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.maplex.fomalhautshop.data.ShopManager
import top.maplex.fomalhautshop.reader.ShopReader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object UIReader {

    val uiConfig = ConcurrentHashMap<String, Configuration>()

    val scriptConfig = ConcurrentHashMap<String, List<String>>()

    val files = ArrayList<File>()

    @Config("ui/default.yml")
    lateinit var default: ConfigFile

    @Config("script/default.yml")
    lateinit var defaultscript: ConfigFile

    fun getUIConfig(data: String): Configuration {
        return uiConfig.getOrDefault(data, default)
    }

    @Awake(LifeCycle.ACTIVE)
    fun load() {
        default.reload()
        defaultscript.reload()
        files.clear()
        scriptConfig.clear()
        uiConfig.clear()
        ShopManager.goods.clear()

        files.clear()
        releaseResourceFile("ui/sell_ui.yml")
        loadFile(File(getDataFolder(), "ui/"))
        loadConfig()

        files.clear()
        loadFile(File(getDataFolder(), "script/"))
        loadScript()

        files.clear()
        loadFile(File(getDataFolder(), "shops/"))
        newFolder(getDataFolder(), "shops/noLoad", create = true)
        files.forEach {
            if (it.path.contains("noLoad")) {
                return@forEach
            }
            ShopReader.loadData(it)
        }
    }

    fun loadConfig() {
        files.forEach {
            Configuration.loadFromFile(it, type = Type.YAML).let { cf ->
                cf.getString("Shop")?.let { name ->
                    uiConfig[name] = cf
                }
            }
        }
    }

    fun loadScript() {
        files.forEach {
            Configuration.loadFromFile(it, type = Type.YAML).let { cf ->
                cf.getKeys(false).forEach z@{ key ->
                    scriptConfig[key] = cf.getStringList(key)
                }
            }
        }
    }

    fun loadFile(file: File) {
        if (file.isFile) {
            files.add(file)
        } else {
            file.listFiles()?.forEach {
                loadFile(it)
            }
        }
    }


}
