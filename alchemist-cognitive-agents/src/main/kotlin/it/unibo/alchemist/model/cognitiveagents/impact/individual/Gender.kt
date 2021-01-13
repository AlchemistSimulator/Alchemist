package it.unibo.alchemist.model.cognitiveagents.impact.individual

/**
 * An enum representing the different genders.
 */
enum class Gender : Characteristic {

    MALE,
    FEMALE;

    companion object {
        private const val MALE_KEYWORD = "male"
        private const val FEMALE_KEYWORD = "female"

        /**
         * Returns the corresponding gender in this enum given a string resembling it.
         *
         * @param gender
         *          the gender as a string.
         */
        fun fromString(gender: String): Gender = when {
            gender.equals(MALE_KEYWORD, ignoreCase = true) -> MALE
            gender.equals(FEMALE_KEYWORD, ignoreCase = true) -> FEMALE
            else -> throw IllegalArgumentException("$gender is not a valid gender")
        }
    }
}
