package com.stillswarm.base.play.billing

import android.util.Base64
import com.stillswarm.base.billing.OwnedPurchase
import java.security.KeyPairGenerator
import java.security.Signature
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LocalSignatureVerifierTest {
    private val keys =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val verifier =
        LocalSignatureVerifier(
            base64PublicKey = Base64.encodeToString(keys.public.encoded, Base64.NO_WRAP),
            packageName = "test.app",
        )

    private fun purchase(): OwnedPurchase {
        val json =
            """{"packageName":"test.app","productId":"premium","purchaseState":0,"purchaseToken":"token"}"""
        val signature =
            Signature.getInstance("SHA1withRSA").run {
                initSign(keys.private)
                update(json.toByteArray(Charsets.UTF_8))
                sign()
            }
        return OwnedPurchase(
            token = "token",
            productIds = setOf("premium"),
            pending = false,
            acknowledged = false,
            originalJson = json,
            signature = Base64.encodeToString(signature, Base64.NO_WRAP),
        )
    }

    @Test
    fun validSignatureAndMatchingFieldsAreAccepted() = runBlocking {
        assertTrue(verifier.verify(purchase()))
    }

    @Test
    fun tamperedPayloadAndMismatchedTokenOrProductAreRejected() = runBlocking {
        val purchase = purchase()
        assertFalse(
            verifier.verify(
                purchase.copy(originalJson = purchase.originalJson.replace("premium", "other"))
            )
        )
        assertFalse(verifier.verify(purchase.copy(token = "other")))
        assertFalse(verifier.verify(purchase.copy(productIds = setOf("other"))))
        assertFalse(verifier.verify(purchase.copy(signature = "not a signature")))
    }

    @Test
    fun wrongAppIsRejected() = runBlocking {
        val other =
            LocalSignatureVerifier(
                base64PublicKey = Base64.encodeToString(keys.public.encoded, Base64.NO_WRAP),
                packageName = "other.app",
            )
        assertFalse(other.verify(purchase()))
    }
}
