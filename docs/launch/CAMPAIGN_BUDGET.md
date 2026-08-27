# Atlas Cyberdeck — Campaign Budget Worksheet

## Rule

**Do not choose the funding goal first.**

Calculate what the campaign must actually fund.

---

# Cost Model

Fill these with defensible estimates.

| Category | Low | Expected | High | Notes |
|---|---:|---:|---:|---|
| Development reserve | $0 | $0 | $0 | Time allocated to 1.0 work |
| Android test devices | $0 | $0 | $0 | Vendor / ABI / OS coverage |
| Hosting / services | $0 | $0 | $0 | Site, email, build services |
| Signing / distribution | $0 | $0 | $0 | Release infrastructure |
| Legal / accounting | $0 | $0 | $0 | Tax, licensing, business review |
| Campaign video / audio | $0 | $0 | $0 | Equipment or contractor costs |
| Design / campaign assets | $0 | $0 | $0 | Only if outside help is used |
| Documentation / support | $0 | $0 | $0 | Public docs / support workflow |
| Reward fulfillment | $0 | $0 | $0 | Prefer digital where possible |
| Contingency | $0 | $0 | $0 | Engineering / fulfillment buffer |
| **Subtotal** | **$0** | **$0** | **$0** | Before fees |

---

# Fee Planning

Current Kickstarter planning assumption for a successfully funded campaign:

```text
Kickstarter platform fee : 5%
Payment processing       : approximately 3–5%
```

For conservative internal modeling, use **10%** for combined platform/payment fees until the exact final payment structure is confirmed.

Example:

```text
Required net campaign budget : $20,000
Fee reserve at 10%           : $2,222
Approx. gross target         : $22,222
```

Formula:

```text
Gross target = Required net / (1 - fee rate)
```

At 10%:

```text
Gross target = Required net / 0.90
```

Taxes and any additional business obligations are separate from this simplified fee calculation.

---

# Goal Reality Check

Before locking the goal, answer:

- How large is the current mailing list?
- How many supporters are realistically reachable on day one?
- What percentage can plausibly convert?
- What is the average expected pledge?
- What is the minimum amount Atlas actually needs?
- Can every promised reward be fulfilled at the goal amount?
- What happens if the campaign barely reaches the goal?
- What happens if it raises 2×?
- What happens if it raises 10×?

A low goal that cannot deliver the project is dangerous.

A high goal unsupported by the current audience can kill an otherwise viable campaign because Kickstarter is all-or-nothing.

---

# Backer Math

Use this table once the likely average pledge is known.

| Goal | Avg. Pledge | Approx. Backers Needed |
|---:|---:|---:|
| $10,000 | $35 | 286 |
| $10,000 | $50 | 200 |
| $15,000 | $50 | 300 |
| $20,000 | $50 | 400 |
| $25,000 | $50 | 500 |
| $25,000 | $75 | 334 |
| $50,000 | $75 | 667 |

These are simple planning numbers and do not account for failed payments, refunds, taxes, or pledge distribution.

---

# Do Not Lock Yet

The following remain intentionally undecided:

```text
Public funding goal
Campaign duration
Final reward prices
Lifetime entitlement
Subscription pricing
Delivery date
Stretch goals
```

Lock them only after audience, budget, and fulfillment analysis.
