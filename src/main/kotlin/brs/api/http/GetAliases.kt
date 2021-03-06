package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.ALIASES_RESPONSE
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.FilteringIterator
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAliases internal constructor(
    private val parameterService: ParameterService,
    private val aliasService: AliasService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.ALIASES),
    TIMESTAMP_PARAMETER,
    ACCOUNT_PARAMETER,
    FIRST_INDEX_PARAMETER,
    LAST_INDEX_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val timestamp = ParameterParser.getTimestamp(request)
        val accountId = parameterService.getAccount(request)?.id ?: return JSONResponses.INCORRECT_ACCOUNT
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val aliases = JsonArray()
        val aliasIterator = FilteringIterator(
            aliasService.getAliasesByOwner(accountId, 0, -1),
            { alias -> alias != null && alias.timestamp >= timestamp },
            firstIndex,
            lastIndex
        )
        while (aliasIterator.hasNext()) {
            val alias = aliasIterator.next()
            val offer = aliasService.getOffer(alias)
            aliases.add(JSONData.alias(alias, offer))
        }

        val response = JsonObject()
        response.add(ALIASES_RESPONSE, aliases)
        return response
    }
}
