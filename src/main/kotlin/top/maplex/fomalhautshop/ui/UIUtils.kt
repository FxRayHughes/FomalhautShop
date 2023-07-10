package top.maplex.fomalhautshop.ui

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.getItemStack
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import top.maplex.fomalhautshop.utils.asChar
import top.maplex.fomalhautshop.utils.papi

fun <T> Linked<T>.inits(data: String, player: Player, edit: Boolean = false) {
    val config = UIReader.getUIConfig(data)
    map(*config.getStringList("Layout").toTypedArray())
    config.getString("Commodity")?.asChar()?.let { slotsBy(it) } ?: slotsBy('@')
    config.getString("NextItem.slot")?.asChar()?.let { nextChar ->
        this.setNextPage(getFirstSlot(nextChar)) { page, hasNextPage ->
            if (hasNextPage) {
                config.getItemStack("NextItem.has").papi(player) ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                config.getItemStack("NextItem.normal").papi(player) ?: buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
    }
    config.getString("PreviousItem.slot")?.asChar()?.let { previoustChar ->
        this.setPreviousPage(getFirstSlot(previoustChar)) { page, hasPreviousPage ->
            if (hasPreviousPage) {
                config.getItemStack("PreviousItem.has").papi(player) ?: buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                config.getItemStack("PreviousItem.normal").papi(player) ?: buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }
    }

    config.getConfigurationSection("OtherItem")?.getKeys(false)?.forEach { key ->
        config.getItemStack("OtherItem.${key}.item")?.let {
            set(key.asChar(), it) {
                isCancelled = true
                if (clickEvent().isLeftClick) {
                    if (clickEvent().isShiftClick) {
                        config.getStringList("OtherItem.${key}.action.left_shift").eval(player)
                        return@set
                    }
                    config.getStringList("OtherItem.${key}.action.left").eval(player)
                    return@set
                }
                if (clickEvent().isRightClick) {
                    if (clickEvent().isShiftClick) {
                        config.getStringList("OtherItem.${key}.action.right_shift").eval(player)
                        return@set
                    }
                    config.getStringList("OtherItem.${key}.action.right").eval(player)
                    return@set
                }
            }
        }
    }

}

fun List<String>.eval(player: Player) {

    val scriptList = mutableListOf<String>()

    this.forEach {
        if (it.contains("link:")) {
            it.split(":").getOrNull(1)?.let { link ->
                UIReader.scriptConfig.getOrDefault(link, listOf()).forEach { script ->
                    scriptList.add(script)
                }
            }
        } else {
            scriptList.add(it)
        }
    }
    try {
        KetherShell.eval(
            scriptList,
            sender = adaptPlayer(player)
        )
    } catch (e: Throwable) {
        e.printKetherErrorMessage()
    }
}
