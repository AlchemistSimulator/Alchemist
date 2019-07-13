package it.unibo.alchemist.characteristics.individual

const val CHILD_THRESHOLD = 18

const val ADULT_THRESHOLD = 60

enum class Age : IndividualCharacteristic {

    CHILD,
    ADULT,
    ELDERLY;

    companion object {

        fun getCategory(age: Int): Age = when {
            age < CHILD_THRESHOLD -> CHILD
            age < ADULT_THRESHOLD -> ADULT
            else -> ELDERLY
        }
    }
}