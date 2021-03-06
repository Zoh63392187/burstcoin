package brs.entity

import brs.transaction.type.TransactionType
import kotlin.reflect.KClass

data class TransactionDuplicationKey(
    internal val transactionType: KClass<out TransactionType>?,
    internal val key: String
) {
    companion object {
        val IS_ALWAYS_DUPLICATE = TransactionDuplicationKey(null, "always")
        val IS_NEVER_DUPLICATE = TransactionDuplicationKey(null, "never")
    }
}
