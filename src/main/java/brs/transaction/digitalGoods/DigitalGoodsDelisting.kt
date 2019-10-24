package brs.transaction.digitalGoods

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.toJsonString
import brs.util.convert.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class DigitalGoodsDelisting(dp: DependencyProvider) : DigitalGoods(dp) {
    override val subtype = SUBTYPE_DIGITAL_GOODS_DELISTING
    override val description = "Delisting"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.DigitalGoodsDelisting(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.DigitalGoodsDelisting(dp, attachmentData)

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsDelisting
        dp.digitalGoodsStoreService.delistGoods(attachment.goodsId)
    }

    override fun doValidateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsDelisting
        val goods = dp.digitalGoodsStoreService.getGoods(attachment.goodsId)
        if (goods != null && transaction.senderId != goods.sellerId) {
            throw BurstException.NotValidException("Invalid digital goods delisting - seller is different: " + attachment.jsonObject.toJsonString())
        }
        if (goods == null || goods.isDelisted) {
            throw BurstException.NotCurrentlyValidException("Goods ${attachment.goodsId.toUnsignedString()} not yet listed or already delisted")
        }
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.DigitalGoodsDelisting
        return TransactionDuplicationKey(DigitalGoodsDelisting::class, attachment.goodsId.toUnsignedString())
    }

    override fun hasRecipient() = false
}
