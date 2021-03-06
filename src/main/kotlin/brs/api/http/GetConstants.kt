package brs.api.http

import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.objects.FluxValues
import brs.objects.Genesis
import brs.transaction.type.TransactionType
import brs.util.convert.toUnsignedString
import brs.util.json.toJsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetConstants(dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO)) {

    private val constants: JsonElement

    init {
        val response = JsonObject()
        response.addProperty("genesisBlockId", Genesis.GENESIS_BLOCK_ID.toUnsignedString())
        response.addProperty("genesisAccountId", Genesis.CREATOR_ID.toUnsignedString())
        response.addProperty("maxBlockPayloadLength", dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH))
        response.addProperty("maxArbitraryMessageLength", Constants.MAX_ARBITRARY_MESSAGE_LENGTH)

        val transactionTypes = JsonArray()
        dp.transactionTypes
            .forEach { (key, value) ->
                val transactionType = JsonObject()
                transactionType.addProperty("value", key)
                transactionType.addProperty("description", TransactionType.getTypeDescription(key))
                val transactionSubtypes = JsonArray()
                transactionSubtypes.addAll(value.entries.map { entry ->
                    val transactionSubtype = JsonObject()
                    transactionSubtype.addProperty("value", entry.key)
                    transactionSubtype.addProperty("description", entry.value.description)
                    transactionSubtype
                }.toJsonArray())
                transactionType.add("subtypes", transactionSubtypes)
                transactionTypes.add(transactionType)
            }
        response.add("transactionTypes", transactionTypes)

        val peerStates = JsonArray()
        var peerState = JsonObject()
        peerState.addProperty("value", 0)
        peerState.addProperty("description", "Non-connected")
        peerStates.add(peerState)
        peerState = JsonObject()
        peerState.addProperty("value", 1)
        peerState.addProperty("description", "Connected")
        peerStates.add(peerState)
        peerState = JsonObject()
        peerState.addProperty("value", 2)
        peerState.addProperty("description", "Disconnected")
        peerStates.add(peerState)
        response.add("peerStates", peerStates)

        // TODO remove this always empty field
        response.add("requestTypes", JsonObject())

        constants = response
    }

    override fun processRequest(request: HttpServletRequest): JsonElement {
        return constants
    }
}
