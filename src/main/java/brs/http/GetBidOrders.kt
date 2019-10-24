package brs.http

import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.BID_ORDERS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetBidOrders internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val assetId = parameterService.getAsset(request).id
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val orders = JsonArray()
        for (bidOrder in assetExchange.getSortedBidOrders(assetId, firstIndex, lastIndex)) {
            orders.add(JSONData.bidOrder(bidOrder))
        }

        val response = JsonObject()
        response.add(BID_ORDERS_RESPONSE, orders)
        return response
    }

}
