# AIOEconomy — Paper 1.21.11 Prototype

All-in-one economy, shop, auction house, selling, payments, and player trading plugin.

## Build on GitHub

Push this repository to GitHub. GitHub Actions will build the plugin automatically.
The resulting JAR is uploaded as a workflow artifact.

## Commands

### Players
- `/sell gui` — opens a selling GUI
- `/sell hand` — sells the item in your main hand
- `/sell allhand` — sells all matching items from your inventory
- `/ah` — opens the auction house
- `/ah sell` — lists the item in your hand for its configured/default auction price
- `/ah sell <price>` — lists the item in your hand for a price
- `/ah search <keyword>` — searches auction listings
- Click your own AH listing to take it off the AH and return the item to your inventory. Other players can click to buy it.
- `/shop` — opens the shop category GUI
- `/ah search hand` — searches listings matching your held item
- `/pay <player> <amount>`
- `/bal` or `/balance`
- `/baltop`
- `/trade <player>` — sends a request; the other player runs the same command to accept.

### Admin
- `/shopadd <section> <name>` — creates a shop section
- `/shopadd <item> <buyprice> <sellprice>` — adds the item in your hand to the selected/last shop section
- `/balset <player> <amount>`

## Prototype notes

This version intentionally uses YAML instead of an external database. It is suitable for
testing and a first server prototype. Auction listings, balances, shop configuration,
and pending trades are persisted under `plugins/AIOEconomy/`.

The shop item command uses the item in the admin's main hand. The current shop section is
the most recently created section.

The auction house uses a simple listing GUI and click-to-buy behavior. A future version
can add expiration, bidding, pagination, tax, confirmation screens, permissions,
transactions/history, and database-backed storage.
