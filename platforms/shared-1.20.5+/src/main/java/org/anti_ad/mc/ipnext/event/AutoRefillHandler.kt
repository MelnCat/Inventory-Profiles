/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2019-2020 jsnimda <7615255+jsnimda@users.noreply.github.com>
 *   Copyright (c) 2021-2022 Plamen K. Kosseff <p.kosseff@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("UnusedImport")

package org.anti_ad.mc.ipnext.event

import org.anti_ad.mc.alias.client.gui.screen.ingame.ContainerScreen
import org.anti_ad.mc.alias.enchantment.Enchantments
import org.anti_ad.mc.alias.inventory.PlayerInventory
import org.anti_ad.mc.alias.item.ArmorItem
import org.anti_ad.mc.alias.item.AxeItem
import org.anti_ad.mc.alias.item.FishingRodItem
import org.anti_ad.mc.alias.item.HoeItem
import org.anti_ad.mc.alias.item.Items
import org.anti_ad.mc.alias.item.PickaxeItem
import org.anti_ad.mc.alias.item.ShovelItem
import org.anti_ad.mc.alias.item.SwordItem
import org.anti_ad.mc.alias.item.ToolItem
import org.anti_ad.mc.alias.text.fromSerializedJson
import org.anti_ad.mc.common.extensions.tryCatch
import org.anti_ad.mc.common.gui.NativeContext
import org.anti_ad.mc.common.math2d.Point
import org.anti_ad.mc.common.math2d.Rectangle
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.common.vanilla.VanillaUtil
import org.anti_ad.mc.common.vanilla.render.glue.Sprite
import org.anti_ad.mc.common.vanilla.render.glue.rDrawCenteredSprite
import org.anti_ad.mc.common.vanilla.render.rDisableDepth
import org.anti_ad.mc.common.vanilla.render.rEnableDepth
import org.anti_ad.mc.common.vanilla.showSubTitle
import org.anti_ad.mc.ipnext.config.AutoRefillNbtMatchType
import org.anti_ad.mc.ipnext.config.AutoRefillSettings
import org.anti_ad.mc.ipnext.config.ThresholdUnit.ABSOLUTE
import org.anti_ad.mc.ipnext.config.ThresholdUnit.PERCENTAGE
import org.anti_ad.mc.ipnext.config.ToolReplaceVisualNotification
import org.anti_ad.mc.ipnext.ingame.`(containerBounds)`
import org.anti_ad.mc.ipnext.ingame.`(equipmentSlot)`
import org.anti_ad.mc.ipnext.ingame.`(id)`
import org.anti_ad.mc.ipnext.ingame.`(invSlot)`
import org.anti_ad.mc.ipnext.ingame.`(inventoryOrNull)`
import org.anti_ad.mc.ipnext.ingame.`(isPressed)`
import org.anti_ad.mc.ipnext.ingame.`(itemStack)`
import org.anti_ad.mc.ipnext.ingame.`(keyDrop)`
import org.anti_ad.mc.ipnext.ingame.`(options)`
import org.anti_ad.mc.ipnext.ingame.`(scaledHeight)`
import org.anti_ad.mc.ipnext.ingame.`(scaledWidth)`
import org.anti_ad.mc.ipnext.ingame.`(slots)`
import org.anti_ad.mc.ipnext.ingame.`(topLeft)`
import org.anti_ad.mc.ipnext.ingame.`(window)`
import org.anti_ad.mc.ipnext.ingame.vCursorStack
import org.anti_ad.mc.ipnext.ingame.vMainhandIndex
import org.anti_ad.mc.ipnext.ingame.vPlayerSlotOf
import org.anti_ad.mc.ipnext.inventory.AreaTypes
import org.anti_ad.mc.ipnext.inventory.ContainerClicker
import org.anti_ad.mc.ipnext.inventory.GeneralInventoryActions
import org.anti_ad.mc.ipnext.item.`(enchantments)`
import org.anti_ad.mc.ipnext.item.`(foodComponent)`
import org.anti_ad.mc.ipnext.item.`(isFood)`
import org.anti_ad.mc.ipnext.item.`(isHarmful)`
import org.anti_ad.mc.ipnext.item.`(saturationModifier)`
import org.anti_ad.mc.ipnext.item.EMPTY
import org.anti_ad.mc.ipnext.item.ItemStack
import org.anti_ad.mc.ipnext.item.ItemType
import org.anti_ad.mc.ipnext.item.comparablePotionEffects
import org.anti_ad.mc.ipnext.item.customName
import org.anti_ad.mc.ipnext.item.customOrTranslatedName
import org.anti_ad.mc.ipnext.item.durability
import org.anti_ad.mc.ipnext.item.hasCustomName
import org.anti_ad.mc.ipnext.item.hasPotionEffects
import org.anti_ad.mc.ipnext.item.isBucket
import org.anti_ad.mc.ipnext.item.isDamageable
import org.anti_ad.mc.ipnext.item.isEmpty
import org.anti_ad.mc.ipnext.item.isEmptyBucket
import org.anti_ad.mc.ipnext.item.isEmptyComparedTo
import org.anti_ad.mc.ipnext.item.isFullBucket
import org.anti_ad.mc.ipnext.item.isFullComparedTo
import org.anti_ad.mc.ipnext.item.isHoneyBottle
import org.anti_ad.mc.ipnext.item.isStackable
import org.anti_ad.mc.ipnext.item.isStew
import org.anti_ad.mc.ipnext.item.itemId
import org.anti_ad.mc.ipnext.item.maxDamage
import org.anti_ad.mc.ipnext.item.rule.file.RuleFileRegister
import org.anti_ad.mc.ipnext.item.rule.natives.compareByMatch
import org.anti_ad.mc.ipnext.item.rule.parameter.Match
import org.anti_ad.mc.ipnext.specific.event.PLockSlotHandler
import org.anti_ad.mc.ipnext.specific.event.PLockSlotHandler.Companion.TEXTURE

object AutoRefillHandler: PLockSlotHandler {

    data object WatchIds {

        const val MAIN_HAND_OFFSET = 36
        val mainHandSelected: () -> Int = { MAIN_HAND_OFFSET + vMainhandIndex() }
        val offHand: () -> Int = { 45 }
        val head: () -> Int = { 5 }
        val chest: () -> Int = { 6 }
        val legs: () -> Int = { 7 }
        val feet: () -> Int = { 8 }
    }

    private val allIds: List<Int>
        get() {
            return run {
                val screen = Vanilla.screen()
                Vanilla.container().`(slots)`.filter { slot ->
                    val playerSlot = vPlayerSlotOf(slot, screen)
                    val inv = playerSlot.`(inventoryOrNull)`
                    inv != null && inv is PlayerInventory && slot.`(invSlot)` in 0 .. 8
                }.map { it.`(id)` }
            }
        }

    val foregroundSprite: Sprite
        get() = Sprite(TEXTURE, Rectangle(8, 8, 32, 32)).right(0).down(2)

    val disabledSlots: MutableList<Int> = mutableListOf()

    private inline val pressingDropKey: Boolean
        get() = Vanilla.mc().`(options)`.`(keyDrop)`.`(isPressed)`

    var screenOpening = false

    val profilesSwappedItems = mutableSetOf<Int>()

    var skipTick = false

    private val blacklist: MutableSet<String> = mutableSetOf()

    private fun ItemStack.isBlackListed(): Boolean = this.itemType.itemId in blacklist

    private var ticksAfterUp = 16

    private val tempDisabledForDamageable: Boolean
        get() {
            return ticksAfterUp < 15
        }

    fun onTickInGame() {
        if (!skipTick) {
            if (AutoRefillSettings.AUTO_REFILL_TEMP_DISABLE_REFILL_FOR_TOOLS.isPressing()) {
                ticksAfterUp = 0
            } else if (ticksAfterUp < 16) {
                ticksAfterUp++
            }
            if (Vanilla.screen() != null || (AutoRefillSettings.DISABLE_FOR_DROP_ITEM.booleanValue && pressingDropKey)) {
                screenOpening = true
            } else if (VanillaUtil.inGame()) { //  Vanilla.screen() == null
                if (screenOpening) {
                    screenOpening = false
                    init() // close screen -> init
                }
                handleAutoRefill()
            }
        } else {
            skipTick = false
            return
        }
    }

    fun onJoinWorld() {
        init()
    }

    private fun refillAllowedOrNull(slot: () -> Int): (() -> Int)? {
        return if (!disabledSlots.contains(slot())) {
            slot
        } else {
            null
        }
    }

    fun init() {
        monitors.clear()
        val list = mutableListOf<ItemSlotMonitor>()

        var swapers = 0
        refillAllowedOrNull(WatchIds.mainHandSelected)?.let {
            list.add(ItemSlotMonitor(it))
            swapers++
        }
        refillAllowedOrNull(WatchIds.offHand)?.let {
            list.add(ItemSlotMonitor(it))
            swapers++
        }

        if (AutoRefillSettings.REFILL_ARMOR.booleanValue) {
            list += listOf(
                ItemSlotMonitor(WatchIds.head), ItemSlotMonitor(WatchIds.chest), ItemSlotMonitor(WatchIds.legs), ItemSlotMonitor(WatchIds.feet)
                          )
        }
        if (swapers > 0) {
            list[0].anothers += list[1]
            list[0].anothers += list.drop(swapers)
        }
        if (swapers > 1) {
            list[1].anothers += list[0]
            list[1].anothers += list.drop(swapers)
        }
        monitors.addAll(list)
    }

    val monitors = mutableListOf<ItemSlotMonitor>()

    // fixed ~.~ [later fun change reminder: see if auto refill fail if item ran out then instantly pick up some items]
    fun handleAutoRefill() { //Log.trace("in handleAutoRefill")
        tryCatch { // just in case (index out of range etc)
            monitors.forEach { it.updateCurrent() }
            monitors.forEach { it.checkShouldHandle() }
            monitors.forEach { it.checkHandle() }
        }
    }

    class ItemSlotMonitor(val slotId: () -> Int) { constructor(slotId: Int): this({ slotId })

        val anothers = mutableListOf<ItemSlotMonitor>() // item may swap with another slot

        var storedItem = ItemStack.EMPTY
        var storedSlotId = -1
        var tickCount = 0

        var lastTickItem = ItemStack.EMPTY
        var currentItem = ItemStack.EMPTY
        var currentSlotId = -1

        private var lastNotifyDurability: Int = -1
        private var lastNotifyBreakDurability: Int = -1

        fun updateCurrent() {
            lastTickItem = currentItem
            currentSlotId = slotId()
            currentItem = Vanilla.playerContainer().`(slots)`[currentSlotId].`(itemStack)`
        }

        var shouldHandle = false

        fun checkShouldHandle() {
            shouldHandle = currentSlotId == storedSlotId && !isSwapped() && shouldHandleItem()
        }

        fun checkHandle() {
            if (shouldHandle) {
                if (tickCount >= AutoRefillSettings.AUTO_REFILL_WAIT_TICK.integerValue) { // do handle
                    handle()
                    updateCurrent()
                    unhandled() // update storedItem
                    LockedSlotKeeper.init()
                } else { // wait and return
                    tickCount++
                    return
                }
            } else {
                unhandled()
            }
        }

        // ============
        // inner
        // ============
        private fun isSwapped(): Boolean { // check this current == other lastTick and other current == this lastTick
            if (currentItem == lastTickItem) return false
            return anothers.any { another ->
                this.currentItem == another.lastTickItem && this.lastTickItem == another.currentItem
            }
        }

        private fun unhandled() {
            storedItem = currentItem
            storedSlotId = currentSlotId
            tickCount = 0
            lastNotifyDurability = storedItem.itemType.durability
            lastNotifyBreakDurability = storedItem.itemType.durability
        }

        private fun handle() { // find same type with stored item in backpack
            GeneralInventoryActions.cleanCursor()
            val itemType = checkingItem.itemType
            val foundSlotId = findCorrespondingSlot(checkingItem, currentItem)
            if (foundSlotId != null) {

                if (itemType.isDamageable) notifySuccessfulChange(itemType, foundSlotId)

                if (currentItem.itemType.isStackable) {
                    ContainerClicker.shiftClick(storedSlotId)
                }

                if ((storedSlotId - WatchIds.MAIN_HAND_OFFSET) in 0 .. 8) { // use swap
                    //handles hotbar
                    ContainerClicker.swap(
                        foundSlotId, storedSlotId - WatchIds.MAIN_HAND_OFFSET
                                         )
                } else { //handles offhand and armor slots
                    ContainerClicker.leftClick(foundSlotId)
                    ContainerClicker.leftClick(storedSlotId)
                    if (!vCursorStack().isEmpty()) {
                        ContainerClicker.leftClick(foundSlotId) // put back
                    }
                }

            } else if (itemType.isDamageable) {
                notifyFailToChange()
            }
        }

        var checkingItem = storedItem // use to select

        private fun shouldHandleItem(): Boolean {

            if (profilesSwappedItems.contains(slotId())) {
                profilesSwappedItems.remove(slotId())
                return false
            }

            checkingItem = storedItem
            if (storedItem.isBlackListed()) return false
            if (storedItem.isEmpty()) return false // nothing become anything
            if (currentItem.isEmpty()) {
                return !(AutoRefillSettings.DISABLE_FOR_LOYALTY_ITEMS.value && storedItem.itemType.`(enchantments)`[Enchantments.LOYALTY] != null)
            }
            val itemType = currentItem.itemType
            if (itemType.isDamageable) {
                if (AutoRefillSettings.REFILL_BEFORE_TOOL_BREAK.booleanValue && !tempDisabledForDamageable) {
                    if (!(AutoRefillSettings.ALLOW_BREAK_FOR_NON_ENCHANTED.value && itemType.`(enchantments)`.isEmpty() && itemType.maxDamage < AutoRefillSettings.TOOL_MAX_DURABILITY_THRESHOLD.value)) {
                        val threshold = getThreshold(itemType)

                        notifyDurabilityChange(itemType, itemType.durability, threshold)
                        if (itemType.durability <= threshold) return true.also { checkingItem = currentItem }
                    }
                }
            }

            return if (storedItem.itemType.isFullBucket && currentItem.itemType.item == Items.BUCKET) {
                true //this case is not handled by isEmptyComparedTo
            } else if (storedItem.itemType.isBucket && storedItem.itemType.isEmptyBucket && currentItem.itemType.isFullBucket) {
                true
            } else if (storedItem.itemType.isBucket && storedItem.itemType.isEmptyComparedTo(currentItem.itemType)) {
                true
            } else if (storedItem.itemType.isBucket && storedItem.itemType.isFullComparedTo(currentItem.itemType)) {
                true
            } else if (storedItem.itemType.item == Items.POTION && currentItem.itemType.item == Items.GLASS_BOTTLE) {
                true
            } else if (storedItem.itemType.isHoneyBottle && currentItem.itemType.item == Items.GLASS_BOTTLE) {
                true
            } else if (storedItem.itemType.isStew && currentItem.itemType.item == Items.BOWL) {
                true
            } else currentItem.itemType.isStackable && currentItem.count <= AutoRefillSettings.STACKABLE_THRESHOLD.integerValue

        }

        private fun notifySuccessfulChange(
            itemType: ItemType, foundSlotId: Int
                                          ) {
            if (AutoRefillSettings.VISUAL_REPLACE_SUCCESS_NOTIFICATION.value) {
                val replacingWith = Vanilla.playerContainer().`(slots)`[foundSlotId].`(itemStack)`.itemType

                val message: (Boolean) -> String = {
                    val newl = if (it) {
                        """{"text": "\n"},"""
                    } else {
                        """{"text": " - ", "color": "#FFFFFF"},"""
                    }
                    """[
                           {"translate" : "inventoryprofiles.config.notification.tool_replace_ping.ipn", "color" : "#3584E4" },
                           $newl
                           {"text" : "\"${itemType.customOrTranslatedName}\" ", "color": "#FF8484"},
                           {"translate" : "inventoryprofiles.config.notification.tool_replace_success", "color": "#FFFFFF"},
                           {"text" : " \"${replacingWith.customOrTranslatedName}\"", "color": "#8484FF"}
                    ]"""
                }
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") when (AutoRefillSettings.TYPE_VISUAL_REPLACE_SUCCESS_NOTIFICATION.value) {
                    ToolReplaceVisualNotification.SUBTITLE -> {
                        showSubTitle(fromSerializedJson(message(false)))
                    }

                    ToolReplaceVisualNotification.HOTBAR   -> Vanilla.inGameHud().setOverlayMessage(
                        fromSerializedJson(message(false)), false
                                                                                                   )

                    ToolReplaceVisualNotification.CHAT     -> VanillaUtil.chat(fromSerializedJson(message(true))!!)
                }
            }
            if (AutoRefillSettings.AUDIO_REPLACE_SUCCESS_NOTIFICATION.value) {
                Sounds.REFILL_STEP_NOTIFY.play(.2f)
                Sounds.REFILL_STEP_NOTIFY.play(1.5f, 5)
            }
        }

        private fun notifyFailToChange() {
            if (!currentItem.isEmpty() && (AutoRefillSettings.VISUAL_REPLACE_FAILED_NOTIFICATION.value || AutoRefillSettings.AUDIO_REPLACE_FAILED_NOTIFICATION.value)) {
                val itemType = currentItem.itemType
                val threshold = getThreshold(itemType)
                val durability = itemType.durability
                if (durability <= threshold) {
                    if (durability != lastNotifyBreakDurability) {
                        if (AutoRefillSettings.VISUAL_REPLACE_FAILED_NOTIFICATION.value) {
                            val message: (Boolean) -> String = {
                                val newl = if (it) {
                                    """{"text": "\n"},"""
                                } else {
                                    """{"text": " - ", "color": "#FFFFFF"},"""
                                }
                                """[
                                   {"translate" : "inventoryprofiles.config.notification.tool_replace_ping.ipn", "color" : "#3584E4" },
                                   {"translate": "inventoryprofiles.config.notification.tool_replace_ping.warning", "color" : "#FF8484"},
                                   $newl
                                   {"translate": "inventoryprofiles.config.notification.tool_replace_failed.replacing", "color" : "#E5A50A", "with": ["${itemType.customOrTranslatedName}"]}
                                   ]"""
                            }
                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") when (AutoRefillSettings.TYPE_VISUAL_REPLACE_FAILED_NOTIFICATION.value) {
                                ToolReplaceVisualNotification.SUBTITLE -> {
                                    showSubTitle(fromSerializedJson(message(false)))
                                }

                                ToolReplaceVisualNotification.HOTBAR   -> Vanilla.inGameHud().setOverlayMessage(
                                    fromSerializedJson(message(false)), false
                                                                                                               )

                                ToolReplaceVisualNotification.CHAT     -> VanillaUtil.chat(fromSerializedJson(message(true))!!)
                            }

                        }
                        if (AutoRefillSettings.AUDIO_REPLACE_FAILED_NOTIFICATION.value) {
                            Sounds.REFILL_STEP_NOTIFY.play(1.2f)
                            Sounds.REFILL_STEP_NOTIFY.play(1.5f, 5)
                        }
                    }
                }
            }
        }

        private fun notifyDurabilityChange(
            itemType: ItemType, durability: Int, threshold: Int
                                          ) {
            if ((AutoRefillSettings.VISUAL_DURABILITY_NOTIFICATION.value || AutoRefillSettings.AUDIO_DURABILITY_NOTIFICATION.value) && isItNotifyStep(
                    durability,
                    threshold
                                                                                                                                                     )
            ) {

                if (AutoRefillSettings.VISUAL_DURABILITY_NOTIFICATION.value) {
                    val message: (Boolean) -> String =
                        { //                        {"translate": "inventoryprofiles.config.notification.tool_replace_ping.warning", "color" : "#FF8484"},
                            val newl = if (it) {
                                """{"text": "\n"},"""
                            } else {
                                """{"text": " - ", "color": "#FFFFFF"},"""
                            }
                            """[
                        {"text" : ""},
                        {"translate" : "inventoryprofiles.config.notification.tool_replace_ping.ipn", "color" : "#3584E4" },
                        $newl
                        {"translate": "inventoryprofiles.config.notification.tool_replace_ping.durability", "color" : "#E5A50A", "with": ["${itemType.customOrTranslatedName}","$durability"]},
                        {"translate": "inventoryprofiles.config.notification.tool_replace_ping.replacing", "color" : "#FF4545", "with": ["$threshold"]}
                        ]"""
                        }
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") when (AutoRefillSettings.TYPE_VISUAL_DURABILITY_NOTIFICATION.value) {
                        ToolReplaceVisualNotification.SUBTITLE -> {
                            showSubTitle(fromSerializedJson(message(false)))
                        }

                        ToolReplaceVisualNotification.HOTBAR   -> Vanilla.inGameHud().setOverlayMessage(fromSerializedJson(message(false)), false)
                        ToolReplaceVisualNotification.CHAT     -> VanillaUtil.chat(fromSerializedJson(message(true))!!)
                    }

                }
                if (AutoRefillSettings.AUDIO_DURABILITY_NOTIFICATION.value) {
                    Sounds.REFILL_STEP_NOTIFY.play()
                }
            }
        }

        private fun isItNotifyStep(
            durability: Int, threshold: Int
                                  ): Boolean {
            if (storedItem.itemType != lastTickItem.itemType) {
                lastNotifyDurability = -1
            }
            if (lastNotifyDurability != durability && durability != threshold) {
                val num = AutoRefillSettings.NUMBER_OF_NOTIFICATIONS.value
                val step = AutoRefillSettings.NOTIFICATION_STEP.value
                for (i in threshold .. threshold + num * step step step) { //            Log.trace("Checking if ")
                    if (durability == i) {
                        lastNotifyDurability = i
                        return true
                    }
                }
            }
            return false
        }

        companion object {

            private fun findCorrespondingSlot(checkingItem: ItemStack, currentItem: ItemStack): Int? { // for stored item
                var filtered = Vanilla.playerContainer().let { playerContainer ->
                    val slots = playerContainer.`(slots)`
                    with(AreaTypes) {
                        if (AutoRefillSettings.IGNORE_LOCKED_SLOTS.value) {
                            playerStorage
                        } else {
                            playerStorage - lockedSlots
                        }
                    }.getItemArea(
                        playerContainer, slots
                                 ).slotIndices.map {
                        IndexedValue(
                            it - 9, slots[it].`(itemStack)`
                                    )
                    }
                }.asSequence()
                val itemType = checkingItem.itemType
                if (itemType.isDamageable) {
                    val threshold = if (AutoRefillSettings.REFILL_BEFORE_TOOL_BREAK.booleanValue) {
                        getThreshold(itemType)
                    } else {
                        -1
                    }
                    filtered = filtered.filter { it.value.itemType.run { isDamageable && durability > threshold } }
                    when (itemType.item) {
                        is ArmorItem      -> {
                            filtered = filtered.filter {
                                val otherType = it.value.itemType
                                otherType.item is ArmorItem && otherType.item.`(equipmentSlot)` == itemType.item.`(equipmentSlot)`
                            }
                        }

                        is SwordItem      -> {
                            filtered = filtered.filter { it.value.itemType.item is SwordItem }
                        }

                        is ShovelItem     -> {
                            filtered = filtered.filter { it.value.itemType.item is ShovelItem }
                        }

                        is PickaxeItem    -> {
                            filtered = filtered.filter { it.value.itemType.item is PickaxeItem }
                        }

                        is AxeItem        -> {
                            filtered = filtered.filter { it.value.itemType.item is AxeItem }
                        }

                        is HoeItem        -> {
                            filtered = filtered.filter { it.value.itemType.item is HoeItem }
                        }

                        is ToolItem       -> {
                            filtered = filtered.filter { it.value.itemType.item is ToolItem }
                        }

                        is FishingRodItem -> {
                            filtered = filtered.filter { it.value.itemType.item is FishingRodItem }
                        }

                        else              -> {
                            filtered = defaultItemMatch(filtered, itemType)
                        }
                    } // find best tool match criteria
                } else if (checkingItem.itemType.hasPotionEffects) { // find best potion match
                    val effectStr = checkingItem.itemType.comparablePotionEffects.map { it.effect }
                    filtered = filtered.filter {
                        it.value.itemType.comparablePotionEffects.map { it.effect }.containsAll(effectStr)
                    }
                } else { // find item
                    filtered = defaultItemMatch(filtered, itemType)
                    if (checkingItem.itemType.isStackable && checkingItem.itemType.item == currentItem.itemType.item) {
                        filtered = filtered.filter {
                            it.value.count > currentItem.count
                        }
                    }
                }
                filtered = filtered.sortedWith(Comparator<IndexedValue<ItemStack>> { a, b ->
                    val aType = a.value.itemType
                    val bType = b.value.itemType
                    compareByMatch(
                        aType, bType, { it.item == itemType.item }, Match.FIRST
                                  ) // type match sort
                }.thenComparator { a, b ->
                    val aType = a.value.itemType
                    val bType = b.value.itemType
                    bType.maxDamage - aType.maxDamage // material sort
                }.thenComparator { a, b ->
                    val aType = a.value.itemType
                    val bType = b.value.itemType
                    if (aType.`(isFood)` && bType.`(isFood)`) {
                        when {
                            bType.`(foodComponent)`.`(saturationModifier)` == aType.`(foodComponent)`.`(saturationModifier)` -> {
                                0
                            }

                            bType.`(foodComponent)`.`(saturationModifier)` > aType.`(foodComponent)`.`(saturationModifier)`  -> {
                                1
                            }

                            else                                                                                             -> {
                                -1
                            }
                        }
                    } else {
                        0
                    }
                }.thenComparator { a, b ->
                    val aType = a.value.itemType
                    val bType = b.value.itemType
                    RuleFileRegister.getCustomRuleOrEmpty("auto_refill_best").compare(
                        aType, bType
                                                                                     )
                }.thenComparator { a, b ->
                    if (AutoRefillSettings.AUTO_REFILL_PREFER_SMALLER_STACKS.booleanValue) {
                        a.value.count - b.value.count
                    } else {
                        b.value.count - a.value.count
                    }
                })
                val index = filtered.firstOrNull()?.index ?: -1 // test // todo better coding
                return index.takeIf { it >= 0 }?.plus(9)
            }

            private fun defaultItemMatch(
                filtered: Sequence<IndexedValue<ItemStack>>, itemType: ItemType
                                        ) = when {
                filtered.firstOrNull {
                    typeItemMatch(
                        it, itemType
                                 )
                } != null -> {
                    filtered.filter {
                        typeItemMatch(
                            it, itemType
                                     )
                    }
                }

                filtered.firstOrNull {
                    val other = it.value.itemType
                    val allowHarmful = AutoRefillSettings.AUTO_REFILL_MATCH_HARMFUL_FOOD.booleanValue
                    val machAnyFood = AutoRefillSettings.AUTO_REFILL_MATCH_ANY_FOOD.booleanValue

                    machAnyFood && (itemType.`(isFood)` && other.`(isFood)`) && (!other.`(foodComponent)`.`(isHarmful)` || allowHarmful)
                } != null -> {

                    filtered.filter {
                        val other = it.value.itemType
                        val allowHarmful = AutoRefillSettings.AUTO_REFILL_MATCH_HARMFUL_FOOD.booleanValue
                        (itemType.`(isFood)` && other.`(isFood)`) && (!other.`(foodComponent)`.`(isHarmful)` || allowHarmful)
                    }
                }

                else      -> emptySequence()

            }

            private fun typeItemMatch(
                it: IndexedValue<ItemStack>, itemType: ItemType
                                     ) =
                if ((itemType.hasCustomName || it.value.itemType.hasCustomName) && AutoRefillSettings.AUTO_REFILL_MATCH_CUSTOM_NAME.booleanValue) {
                    it.value.itemType.item == itemType.item && it.value.itemType.customName == itemType.customName && checkNBTIfNeeded(it, itemType)
                } else {
                    it.value.itemType.item == itemType.item && checkNBTIfNeeded(it, itemType)
                }

            private fun checkNBTIfNeeded(
                it: IndexedValue<ItemStack>, itemType: ItemType
                                        ) = if (AutoRefillSettings.AUTO_REFILL_MATCH_NBT.booleanValue) {
                if (!itemType.isBucket || (itemType.isBucket && !AutoRefillSettings.AUTO_REFILL_IGNORE_NBT_FOR_BUCKETS.booleanValue)) {

                    when (AutoRefillSettings.AUTO_REFILL_MATCH_NBT_TYPE.value) {
                        AutoRefillNbtMatchType.CAN_HAVE_EXTRA -> {

                            val tagsIn = itemType.changes
                            val tagsOut = it.value.itemType.changes
                            var res = tagsIn.isEmpty && tagsOut.isEmpty
                            run earlyFinish@{
                                if (!tagsIn.isEmpty && !tagsOut.isEmpty) {
                                    res = true
                                    tagsIn.entrySet().forEach { (type, value) ->
                                        if (value != tagsOut[type]) {
                                            res = false
                                            return@earlyFinish
                                        }
                                    }
                                }
                            }
                            res
                        }

                        AutoRefillNbtMatchType.EXACT          -> {
                            val eq = it.value.itemType.changes == itemType.changes
                            eq
                        }

                    }
                } else {
                    true
                }
            } else {
                true
            }

            private fun getThreshold(itemType: ItemType): Int {
                if (!itemType.isDamageable) return 0
                return when (AutoRefillSettings.THRESHOLD_UNIT.value) {
                    ABSOLUTE   -> AutoRefillSettings.TOOL_DAMAGE_THRESHOLD.integerValue
                    PERCENTAGE -> AutoRefillSettings.TOOL_DAMAGE_THRESHOLD.integerValue * itemType.maxDamage / 100
                }.coerceAtLeast(0)
            }
        }
    }

    fun blackListChanged() {
        blacklist.clear()
        AutoRefillSettings.AUTOREFILL_BLACKLIST.value.split(",").forEach {
            blacklist.add(it.trim())
        }
    }

    override val enabled: Boolean
        get() = AutoRefillSettings.DRAW_OVERLAY_FOR_ENABLED_SLOTS.value

    override val slotLocations: Map<Int, Point>
        get() {
            val screen = Vanilla.screen() as? ContainerScreen<*> ?: return mapOf()

            @Suppress("USELESS_ELVIS")
            val container = Vanilla.container() ?: return mapOf()
            return container.`(slots)`.mapNotNull { slot ->
                val playerSlot = vPlayerSlotOf(
                    slot, screen
                                              )
                if (playerSlot.id in allIds) {
                    val topLeft = slot.`(topLeft)`
                    val inv = playerSlot.`(inventoryOrNull)` ?: return@mapNotNull null
                    return@mapNotNull if (inv is PlayerInventory) playerSlot.`(invSlot)` to topLeft else null
                }
                return@mapNotNull null
            }.toMap()
        }

    override fun drawForeground(context: NativeContext) {
        val screen = Vanilla.screen() as? ContainerScreen<*> ?: return
        val topLeft = screen.`(containerBounds)`.topLeft
        for ((invSlot, slotTopLeft) in slotLocations) {
            if (invSlot !in disabledSlots) {
                val center = topLeft + slotTopLeft + eightByEight
                rDrawCenteredSprite(context, foregroundSprite, center)
            }
        }
    }

    override fun drawConfig(context: NativeContext) {

    }

    fun onBackgroundRender(context: NativeContext) {

    }

    private fun drawHotSprite(context: NativeContext) {
        if (!enabled) return //    rClearDepth() // use translate or zOffset
        rDisableDepth() //RenderSystem.enableBlend()
        val screenWidth = Vanilla.mc().`(window)`.`(scaledWidth)`
        val screenHeight = Vanilla.mc().`(window)`.`(scaledHeight)`
        val i = screenWidth / 2
        for (j1 in 0 .. 8) {
            if ((j1 + 36) !in disabledSlots) {

                val k1: Int = i - 90 + j1 * 20 + 2
                val l1: Int = screenHeight - 16 - 3
                val topLeft = Point(k1, l1)
                val topLeftCentered = topLeft + eightByEight //if (LockedSlotsSettings.SHOW_LOCKED_SLOTS_FOREGROUND.booleanValue) {
                rDrawCenteredSprite(context, foregroundSprite, topLeftCentered) //}
            }
        } //RenderSystem.disableBlend()
        rEnableDepth()
    }

    fun postRenderHud(context: NativeContext) { //if (AutoRefillSettings.ALSO_SHOW_LOCKED_SLOTS_IN_HOTBAR.value && GameType.SPECTATOR != Vanilla.gameMode()) {
        drawHotSprite(context) //}
    }

    fun preRenderHud(context: NativeContext) {
    }

}
