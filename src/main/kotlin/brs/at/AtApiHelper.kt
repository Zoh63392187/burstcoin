/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

*/

package brs.at

import brs.at.AtApi.Companion.REGISTER_PART_SIZE
import brs.util.byteArray.partEquals
import burst.kit.crypto.BurstCrypto
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.experimental.and

object AtApiHelper {

    private val burstCrypto = BurstCrypto.getInstance()

    fun longToHeight(x: Long): Int {
        return (x shr 32).toInt()
    }

    /**
     * Little Endian.
     */
    fun getLong(bytes: ByteArray): Long {
        return burstCrypto.bytesToLong(bytes)
    }

    /**
     * Little Endian.
     */
    fun getByteArray(long: Long, dest: ByteArray, offset: Int = 0) {
        // TODO integrate with BurstKit if/when it provides a method that does not create a new array
        // TODO optimize to iterate upwards rather than downwards
        require(dest.size - offset >= 8)
        var l = long
        for (i in 7 downTo 0) {
            dest[offset + 7 - i] = (l and 0xFF).toByte()
            l = l shr 8
        }
    }

    /**
     * Little Endian.
     */
    fun getByteArray(long: Long): ByteArray {
        val bytes = burstCrypto.longToBytes(long)
        bytes.reverse()
        return bytes
    }

    /**
     * Little Endian.
     */
    fun hashLong(messageDigest: MessageDigest, long: Long) {
        var l = long
        for (i in 7 downTo 0) {
            messageDigest.update((l and 0xFF).toByte())
            l = l shr 8
        }
    }

    fun longToNumOfTx(x: Long): Int {
        return x.toInt()
    }

    internal fun getLongTimestamp(height: Int, numOfTx: Int): Long {
        return height.toLong() shl 32 or numOfTx.toLong()
    }

    fun getBigInteger(b1: ByteArray, b2: ByteArray, b3: ByteArray, b4: ByteArray): BigInteger {
        return BigInteger(
            byteArrayOf(
                b4[7],
                b4[6],
                b4[5],
                b4[4],
                b4[3],
                b4[2],
                b4[1],
                b4[0],
                b3[7],
                b3[6],
                b3[5],
                b3[4],
                b3[3],
                b3[2],
                b3[1],
                b3[0],
                b2[7],
                b2[6],
                b2[5],
                b2[4],
                b2[3],
                b2[2],
                b2[1],
                b2[0],
                b1[7],
                b1[6],
                b1[5],
                b1[4],
                b1[3],
                b1[2],
                b1[1],
                b1[0]
            )
        )
    }

    fun getByteArray(bigInt: BigInteger): ByteArray { // TODO optimize
        val resultSize = 32
        val bigIntBytes = bigInt.toByteArray()
        bigIntBytes.reverse()
        val result = ByteArray(resultSize)
        if (bigIntBytes.size < resultSize) {
            val padding = ((bigIntBytes[bigIntBytes.size - 1] and 0x80.toByte()).toInt() shr 7).toByte()
            var i = 0
            val length = resultSize - bigIntBytes.size
            while (i < length) {
                result[resultSize - 1 - i] = padding
                i++
            }
        }
        System.arraycopy(
            bigIntBytes,
            0,
            result,
            0,
            if (resultSize >= bigIntBytes.size) bigIntBytes.size else resultSize
        )
        return result
    }
}

fun AtMachineState.putInA(data: ByteArray) {
    require(data.size == 32)
    data.copyInto(this.a1, 0, 0, 8)
    data.copyInto(this.a2, 0, 8, 16)
    data.copyInto(this.a3, 0, 16, 24)
    data.copyInto(this.a4, 0, 24, 32)
}

fun AtMachineState.putInB(data: ByteArray) {
    require(data.size == 32)
    data.copyInto(this.b1, 0, 0, 8)
    data.copyInto(this.b2, 0, 8, 16)
    data.copyInto(this.b3, 0, 16, 24)
    data.copyInto(this.b4, 0, 24, 32)
}

fun AtMachineState.bEquals(data: ByteArray): Boolean {
    require(data.size == 32)
    return data.partEquals(this.b1, 0, REGISTER_PART_SIZE)
            && data.partEquals(this.b2, 8, REGISTER_PART_SIZE)
            && data.partEquals(this.b3, 16, REGISTER_PART_SIZE)
            && data.partEquals(this.b4, 24, REGISTER_PART_SIZE)
}
