/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2023 Plamen K. Kosseff <p.kosseff@gmail.com>
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

package org.anti_ad.mc.ipnext.compat.integrations

import org.anti_ad.mc.common.extensions.trySwallow
import org.anti_ad.mc.common.vanilla.alias.Slot

object Integrations {

    var ___getCarpetEmptyShulkersStackSize: () -> Int = { 1 }

    val carpetEmptyShulkersStackSize: Int
        get() {
            return ___getCarpetEmptyShulkersStackSize();
        }


    var ___getSophisticatedMaxCount: (Slot) -> Int = { -1 }


    fun init() {
        trySwallow {
            CarpetIntegration().init()
        }
    }

    fun sophisticatedMaxCount(sourceSlot: Slot): Int = ___getSophisticatedMaxCount(sourceSlot)
}