package top.maplex.fomalhautshop.item.itemlib

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.plugin.NBTAPI
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

object ItemSaveLib {

    val items = ConcurrentHashMap<String, ItemStack>()

    fun getItem(id: String): ItemStack? {
        return items[id]
    }

    fun addItem(id: String, itemStack: ItemStack) {
        items[id] = itemStack
        runCatching {
            File(getDataFolder(), "save/$id.yml").apply {
                if (!exists()) {
                    createNewFile()
                }
            }
        }.getOrNull().let {
            if (it == null) {
                error("文件创建失败: $id 文件: save/$id.yml")
            }
            if (it.exists()) {
                it.createNewFile()
            }
            it.writeText(
                NBT.itemStackToNBT(itemStack).toString(),
                StandardCharsets.UTF_8
            )
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
