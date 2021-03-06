package brs.api.http

import brs.api.http.common.ResultFields.ATS_RESPONSE
import brs.at.AT
import brs.at.AtMachineState
import brs.common.QuickMocker
import brs.common.TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED
import brs.entity.Account
import brs.services.ATService
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountATsTest {

    private lateinit var t: GetAccountATs

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockATService: ATService
    private lateinit var mockAccountService: AccountService

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockATService = mock()
        mockAccountService = mock()

        t = GetAccountATs(mockParameterService, mockATService, mockAccountService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccountId = 123L
        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(mockAccountId)

        val mockATId = 1L
        val mockATIDBytes = TEST_ACCOUNT_NUMERIC_ID_PARSED
        val creatorBytes = TEST_ACCOUNT_NUMERIC_ID_PARSED + 1
        val mockMachineState = mock<AtMachineState.MachineState>()
        val mockAT = mock<AT>()
        whenever(mockAT.creator).doReturn(creatorBytes)
        whenever(mockAT.id).doReturn(mockATIDBytes)
        whenever(mockAT.machineState).doReturn(mockMachineState)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        whenever(mockAccountService.getAccount(any<Long>())).doReturn(mockAccount)

        whenever(mockATService.getATsIssuedBy(eq(mockAccountId))).doReturn(listOf(mockATId))
        whenever(mockATService.getAT(eq(mockATId))).doReturn(mockAT)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val atsResultList = result.get(ATS_RESPONSE) as JsonArray
        assertNotNull(atsResultList)
        assertEquals(1, atsResultList.size().toLong())

        val atsResult = atsResultList.get(0) as JsonObject
        assertNotNull(atsResult)
    }

}
