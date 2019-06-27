package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

enum class Gender : IndividualCharacteristic {

    MALE,
    FEMALE;

    companion object {
        private val MALE_KEYWORDS = setOf("male", "m", "MALE", "M")
        private val FEMALE_KEYWORDS = setOf("female", "f", "FEMALE", "F")

        fun getCategory(gender: String): Gender = when {
            MALE_KEYWORDS.contains(gender) -> MALE
            FEMALE_KEYWORDS.contains(gender) -> FEMALE
            else -> throw IllegalArgumentException("$gender is not a valid gender")
        }
    }
}