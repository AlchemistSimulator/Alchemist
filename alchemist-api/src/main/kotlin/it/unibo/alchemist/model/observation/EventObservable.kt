package it.unibo.alchemist.model.observation

/**
 * An implementation of [Observable] that emits updates to its observers when triggered manually
 * via the [emit] method. The observed value is always [Unit]. A standard [MutableObservable] of
 * [Unit] is not the answer to an observable emitter, because of observable idempotency.
 *
 * This class allows multiple observers to register for notifications, and provides the ability
 * to manually emit updates to those observers.
 */
class EventObservable : Observable<Unit> {

    private val observingCallbacks: MutableMap<Any, List<(Unit) -> Unit>> = linkedMapOf()

    override val current: Unit = Unit

    override val observers: List<Any> get() = observingCallbacks.keys.toList()

    /**
     * Triggers the emission of a notification from this observable notifying
     * all of its observers.
     */
    fun emit() {
        observingCallbacks.values.forEach { callbacks -> callbacks.forEach { it(Unit) } }
    }

    override fun onChange(registrant: Any, callback: (Unit) -> Unit) {
        observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
        callback(current)
    }

    override fun stopWatching(registrant: Any) {
        observingCallbacks.remove(registrant)
    }
}
