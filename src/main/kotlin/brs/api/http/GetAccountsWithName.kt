package brs.api.http

import brs.api.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.services.AccountService
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAccountsWithName internal constructor(private val accountService: AccountService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), NAME_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accounts = accountService.getAccountsWithName(request.getParameter(NAME_PARAMETER))
        val accountIds = JsonArray()

        for (account in accounts) {
            accountIds.add(account.id.toUnsignedString())
        }

        val response = JsonObject()
        response.add(ACCOUNTS_RESPONSE, accountIds)
        return response
    }
}
