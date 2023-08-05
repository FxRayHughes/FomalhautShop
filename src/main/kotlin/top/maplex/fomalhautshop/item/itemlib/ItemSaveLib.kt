package top.maplex.fomalhautshop.item.itemlib

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.plugin.NBTAPI
import github.saukiya.sxitem.SXItem
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import top.maplex.fomalhautshop.item.impl.ShopSXItem
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

object ItemSaveLib {

    val items = ConcurrentHashMap<String, ItemStack>()

    fun getItem(id: String): ItemStack? {
        return items[id]
    }

    fun addItem(id: String, itemStack: ItemStack): Boolean {
        items[id] = itemStack
        runCatching {
            val file = File(getDataFolder(), "save/$id.yml").apply {
                if (!exists()) {
                    createNewFile()
                }
            }
            file.writeText(
                NBT.itemStackToNBT(itemStack).toString(),
                StandardCharsets.UTF_8
            )
            return true
        }.getOrElse {
            if (Bukkit.getPluginManager().getPlugin("SX-Item") != null) {
                SXItem.getItemManager().saveItem(id, itemStack, "Import")
                info("文件转为SXItem")
                return true
            } else {
                info("文件创建或写入失败: $id 文件: save/$id.yml")
                return false
            }
        }
    }

    fun loadItem(id: String) {
        val file = newFile(getDataFolder(), "save/$id.yml")
        if (file.exists()) {
            return
        }
        val itemStack = NBT.parseNBT(file.readText(StandardCharsets.UTF_8))
        items[id] = NBT.itemStackFromNBT(itemStack)
    }

    //loadAllItem
    @Awake(LifeCycle.ENABLE)
    fun loadAllItem() {
        val file = newFile(getDataFolder(), "save/", folder = true, create = true).listFiles()
        if (file != null) {
            for (i in file) {
                val itemStack = NBT.parseNBT(i.readText(StandardCharsets.UTF_8))
                items[i.nameWithoutExtension] = NBT.itemStackFromNBT(itemStack)
            }
        }
    }

    fun deleteItem(name: String) {
        items.remove(name)
        newFile(getDataFolder(), "save/$name.yml").delete()
    }

}
