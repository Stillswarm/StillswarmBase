package com.stillswarm.base.play.billing

import android.util.Base64
import com.stillswarm.base.billing.OwnedPurchase
import com.stillswarm.base.billing.PurchaseVerifier
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import org.json.JSONObject

/**
 * Client-only fallback for low-value lifetime unlocks; not a substitute for server verification.
 */
class LocalSignatureVerifier(base64PublicKey: String, private val packageName: String) :
    PurchaseVerifier {
    private val key =
        KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(Base64.decode(base64PublicKey, Base64.DEFAULT)))

    override suspend fun verify(purchase: OwnedPurchase): Boolean =
        try {
            val json = JSONObject(purchase.originalJson)
            val signedToken = json.optString("token", json.optString("purchaseToken"))
            val signedProducts =
                json.optJSONArray("productIds")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }.toSet()
                } ?: setOf(json.optString("productId"))
            json.optString("packageName") == packageName &&
                json.optInt("purchaseState", -1) == 0 &&
                signedToken.isNotBlank() &&
                signedToken == purchase.token &&
                signedProducts == purchase.productIds &&
                Signature.getInstance("SHA1withRSA").run {
                    initVerify(key)
                    update(purchase.originalJson.toByteArray(Charsets.UTF_8))
                    verify(Base64.decode(purchase.signature, Base64.DEFAULT))
                }
        } catch (_: Exception) {
            false
        }
}
