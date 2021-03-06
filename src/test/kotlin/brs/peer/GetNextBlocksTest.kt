package brs.peer

import brs.entity.Block
import brs.services.BlockchainService
import brs.objects.Genesis
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetNextBlocksTest {
    private lateinit var getNextBlocks: GetNextBlocks
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var mockPeer: Peer

    @Before
    fun setUpGetNextBlocksTest() {
        mockBlockchainService = mock()
        mockPeer = mock()
        val mockBlock = mock<Block>()
        whenever(mockBlock.toJsonObject()).doReturn(JsonObject())
        val blocks = mutableListOf<Block>()
        repeat(100) {
            blocks.add(mockBlock)
        }
        whenever(mockBlockchainService.getBlocksAfter(eq(Genesis.GENESIS_BLOCK_ID), any())).doReturn(blocks)
        getNextBlocks = GetNextBlocks(mockBlockchainService)
    }

    @Test
    fun testGetNextBlocks() {
        val request = JsonObject()
        request.addProperty("blockId", Genesis.GENESIS_BLOCK_ID.toUnsignedString())
        val responseElement = getNextBlocks.processRequest(request, mockPeer)
        assertNotNull(responseElement)
        assertTrue(responseElement.isJsonObject)
        val response = responseElement.asJsonObject
        assertTrue(response.has("nextBlocks"))
        val nextBlocksElement = response.get("nextBlocks")
        assertNotNull(nextBlocksElement)
        assertTrue(nextBlocksElement.isJsonArray)
        val nextBlocks = nextBlocksElement.asJsonArray
        assertEquals(100, nextBlocks.size().toLong())
        nextBlocks.forEach { nextBlock ->
            assertNotNull(nextBlock)
            assertTrue(nextBlock.isJsonObject)
        }
    }

    @Test
    fun testGetNextBlocks_noIdSpecified() {
        val request = JsonObject()
        val responseElement = getNextBlocks.processRequest(request, mockPeer)
        assertNotNull(responseElement)
        assertTrue(responseElement is JsonObject)
        val response = responseElement.asJsonObject
        assertTrue(response.has("nextBlocks"))
        val nextBlocksElement = response.get("nextBlocks")
        assertNotNull(nextBlocksElement)
        assertTrue(nextBlocksElement is JsonArray)
        val nextBlocks = nextBlocksElement.asJsonArray
        assertEquals(0, nextBlocks.size().toLong())
    }
}
