/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

// AIS ship types are an external standards table: inline numeric codes and compact public entries are intentional.
@file:Suppress("MagicNumber", "UndocumentedPublicClass", "UndocumentedPublicProperty")

package it.unibo.alchemist.boundary.gps.loaders.ais

/**
 * AIS ship and cargo type codes.
 *
 * The values follow the VT Explorer AIS ship type reference.
 *
 * @property code raw AIS ship type code.
 * @property description human-readable AIS ship type description.
 */
sealed interface AISShipType {
    val code: Int
    val description: String

    data object NotAvailable : AISShipType {
        override val code = 0
        override val description = "Not available (default)"
    }

    data class ReservedForFutureUse(override val code: Int) : AISShipType {
        init {
            require(code in 1..19) { "Reserved AIS ship type code must be in 1..19" }
        }

        override val description = "Reserved for future use"
    }

    sealed interface WingInGround : AISShipType

    data object AnyWingInGround : WingInGround {
        override val code = 20
        override val description = "Wing in ground (WIG), all ships of this type"
    }

    sealed class HazardousWingInGround(private val category: String, final override val code: Int) : WingInGround {
        final override val description = "Wing in ground (WIG), Hazardous category $category"
    }

    data object HazardousWingInGroundA : HazardousWingInGround("A", 21)

    data object HazardousWingInGroundB : HazardousWingInGround("B", 22)

    data object HazardousWingInGroundC : HazardousWingInGround("C", 23)

    data object HazardousWingInGroundD : HazardousWingInGround("D", 24)

    data class WingInGroundReservedForFutureUse(override val code: Int) : WingInGround {
        init {
            require(code in 25..29) { "Reserved WIG AIS ship type code must be in 25..29" }
        }

        override val description = "Wing in ground (WIG), Reserved for future use"
    }

    data object Fishing : AISShipType {
        override val code = 30
        override val description = "Fishing"
    }

    data object Towing : AISShipType {
        override val code = 31
        override val description = "Towing"
    }

    data object TowingLarge : AISShipType {
        override val code = 32
        override val description = "Towing: length exceeds 200m or breadth exceeds 25m"
    }

    data object DredgingOrUnderwaterOps : AISShipType {
        override val code = 33
        override val description = "Dredging or underwater ops"
    }

    data object DivingOps : AISShipType {
        override val code = 34
        override val description = "Diving ops"
    }

    data object MilitaryOps : AISShipType {
        override val code = 35
        override val description = "Military ops"
    }

    data object Sailing : AISShipType {
        override val code = 36
        override val description = "Sailing"
    }

    data object PleasureCraft : AISShipType {
        override val code = 37
        override val description = "Pleasure Craft"
    }

    data class Reserved(override val code: Int) : AISShipType {
        init {
            require(code in 38..39) { "Reserved AIS ship type code must be in 38..39" }
        }

        override val description = "Reserved"
    }

    sealed interface HighSpeedCraft : AISShipType

    data object AnyHighSpeedCraft : HighSpeedCraft {
        override val code = 40
        override val description = "High speed craft (HSC), all ships of this type"
    }

    sealed class HazardousHighSpeedCraft(private val category: String, final override val code: Int) : HighSpeedCraft {
        final override val description = "High speed craft (HSC), Hazardous category $category"
    }

    data object HazardousHighSpeedCraftA : HazardousHighSpeedCraft("A", 41)

    data object HazardousHighSpeedCraftB : HazardousHighSpeedCraft("B", 42)

    data object HazardousHighSpeedCraftC : HazardousHighSpeedCraft("C", 43)

    data object HazardousHighSpeedCraftD : HazardousHighSpeedCraft("D", 44)

    data class HighSpeedCraftReservedForFutureUse(override val code: Int) : HighSpeedCraft {
        init {
            require(code in 45..48) { "Reserved HSC AIS ship type code must be in 45..48" }
        }

        override val description = "High speed craft (HSC), Reserved for future use"
    }

    data object HighSpeedCraftNoAdditionalInformation : HighSpeedCraft {
        override val code = 49
        override val description = "High speed craft (HSC), No additional information"
    }

    data object PilotVessel : AISShipType {
        override val code = 50
        override val description = "Pilot Vessel"
    }

    data object SearchAndRescueVessel : AISShipType {
        override val code = 51
        override val description = "Search and Rescue vessel"
    }

    data object Tug : AISShipType {
        override val code = 52
        override val description = "Tug"
    }

    data object PortTender : AISShipType {
        override val code = 53
        override val description = "Port Tender"
    }

    data object AntiPollutionEquipment : AISShipType {
        override val code = 54
        override val description = "Anti-pollution equipment"
    }

    data object LawEnforcement : AISShipType {
        override val code = 55
        override val description = "Law Enforcement"
    }

    data class SpareLocalVessel(override val code: Int) : AISShipType {
        init {
            require(code in 56..57) { "Spare local vessel AIS ship type code must be in 56..57" }
        }

        override val description = "Spare - Local Vessel"
    }

    data object MedicalTransport : AISShipType {
        override val code = 58
        override val description = "Medical Transport"
    }

    data object NoncombatantShip : AISShipType {
        override val code = 59
        override val description = "Noncombatant ship according to RR Resolution No. 18"
    }

    sealed interface Passenger : AISShipType

    data object AnyPassenger : Passenger {
        override val code = 60
        override val description = "Passenger, all ships of this type"
    }

    sealed class HazardousPassenger(private val category: String, final override val code: Int) : Passenger {
        final override val description = "Passenger, Hazardous category $category"
    }

    data object HazardousPassengerA : HazardousPassenger("A", 61)

    data object HazardousPassengerB : HazardousPassenger("B", 62)

    data object HazardousPassengerC : HazardousPassenger("C", 63)

    data object HazardousPassengerD : HazardousPassenger("D", 64)

    data class PassengerReservedForFutureUse(override val code: Int) : Passenger {
        init {
            require(code in 65..68) { "Reserved passenger AIS ship type code must be in 65..68" }
        }

        override val description = "Passenger, Reserved for future use"
    }

    data object PassengerNoAdditionalInformation : Passenger {
        override val code = 69
        override val description = "Passenger, No additional information"
    }

    sealed interface Cargo : AISShipType

    data object AnyCargo : Cargo {
        override val code = 70
        override val description = "Cargo, all ships of this type"
    }

    sealed class HazardousCargo(private val category: String, final override val code: Int) : Cargo {
        final override val description = "Cargo, Hazardous category $category"
    }

    data object HazardousCargoA : HazardousCargo("A", 71)

    data object HazardousCargoB : HazardousCargo("B", 72)

    data object HazardousCargoC : HazardousCargo("C", 73)

    data object HazardousCargoD : HazardousCargo("D", 74)

    data class CargoReservedForFutureUse(override val code: Int) : Cargo {
        init {
            require(code in 75..78) { "Reserved cargo AIS ship type code must be in 75..78" }
        }

        override val description = "Cargo, Reserved for future use"
    }

    data object CargoNoAdditionalInformation : Cargo {
        override val code = 79
        override val description = "Cargo, No additional information"
    }

    sealed interface Tanker : AISShipType

    data object AnyTanker : Tanker {
        override val code = 80
        override val description = "Tanker, all ships of this type"
    }

    sealed class HazardousTanker(private val category: String, final override val code: Int) : Tanker {
        final override val description = "Tanker, Hazardous category $category"
    }

    data object HazardousTankerA : HazardousTanker("A", 81)

    data object HazardousTankerB : HazardousTanker("B", 82)

    data object HazardousTankerC : HazardousTanker("C", 83)

    data object HazardousTankerD : HazardousTanker("D", 84)

    data class TankerReservedForFutureUse(override val code: Int) : Tanker {
        init {
            require(code in 85..88) { "Reserved tanker AIS ship type code must be in 85..88" }
        }

        override val description = "Tanker, Reserved for future use"
    }

    data object TankerNoAdditionalInformation : Tanker {
        override val code = 89
        override val description = "Tanker, No additional information"
    }

    sealed interface OtherType : AISShipType

    data object AnyOtherType : OtherType {
        override val code = 90
        override val description = "Other Type, all ships of this type"
    }

    sealed class HazardousOtherType(private val category: String, final override val code: Int) : OtherType {
        final override val description = "Other Type, Hazardous category $category"
    }

    data object HazardousOtherTypeA : HazardousOtherType("A", 91)

    data object HazardousOtherTypeB : HazardousOtherType("B", 92)

    data object HazardousOtherTypeC : HazardousOtherType("C", 93)

    data object HazardousOtherTypeD : HazardousOtherType("D", 94)

    data class OtherTypeReservedForFutureUse(override val code: Int) : OtherType {
        init {
            require(code in 95..98) { "Reserved other-type AIS ship type code must be in 95..98" }
        }

        override val description = "Other Type, Reserved for future use"
    }

    data object OtherTypeNoAdditionalInformation : OtherType {
        override val code = 99
        override val description = "Other Type, no additional information"
    }

    /**
     * Lookup helpers for AIS ship type codes.
     */
    companion object {
        /**
         * Returns the AIS ship type associated with [code], or `null` for values outside the AIS table.
         */
        fun fromCode(code: Int): AISShipType? = when (code) {
            0 -> NotAvailable
            in 1..19 -> ReservedForFutureUse(code)
            20 -> AnyWingInGround
            21 -> HazardousWingInGroundA
            22 -> HazardousWingInGroundB
            23 -> HazardousWingInGroundC
            24 -> HazardousWingInGroundD
            in 25..29 -> WingInGroundReservedForFutureUse(code)
            30 -> Fishing
            31 -> Towing
            32 -> TowingLarge
            33 -> DredgingOrUnderwaterOps
            34 -> DivingOps
            35 -> MilitaryOps
            36 -> Sailing
            37 -> PleasureCraft
            in 38..39 -> Reserved(code)
            40 -> AnyHighSpeedCraft
            41 -> HazardousHighSpeedCraftA
            42 -> HazardousHighSpeedCraftB
            43 -> HazardousHighSpeedCraftC
            44 -> HazardousHighSpeedCraftD
            in 45..48 -> HighSpeedCraftReservedForFutureUse(code)
            49 -> HighSpeedCraftNoAdditionalInformation
            50 -> PilotVessel
            51 -> SearchAndRescueVessel
            52 -> Tug
            53 -> PortTender
            54 -> AntiPollutionEquipment
            55 -> LawEnforcement
            in 56..57 -> SpareLocalVessel(code)
            58 -> MedicalTransport
            59 -> NoncombatantShip
            60 -> AnyPassenger
            61 -> HazardousPassengerA
            62 -> HazardousPassengerB
            63 -> HazardousPassengerC
            64 -> HazardousPassengerD
            in 65..68 -> PassengerReservedForFutureUse(code)
            69 -> PassengerNoAdditionalInformation
            70 -> AnyCargo
            71 -> HazardousCargoA
            72 -> HazardousCargoB
            73 -> HazardousCargoC
            74 -> HazardousCargoD
            in 75..78 -> CargoReservedForFutureUse(code)
            79 -> CargoNoAdditionalInformation
            80 -> AnyTanker
            81 -> HazardousTankerA
            82 -> HazardousTankerB
            83 -> HazardousTankerC
            84 -> HazardousTankerD
            in 85..88 -> TankerReservedForFutureUse(code)
            89 -> TankerNoAdditionalInformation
            90 -> AnyOtherType
            91 -> HazardousOtherTypeA
            92 -> HazardousOtherTypeB
            93 -> HazardousOtherTypeC
            94 -> HazardousOtherTypeD
            in 95..98 -> OtherTypeReservedForFutureUse(code)
            99 -> OtherTypeNoAdditionalInformation
            else -> null
        }
    }
}
