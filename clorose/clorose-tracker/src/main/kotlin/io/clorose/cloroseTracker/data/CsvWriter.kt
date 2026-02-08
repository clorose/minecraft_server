package io.clorose.cloroseTracker.data

import io.clorose.cloroseTracker.model.TransactionType
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class CsvWriter(private val dataFolder: File) {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val transactionsFile = File(dataFolder, "transactions.csv")
    private val balancesFile = File(dataFolder, "balances.csv")

    private val transactionsLock = Any()
    private val balancesLock = Any()

    fun init() {
        dataFolder.mkdirs()
        if (!transactionsFile.exists()) {
            transactionsFile.writeText("timestamp,player_uuid,player_name,type,amount,detail\n")
        }
        if (!balancesFile.exists()) {
            balancesFile.writeText("timestamp,player_uuid,player_name,balance,event\n")
        }
    }

    fun writeTransaction(
        uuid: UUID,
        name: String,
        type: TransactionType,
        amount: Double,
        detail: String,
    ) {
        val timestamp = LocalDateTime.now().format(formatter)
        val line = "$timestamp,$uuid,$name,${type.name},${"%.2f".format(amount)},${escapeCsv(detail)}\n"
        synchronized(transactionsLock) {
            BufferedWriter(FileWriter(transactionsFile, true)).use { writer ->
                writer.write(line)
                writer.flush()
            }
        }
    }

    fun writeBalance(
        uuid: UUID,
        name: String,
        balance: Double,
        event: String,
    ) {
        val timestamp = LocalDateTime.now().format(formatter)
        val line = "$timestamp,$uuid,$name,${"%.2f".format(balance)},$event\n"
        synchronized(balancesLock) {
            BufferedWriter(FileWriter(balancesFile, true)).use { writer ->
                writer.write(line)
                writer.flush()
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
