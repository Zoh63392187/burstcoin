/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

*/
package brs.at

data class AtBlock internal constructor(val totalFees: Long, val totalAmountPlanck: Long, val bytesForBlock: ByteArray?)
