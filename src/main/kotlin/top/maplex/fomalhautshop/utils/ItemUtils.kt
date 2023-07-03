package top.maplex.fomalhautshop.utils

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.module.chat.colored
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta


fun ItemStack?.ifAir(): ItemStack? {
    if (this == null) {
        return null
    }
    if (this.isAir) {
        return null
    }
    if (this.type == Material.AIR) {
        return null
    }
    return this
}

fun ItemStack.getString(key: String, def: String = "null"): String {
    if (this.isAir) {
        return def
    }
    if (key.contains(".")) {
        return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asString()
    }
    return this.getItemTag().getOrElse(key, ItemTagData(def)).asString()
}

fun ItemStack.getInt(key: String, def: Int = -1): Int {
    if (this.isAir) {
        return def
    }
    if (key.contains(".")) {
        return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asInt()
    }
    return this.getItemTag().getOrElse(key, ItemTagData(def)).asInt()
}

fun ItemStack.getDouble(key: String, def: Double = -1.0): Double {
    if (this.isAir) {
        return def
    }
    if (key.contains(".")) {
        return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asDouble()
    }
    return this.getItemTag().getOrElse(key, ItemTagData(def)).asDouble()
}

fun ItemStack.getStringList(key: String): List<String> {
    if (this.isAir) {
        return listOf()
    }
    if (key.contains(".")) {
        return this.getItemTag().getDeep(key)?.asList()?.map { it.asString() } ?: listOf()
    }
    return this.getItemTag()[key]?.asList()?.map { it.asString() } ?: listOf()
}

fun ItemStack.getDoubleList(key: String): List<Double> {
    if (this.isAir) {
        return listOf()
    }
    if (key.contains(".")) {
        return this.getItemTag().getDeep(key)?.asList()?.map { it.asDouble() } ?: listOf()
    }
    return this.getItemTag()[key]?.asList()?.map { it.asDouble() } ?: listOf()
}

fun ItemStack.getIntList(key: String): List<Int> {
    if (this.isAir) {
        return listOf()
    }
    if (key.contains(".")) {
        return this.getItemTag().getDeep(key)?.asList()?.map { it.asInt() } ?: listOf()
    }
    return this.getItemTag()[key]?.asList()?.map { it.asInt() } ?: listOf()
}

fun ItemStack.set(key: String, value: Any?) {
    val tag = getItemTag()
    if (key.contains(".")) {
        if (value == null) {
            tag.removeDeep(key)
        } else {
            tag.putDeep(key, value)
        }
    } else {
        if (value == null) {
            tag.remove(key)
        } else {
            tag.put(key, value)
        }
    }
    tag.saveTo(this)
}

fun ItemStack?.papi(player: Player): ItemStack? {
    if (this == null) {
        return null
    }
    if (this.isAir()) {
        return this
    }
    modifyMeta<ItemMeta> {
        if (this.hasDisplayName()) {
            setDisplayName(displayName.replacePlaceholder(player))
        }
    }
    modifyLore {
        val clone = map { it.replacePlaceholder(player).colored() }.toMutableList()
        this.clear()
        this.addAll(clone)
    }
    return this
}
