package top.maplex.fomalhautshop.item.impl

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.parseToItemStack
import taboolib.platform.util.countItem
import taboolib.platform.util.hasLore
import taboolib.platform.util.hasName
import top.maplex.fomalhautshop.item.ShopItem
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

object ShopMinecraftItem : ShopItem {

    override val source: String = "MC"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        registerItem()
    }

    override fun getItemList(): MutableList<ItemStack> {
        return Material.values().map { ItemStack(it) }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return try {
            id.parseToItemStack()
        } catch (e: Exception) {
            null
        }
    }
    override fun getItemId(itemStack: ItemStack): String {
        if (itemStack.hasItemMeta() || itemStack.hasName() || itemStack.hasLore()) {
            return "none"
        }
        return itemStack.type.name
    }

    fun ItemStack.serializeToString(): String {
        val outputStream = ByteArrayOutputStream()
        val bukkitOutputStream = BukkitObjectOutputStream(outputStream)

        bukkitOutputStream.writeObject(this)
        bukkitOutputStream.flush()

        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    fun String.deserializeToItemStack(): ItemStack {
        val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(this))
        val bukkitInputStream = BukkitObjectInputStream(inputStream)

        return bukkitInputStream.readObject() as ItemStack
    }

}
