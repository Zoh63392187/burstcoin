package brs.api.http

import brs.entity.Account
import brs.entity.Asset
import brs.entity.AssetTransfer
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.TRANSFERS_RESPONSE
import brs.services.AccountService
import brs.services.ParameterService
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

class GetAssetTransfersTest : AbstractUnitTest() {

    private lateinit var t: GetAssetTransfers

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAccountService: AccountService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAccountService = mock()
        mockAssetExchangeService = mock()

        t = GetAssetTransfers(mockParameterService, mockAccountService, mockAssetExchangeService)
    }

    @Test
    fun processRequest_byAsset() {
        val assetId = 123L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockAssetTransfer = mock<AssetTransfer>()
        val mockAssetTransferIterator = mockCollection(mockAssetTransfer)

        whenever(mockParameterService.getAsset(eq(request))).doReturn(mockAsset)

        whenever(mockAssetExchangeService.getAssetTransfers(eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetTransferIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_byAccount() {
        val accountId = 234L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockAssetTransfer = mock<AssetTransfer>()
        val mockAssetTransferIterator = mockCollection(mockAssetTransfer)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        whenever(mockAccountService.getAssetTransfers(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetTransferIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_byAccountAndAsset() {
        val assetId = 123L
        val accountId = 234L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)
        whenever(mockAsset.name).doReturn("assetName")

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockAssetTransfer = mock<AssetTransfer>()
        whenever(mockAssetTransfer.assetId).doReturn(assetId)
        val mockAssetTransferIterator = mockCollection(mockAssetTransfer)

        whenever(mockParameterService.getAsset(eq(request))).doReturn(mockAsset)
        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        whenever(mockAssetExchangeService.getAsset(eq(mockAssetTransfer.assetId))).doReturn(mockAsset)

        whenever(mockAssetExchangeService.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetTransferIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(TRANSFERS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val transferInfoResult = resultList.get(0) as JsonObject
        assertEquals(assetId.toString(), transferInfoResult.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(mockAsset.name, transferInfoResult.get(NAME_RESPONSE).safeGetAsString())
    }

}
