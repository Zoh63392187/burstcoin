package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.entity.DependencyProvider
import brs.entity.Transaction

class BroadcastTransactionBytesHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.TransactionBroadcastResult> {
    override fun handleRequest(transactionBytes: BrsApi.TransactionBytes): BrsApi.TransactionBroadcastResult {
        return BrsApi.TransactionBroadcastResult.newBuilder()
            .setNumberOfPeersSentTo(
                dp.transactionProcessorService.broadcast(
                    Transaction.parseTransaction(
                        dp,
                        transactionBytes.transactionBytes.toByteArray()
                    )
                )!!
            )
            .build()
    }
}
