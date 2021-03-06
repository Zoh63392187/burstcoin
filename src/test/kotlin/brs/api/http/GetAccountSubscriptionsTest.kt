package brs.api.http

import brs.entity.Account
import brs.entity.Subscription
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.SUBSCRIPTIONS_RESPONSE
import brs.api.http.common.ResultFields.AMOUNT_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.FREQUENCY_RESPONSE
import brs.api.http.common.ResultFields.ID_RESPONSE
import brs.api.http.common.ResultFields.TIME_NEXT_RESPONSE
import brs.services.ParameterService
import brs.services.SubscriptionService
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountSubscriptionsTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var subscriptionServiceMock: SubscriptionService

    private lateinit var t: GetAccountSubscriptions

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        subscriptionServiceMock = mock()

        t = GetAccountSubscriptions(parameterServiceMock, subscriptionServiceMock)
    }

    @Test
    fun processRequest() {
        val userId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, userId)
        )

        val account = mock<Account>()
        whenever(account.id).doReturn(userId)
        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(account)

        val subscription = mock<Subscription>()
        whenever(subscription.id).doReturn(1L)
        whenever(subscription.amountPlanck).doReturn(2L)
        whenever(subscription.frequency).doReturn(3)
        whenever(subscription.timeNext).doReturn(4)

        val subscriptionIterator = mockCollection(subscription)
        whenever(subscriptionServiceMock.getSubscriptionsByParticipant(eq(userId))).doReturn(subscriptionIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultSubscriptions = result.get(SUBSCRIPTIONS_RESPONSE) as JsonArray
        assertNotNull(resultSubscriptions)
        assertEquals(1, resultSubscriptions.size().toLong())

        val resultSubscription = resultSubscriptions.get(0) as JsonObject
        assertNotNull(resultSubscription)

        assertEquals(subscription.id.toString(), resultSubscription.get(ID_RESPONSE).safeGetAsString())
        assertEquals(subscription.amountPlanck.toString(), resultSubscription.get(AMOUNT_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(subscription.frequency.toLong(), resultSubscription.get(FREQUENCY_RESPONSE).safeGetAsLong())
        assertEquals(subscription.timeNext.toLong(), resultSubscription.get(TIME_NEXT_RESPONSE).safeGetAsLong())
    }

}
