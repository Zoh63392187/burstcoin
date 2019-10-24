package brs.services

import brs.Account
import brs.Account.*
import brs.AssetTransfer
import brs.util.Observable

interface AccountService : Observable<Account, Event> {
    fun getCount(): Int

    fun addAssetListener(eventType: Event, listener: (AccountAsset) -> Unit)

    fun getAccount(id: Long): Account?

    fun getAccount(id: Long, height: Int): Account?

    fun getAccount(publicKey: ByteArray): Account?

    fun getAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAssets(accountId: Long, from: Int, to: Int): Collection<AccountAsset>

    fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<RewardRecipientAssignment>

    fun getAccountsWithName(name: String): Collection<Account>

    fun getAllAccounts(from: Int, to: Int): Collection<Account>

    fun getOrAddAccount(id: Long): Account

    fun flushAccountTable()

    // TODO rename methods
    fun addToForgedBalanceNQT(account: Account, amountNQT: Long)

    fun setAccountInfo(account: Account, name: String, description: String)

    fun addToAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    fun addToUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    fun addToAssetAndUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    fun addToBalanceNQT(account: Account, amountNQT: Long)

    fun addToUnconfirmedBalanceNQT(account: Account, amountNQT: Long)

    fun addToBalanceAndUnconfirmedBalanceNQT(account: Account, amountNQT: Long)

    fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment?

    fun setRewardRecipientAssignment(account: Account, recipient: Long)

    fun getUnconfirmedAssetBalanceQNT(account: Account, assetId: Long): Long
}
