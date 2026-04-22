package com.mirai.mymoneynotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val date: Long,
    val description: String
)

enum class TransactionType {
    INCOME,
    EXPENSE
}
