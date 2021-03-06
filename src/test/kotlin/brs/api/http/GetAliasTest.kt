package brs.api.http

import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.common.QuickMocker
import brs.api.http.common.ResultFields.ALIAS_NAME_RESPONSE
import brs.api.http.common.ResultFields.BUYER_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.services.AliasService
import brs.services.ParameterService
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

class GetAliasTest {

    private lateinit var t: GetAlias

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAliasService: AliasService

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAliasService = mock()

        t = GetAlias(mockParameterService, mockAliasService)
    }

    @Test
    fun processRequest() {
        val mockAlias = mock<Alias>()
        whenever(mockAlias.aliasName).doReturn("mockAliasName")

        val mockOffer = mock<Offer>()
        whenever(mockOffer.pricePlanck).doReturn(123L)
        whenever(mockOffer.buyerId).doReturn(345L)

        val request = QuickMocker.httpServletRequest()

        whenever(mockParameterService.getAlias(eq(request))).doReturn(mockAlias)
        whenever(mockAliasService.getOffer(eq(mockAlias))).doReturn(mockOffer)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
        assertEquals(mockAlias.aliasName, result.get(ALIAS_NAME_RESPONSE).safeGetAsString())
        assertEquals(mockOffer.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockOffer.buyerId.toString(), result.get(BUYER_RESPONSE).safeGetAsString())
    }

}
