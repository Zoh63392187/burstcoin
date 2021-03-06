package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.services.ParameterService
import brs.services.SubscriptionService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetSubscriptionsToAccount(
    private val parameterService: ParameterService,
    private val subscriptionService: SubscriptionService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT

        val response = JsonObject()

        val subscriptions = JsonArray()

        for (subscription in subscriptionService.getSubscriptionsToId(account.id)) {
            subscriptions.add(JSONData.subscription(subscription))
        }

        response.add("subscriptions", subscriptions)
        return response
    }
}
