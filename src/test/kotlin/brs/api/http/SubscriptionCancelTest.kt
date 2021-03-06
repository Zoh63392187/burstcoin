package brs.api.http

import brs.entity.Account
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.common.Parameters.SUBSCRIPTION_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.entity.DependencyProvider
import brs.entity.Subscription
import brs.services.ParameterService
import brs.services.SubscriptionService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.util.json.safeGetAsLong
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SubscriptionCancelTest : AbstractTransactionTest() {

    private lateinit var t: SubscriptionCancel
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var subscriptionServiceMock: SubscriptionService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        subscriptionServiceMock = mock()
        blockchainServiceMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            subscriptionServiceMock,
            blockchainServiceMock,
            apiTransactionManagerMock
        )
        t = SubscriptionCancel(dp)
    }

    @Test
    fun processRequest() {
        val subscriptionIdParameter = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionIdParameter)
        )

        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(1L)

        val mockSubscription = mock<Subscription>()
        whenever(mockSubscription.id).doReturn(subscriptionIdParameter)
        whenever(mockSubscription.senderId).doReturn(1L)
        whenever(mockSubscription.recipientId).doReturn(2L)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockSender)
        whenever(subscriptionServiceMock.getSubscription(eq(subscriptionIdParameter))).doReturn(mockSubscription)

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.AdvancedPaymentSubscriptionCancel
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.type.advancedPayment.SubscriptionCancel)
        assertEquals(subscriptionIdParameter, attachment.subscriptionId)
    }

    @Test
    fun processRequest_missingSubscriptionParameter() {
        val request = QuickMocker.httpServletRequest()

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(3L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_failedToParseSubscription() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, "notALong")
        )

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(4L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_subscriptionNotFound() {
        val subscriptionId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionId)
        )

        whenever(subscriptionServiceMock.getSubscription(eq(subscriptionId))).doReturn(null)

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(5L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_userIsNotSenderOrRecipient() {
        val subscriptionId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionId)
        )

        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(1L)

        val mockSubscription = mock<Subscription>()
        whenever(mockSubscription.senderId).doReturn(2L)
        whenever(mockSubscription.recipientId).doReturn(3L)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockSender)
        whenever(subscriptionServiceMock.getSubscription(eq(subscriptionId))).doReturn(mockSubscription)

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(7L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }
}
