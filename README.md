# MineStar Velocity

## Time format:

 `d` - days\
 `h` - hours\
 `m` - minutes\
 `s` - seconds

---

 **Example:** `1d2h3m4s` - 1 day 2 hours 3 minutes and 4 seconds

---

## Per-Server permissions:
### Format: `<suffix>.<server>`
### List:
**Suffix:** `msvelocity.moderation`\
**Commands:**\
`/mute`, `/unmute`,\
`/fine`, `/tempwarn`, `/warn`, `/unwarn`, \
`/tempban`, `/permban`, `/unban`\
**Example:** `msvelocity.moderation.create`

---

**Suffix:** `msvelocity.government`\
**Commands:** `/ticket`, `/unticket`\
**Example:** `msvelocity.government.main`

---

# Commands:

---

## Global:

---

**Usage:** `/msvreload`\
**Aliases:** None\
**Permission:** `msvelocity.admin.reload`\
**Discord support:** ✅ (only with `Administrator` permission)

---

## Discord:

---

**Usage:**\
In-game: `/link` (if lobby enabled)\
In discord: `/link <NICKNAME> <CODE>`\
**Aliases:** None\
**Permission:** None\
**Discord support:** ✅ (back-side usage)

---

**Usage:** `/unlink <NICKNAME>`\
**Aliases:** None\
**Permission:** `msvelocity.admin.discord.unlink`\
**Discord support:** ✅

---

## Punishments:

---

**Usage:** `/punishments` (for self)\
**Aliases:** `/offenses`\
**Permission:** `msvelocity.punishment.punishments`\
**Discord support:** ❌ (punishment channel)

---

**Usage:** `/punishments SERVER NICKNAME`\
**Aliases:** `/offenses`\
**Permission:** `msvelocity.punishment.punishments.other`\
**Discord support:** ❌ (punishment channel)

---

**Usage:** `/unban SERVER NICKNAME`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.ban.revoke`\
**Discord support:** ✅

---

**Usage:** `/unwarn SERVER NICKNAME NUMBER`\
**Aliases:** `/unfine`\
**Permission:** `msvelocity.punishment.warn.revoke`\
**Discord support:** ✅

---

**Usage:** `/unticket NICKNAME NUMBER`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.ticket.revoke`\
**Discord support:** ❌

---

**Usage:** `/unmute SERVER NICKNAME`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.mute.revoke`\
**Discord support:** ✅

---

**Usage:** `/permban SERVER NICKNAME REASON`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.ban.permanent`\
**Discord support:** ✅

---

**Usage:** `/tempban SERVER NICKNAME TIME REASON`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.ban.temporary`\
**Discord support:** ✅

---

**Usage:** `/warn SERVER NICKNAME REASON`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.warn.permanent`\
**Discord support:** ✅

---

**Usage:** `/tempwarn SERVER NICKNAME TIME REASON`\
**Aliases:** `/owarn`\
**Permission:** `msvelocity.punishment.warn.temporary`\
**Discord support:** ✅

---

**Usage:** `/fine SERVER NICKNAME VICTIM TIME REASON`\
**Aliases:** `/ywarn`\
**Permission:** `msvelocity.punishment.warn.fine`\
**Discord support:** ✅

---

**Usage:** `/ticket NICKNAME VICTIM TIME REASON`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.ticket`\
**Discord support:** ❌

---

**Usage:** `/mute SERVER NICKNAME TIME REASON`\
**Aliases:** None\
**Permission:** `msvelocity.punishment.mute`\
**Discord support:** ✅