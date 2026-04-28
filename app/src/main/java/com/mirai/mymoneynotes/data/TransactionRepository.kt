package com.mirai.mymoneynotes.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TransactionRepository(context: Context) {
    private val transactionDao = AppDatabase.getDatabase(context).transactionDao()

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionCount(): Flow<Int> = transactionDao.getTransactionCount()

    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)

    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    fun getTotalIncome(): Flow<Double> = transactionDao.getTotalIncome()

    fun getTotalExpense(): Flow<Double> = transactionDao.getTotalExpense()

    fun getBalance(): Flow<Double> = transactionDao.getBalance()

    companion object {
        @Volatile
        private var INSTANCE: TransactionRepository? = null

        fun getInstance(context: Context): TransactionRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TransactionRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
