package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.api.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.api.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.api.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.api.http.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.services.TimeService
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsPurchase
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DGSPurchaseTest : AbstractTransactionTest() {

    private lateinit var t: DGSPurchase
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var mockAccountService: AccountService
    private lateinit var mockTimeService: TimeService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock()
        whenever(mockParameterService.getSenderAccount(any())).doReturn(mock())
        mockBlockchainService = mock()
        mockAccountService = mock()
        mockTimeService = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            mockParameterService,
            mockBlockchainService,
            mockAccountService,
            mockTimeService,
            apiTransactionManagerMock
        )
        t = DGSPurchase(dp)
    }

    @Test
    fun processRequest() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockSellerId = 123L
        val mockGoodsId = 123L
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(mockGoodsId)
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.pricePlanck).doReturn(10L)
        whenever(mockGoods.sellerId).doReturn(mockSellerId)

        val mockSellerAccount = mock<Account>()

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)
        whenever(mockTimeService.epochTime).doReturn(10)

        whenever(mockAccountService.getAccount(eq(mockSellerId))).doReturn(mockSellerAccount)
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsPurchase
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsPurchase)
        assertEquals(goodsQuantity.toLong(), attachment.quantity.toLong())
        assertEquals(goodsPrice, attachment.pricePlanck)
        assertEquals(deliveryDeadlineTimestamp, attachment.deliveryDeadlineTimestamp.toLong())
        assertEquals(mockGoodsId, attachment.goodsId)
    }

    @Test
    fun processRequest_unknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPurchaseQuantity() {
        val goodsQuantity = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(4)

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_PURCHASE_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPurchasePrice() {
        val goodsQuantity = 5
        val goodsPrice = 5L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.pricePlanck).doReturn(10L)

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_PURCHASE_PRICE, t.processRequest(request))
    }


    @Test
    fun processRequest_missingDeliveryDeadlineTimestamp() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.pricePlanck).doReturn(10L)

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(MISSING_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDeliveryDeadlineTimestamp_unParsable() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, "unParsable")
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.pricePlanck).doReturn(10L)

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDeliveryDeadlineTimestamp_beforeCurrentTime() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.pricePlanck).doReturn(10L)

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)
        whenever(mockTimeService.epochTime).doReturn(1000)

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(request))
    }
}
