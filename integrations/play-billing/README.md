# Google Play Billing

Artifact: `com.stillswarm.base:foundation-play-billing:0.1.0-SNAPSHOT`.

Uses Play Billing 8.3.0. This adapter supports configured **lifetime INAPP products**
with one eligible purchase option. It rejects rental/preorder offers and ambiguous
multi-offer catalogs; never configure consumables or time-limited products as lifetime IDs.
No subscription or consumable APIs are provided.

Own one `PlayBilling` instance per application process. Pass application context,
allowed product IDs, a `PurchaseVerifier`, and `PreferenceEntitlementCache`.
Observe `state` from ViewModels with lifecycle-aware collection. Launch
`purchase(activity, productId)` from a UI coroutine: the Activity is used only
for checkout and is not stored. Product details are fetched fresh before launch.
Call `onResume()` from the host Activity and expose `refresh()` as Restore purchases.
The adapter connects lazily, uses SDK reconnection, handles asynchronous purchase
updates, pending payments, cancellation, already-owned results, and acknowledgement.
Call `close()` when permanently disposing a non-process owner.

For low-value client-only lifetime unlocks, `LocalSignatureVerifier` checks the
RSA signature, package, signed token/products, and signed purchased state. Supply
the app's **Base64 licensing public key** from Play Console (Monetization setup /
Licensing), not a Firebase API key or service-account private key. Client verification
is bypassable on a modified app and cannot independently discover refunds. A secure
backend verifier using Google Play Developer API is recommended for stronger protection.
A backend verifier must validate package, token, product, purchase state, and ownership;
throw on network failures rather than returning false. Do not embed server credentials.

## Demo setup

The default demo uses simulated billing. For real Play testing:

1. Payments profile: already configured by the project owner.
2. Use the Play Console app matching `com.example.stillswarmbase`.
3. Create and activate a one-time product, e.g. `premium_lifetime`, with one Buy
   purchase option and the desired price/countries. Do not configure a rental.
4. In the ignored root `local.properties`, add:
   ```properties
   billing.productId=premium_lifetime
   billing.publicKey=YOUR_BASE64_APP_LICENSING_PUBLIC_KEY
   ```
   The public key is public app configuration, not a secret. Never add a private key.
5. Build and upload a signed bundle to an internal testing track. Enrol a tester,
   and add that Google account separately to Play Console's **License testing**.
6. Install from the test track using that account. Open **Try payments → Google Play
   checkout**. Confirm Google shows a test payment method before purchasing; track
   access alone does not prevent real charges.
7. Exercise approved/declined/pending test methods, complete a pending payment,
   background/resume, reinstall/restore, and refund/revoke then reconcile.

App-specific prices come from Play. A missing configuration shows setup text;
there is no fake fallback in the real checkout screen. Automated instrumentation
uses TestDemoApplication, which disables real billing even if local values exist.
Real purchase delivery and Play Console configuration require a license-test run;
unit tests and the simulation do not establish that live checkout works.

The demo retains cached lifetime access offline. Billing data shares the default
DataStore; that file is excluded from demo cloud backup and device transfer (so
demo appearance preferences are also excluded). Apps should exclude this cache
or place it in a separately excluded store.

References:
- https://developer.android.com/google/play/billing/integrate
- https://developer.android.com/google/play/billing/security
- https://developer.android.com/google/play/billing/test
