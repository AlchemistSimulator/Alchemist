package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

enum class Age : IndividualCharacteristic {

    CHILD,
    ADULT,
    ELDERLY;

    companion object {

        private const val CHILD_THRESHOLD = 18
        private const val ADULT_THRESHOLD = 60
        private val CHILD_KEYWORDS = setOf("child", "CHILD")
        private val ADULT_KEYWORDS = setOf("adult", "ADULT")
        private val ELDERLY_KEYWORDS = setOf("elderly", "ELDERLY")

        fun getCategory(age: Int): Age = when {
            age < CHILD_THRESHOLD -> CHILD
            age < ADULT_THRESHOLD -> ADULT
            else -> ELDERLY
        }

        fun getCategory(age: String): Age = when {
            CHILD_KEYWORDS.contains(age) -> CHILD
            ADULT_KEYWORDS.contains(age) -> ADULT
            ELDERLY_KEYWORDS.contains(age) -> ELDERLY
            else -> throw IllegalArgumentException("$age is not a valid age")
        }
    }
}