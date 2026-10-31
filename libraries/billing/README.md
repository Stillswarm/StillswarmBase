# Billing

Artifact: `com.stillswarm.base:foundation-billing:0.1.3`.

This first version supports **one-time, non-consumable lifetime unlocks**.
Subscriptions, consumable credits, rentals, and prepaid plans are not implemented.
Paid app downloads are configured separately in Play Console.

`Billing` exposes a `StateFlow<BillingState>` and `refresh()` (also restore).
`BillingSession` reconciles an allowlisted product catalog through `BillingBackend`,
`PurchaseVerifier`, and `EntitlementCache`. Apps map owned product IDs to features;
they should not derive premium access from checkout success alone.

- Pending purchases never grant access or get acknowledged.
- Verified purchases are persisted and granted before acknowledgement.
- Failed acknowledgement is retried on subsequent refreshes. Apps should call
  refresh on resume and expose retry; a client-only app cannot guarantee acknowledgement
  while it is closed. Google normally refunds unacknowledged purchases after three days.
- Query/verification failures preserve known access. A successful empty purchase
  list removes access. Catalog failures do not prevent ownership reconciliation.
- Verification `false` means invalid; throw for transient backend failures.
- Refreshes are serialized and bounded. Cancellation propagates; loading clears.
- `reconciled` means this session has reconciled at least once, not that it is online.
- No connectivity monitor, polling loop, or telemetry of purchase tokens.

`PreferenceEntitlementCache` stores only owned IDs in `:storage`. Use a unique
namespace per environment. It is a convenience for offline access, not authoritative
proof: local state can be modified, and offline refunds/account changes cannot be
observed until reconciliation. Exclude billing cache data from cloud/device backup
(or disable app backup) to avoid restoring another installation's cached access.
For stronger guarantees use server verification and an explicit offline policy.

See `integrations/play-billing` for the production adapter and local setup.
`testing/FakeBilling` drives the demo's simulated checkout and never calls Play.
