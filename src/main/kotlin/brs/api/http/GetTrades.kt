package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.isFalse
import brs.api.http.common.ResultFields.TRADES_RESPONSE
import brs.entity.Trade
import brs.services.AssetExchangeService
import brs.services.ParameterService
import brs.util.convert.emptyToNull
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetTrades internal constructor(
    private val parameterService: ParameterService,
    private val assetExchangeService: AssetExchangeService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.AE),
    ASSET_PARAMETER,
    ACCOUNT_PARAMETER,
    FIRST_INDEX_PARAMETER,
    LAST_INDEX_PARAMETER,
    INCLUDE_ASSET_INFO_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val assetId = request.getParameter(ASSET_PARAMETER).emptyToNull()
        val accountId = request.getParameter(ACCOUNT_PARAMETER).emptyToNull()

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val includeAssetInfo = !isFalse(request.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

        val response = JsonObject()
        val tradesData = JsonArray()
        val trades: Collection<Trade>
        trades = when {
            accountId == null -> {
                val asset = parameterService.getAsset(request)
                assetExchangeService.getTrades(asset.id, firstIndex, lastIndex)
            }
            assetId == null -> {
                val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
                assetExchangeService.getAccountTrades(account.id, firstIndex, lastIndex)
            }
            else -> {
                val asset = parameterService.getAsset(request)
                val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
                assetExchangeService.getAccountAssetTrades(account.id, asset.id, firstIndex, lastIndex)
            }
        }
        for (trade in trades) {
            val asset = if (includeAssetInfo) assetExchangeService.getAsset(trade.assetId) else null
            tradesData.add(JSONData.trade(trade, asset))
        }
        response.add(TRADES_RESPONSE, tradesData)

        return response
    }
}
