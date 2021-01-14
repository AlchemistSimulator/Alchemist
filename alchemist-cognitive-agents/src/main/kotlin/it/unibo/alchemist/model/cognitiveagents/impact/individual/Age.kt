package it.unibo.alchemist.model.cognitiveagents.impact.individual

/**
 * An enum representing the different periods of life.
 */
enum class Age : Characteristic {

    CHILD,
    ADULT,
    ELDERLY;

    companion object {

        private const val CHILD_THRESHOLD = 18
        private const val ADULT_THRESHOLD = 60
        private const val CHILD_KEYWORD = "child"
        private const val ADULT_KEYWORD = "adult"
        private const val ELDERLY_KEYWORD = "elderly"

        /**
         * Returns the corresponding age in this enum given the age in years.
         *
         * @param age
         *          the age in years.
         */
        fun fromYears(age: Int): Age = when {
            age < CHILD_THRESHOLD -> CHILD
            age < ADULT_THRESHOLD -> ADULT
            else -> ELDERLY
        }

        /**
         * Returns the corresponding age in this enum given a string resembling it.
         *
         * @param age
         *          the age as a string.
         */
        fun fromString(age: String): Age = when {
            age.equals(CHILD_KEYWORD, ignoreCase = true) -> CHILD
            age.equals(ADULT_KEYWORD, ignoreCase = true) -> ADULT
            age.equals(ELDERLY_KEYWORD, ignoreCase = true) -> ELDERLY
            else -> throw IllegalArgumentException("$age is not a valid age")
        }
    }
}
