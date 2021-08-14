package com.anatawa12.fixRtm.rtm.rail.util

import jp.ngt.rtm.rail.util.*
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

class SwitchTypes {
    object SwitchSingleCross {
        fun makeRailMap(switches: List<RailPosition>, normals: List<RailPosition>): Array<RailMapSwitch> {
            tryMakeRailMapByDirection(switches, normals)?.let { return it }
            tryMakeRailMapByAngle(switches, normals)?.let { return it }
            return fallbackMakeRailMap(switches, normals)
        }

        private fun tryMakeRailMapByDirection(switches: List<RailPosition>, normals: List<RailPosition>): Array<RailMapSwitch>? {
            // candidates of normal marker for straight rail
            val candidates = switches.associateWith { switch -> normals.filter { it.direction != switch.direction } }
            // if some switch has no candidate
            if (candidates.values.any(List<*>::isEmpty)) return null

            val (switch0, normal0) = candidates.entries
                .firstNotNullOfOrNull { (key, value) -> value.singleOrNull()?.let { key to it } }
                ?: return null

            val (switch1Index, switch1) = switches.withIndex().single { it.value !== switch0 }
            val normal1 = normals.single { it !== normal0 }

            return makeRailMapArray(
                switchIn0 = switch0,
                normalIn0 = normal0,
                switchIn1 = switch1,
                normalIn1 = normal1,
                swap = switch1Index == 0,
            )
        }

        private fun tryMakeRailMapByAngle(switches: List<RailPosition>, normals: List<RailPosition>): Array<RailMapSwitch>? {
            // candidates
            for (switch0 in switches) {
                val (normal0, normal1) = switch0.findStraighter(normals[0], normals[1]) ?: continue
                val (switch1Index, switch1) = switches.withIndex().single { it.value !== switch0 }

                return makeRailMapArray(
                    switchIn0 = switch0,
                    normalIn0 = normal0,
                    switchIn1 = switch1,
                    normalIn1 = normal1,
                    swap = switch1Index == 0,
                )
            }

            return null
        }

        private fun fallbackMakeRailMap(switches: List<RailPosition>, normals: List<RailPosition>): Array<RailMapSwitch> {
            return makeRailMapArray(
                switchIn0 = switches[0],
                normalIn0 = normals[0],
                switchIn1 = switches[1],
                normalIn1 = normals[1],
                swap = false,
            )
        }

        private fun makeRailMapArray(
            switchIn0: RailPosition,
            normalIn0: RailPosition,
            switchIn1: RailPosition,
            normalIn1: RailPosition,
            swap: Boolean,
        ): Array<RailMapSwitch> {
            val switch0: RailPosition
            val normal0: RailPosition
            val switch1: RailPosition
            val normal1: RailPosition

            if (swap) {
                switch0 = switchIn1
                normal0 = normalIn1
                switch1 = switchIn0
                normal1 = normalIn0
            } else {
                switch0 = switchIn0
                normal0 = normalIn0
                switch1 = switchIn1
                normal1 = normalIn1
            }

            val dir0 = switch0.getDir(switch1, normal0)
            val dir1 = switch1.getDir(switch0, normal1)

            return arrayOf(
                RailMapSwitch(switch0, normal0, dir0.invert(), RailDir.NONE),
                RailMapSwitch(switch1, normal1, dir1.invert(), RailDir.NONE),
                RailMapSwitch(switch0, normal1, dir0, dir1),
            )
        }

        private fun RailPosition.findStraighter(normal0: RailPosition, normal1: RailPosition): Pair<RailPosition, RailPosition>? {
            val angle0 = calcAngleMade(normal0)
            val angle1 = calcAngleMade(normal1)

            return when {
                abs(angle0 - angle1) < 0.0000000001 -> null
                angle0 < angle1 -> normal0 to normal1
                else -> normal1 to normal0
            }
        }

        // zero to PI
        private fun RailPosition.calcAngleMade(o: RailPosition): Double {
            return acos(this.dotProduct(o) /
                    (sqrt(this.dotProduct(this)) * sqrt(o.dotProduct(o))))
        }

        private infix fun RailPosition.dotProduct(o: RailPosition) =
            posX * o.posX + posY * o.posY + posZ * o.posZ
    }
}
