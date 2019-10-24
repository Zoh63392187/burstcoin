package brs.http

import brs.TransactionProcessor
import brs.http.common.ResultFields.DONE_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class ClearUnconfirmedTransactions internal constructor(private val transactionProcessor: TransactionProcessor) : APIServlet.JsonRequestHandler(arrayOf(APITag.DEBUG)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        try {
            transactionProcessor.clearUnconfirmedTransactions()
            response.addProperty(DONE_RESPONSE, true)
        } catch (e: RuntimeException) {
            response.addProperty(ERROR_RESPONSE, e.toString())
        }

        return response
    }

    override fun requirePost(): Boolean {
        return true
    }

}
