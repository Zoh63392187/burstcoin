package brs.api.http
import brs.api.http.common.ResultFields.DELISTED_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.GOODS_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_RESPONSE
import brs.api.http.common.ResultFields.TAGS_RESPONSE
import brs.api.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.common.QuickMocker
import brs.entity.Goods
import brs.services.ParameterService
import brs.util.json.safeGetAsBoolean
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetDGSGoodTest {

    private lateinit var t: GetDGSGood

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mock()

        t = GetDGSGood(mockParameterService)
    }

    @Test
    fun processRequest() {
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(1L)
        whenever(mockGoods.name).doReturn("name")
        whenever(mockGoods.description).doReturn("description")
        whenever(mockGoods.quantity).doReturn(2)
        whenever(mockGoods.pricePlanck).doReturn(3L)
        whenever(mockGoods.tags).doReturn("tags")
        whenever(mockGoods.isDelisted).doReturn(true)
        whenever(mockGoods.timestamp).doReturn(12345)

        val request = QuickMocker.httpServletRequest()

        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        assertEquals(mockGoods.id.toString(), result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGoods.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGoods.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGoods.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals(mockGoods.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockGoods.tags, result.get(TAGS_RESPONSE).safeGetAsString())
        assertEquals(mockGoods.isDelisted, result.get(DELISTED_RESPONSE).safeGetAsBoolean())
        assertEquals(mockGoods.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
    }
}