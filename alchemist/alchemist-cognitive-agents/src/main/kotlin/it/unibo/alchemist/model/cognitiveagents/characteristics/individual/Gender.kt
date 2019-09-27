package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

enum class Gender : IndividualCharacteristic {

    MALE,
    FEMALE;

    companion object {
        private const val MALE_KEYWORD = "male"
        private const val FEMALE_KEYWORD = "female"

        fun fromString(gender: String): Gender = when {
            gender.equals(MALE_KEYWORD, ignoreCase = true) -> MALE
            gender.equals(FEMALE_KEYWORD, ignoreCase = true) -> FEMALE
            else -> throw IllegalArgumentException("$gender is not a valid gender")
        }
    }
}