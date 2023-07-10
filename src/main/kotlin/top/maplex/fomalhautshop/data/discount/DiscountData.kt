package top.maplex.fomalhautshop.data.discount

import java.util.Arrays

data class DiscountData(
    val group: String,
    val permission: String,
    val shop: List<String>,
    val data: Map<String, Double>,
)
