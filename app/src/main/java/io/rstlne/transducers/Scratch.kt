package io.rstlne.transducers

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import net.onedaybeard.transducers.*
import java.util.concurrent.atomic.AtomicBoolean

val composedTransducer =
    filter<Int> { it % 5 == 0  } +
    map<Int, Int> { it / 5 }

val reduced = transduce(
    xf = composedTransducer,
    rf = { result, input -> result + input },
    init = 0,
    input = (0..20)
)

sealed class Action {
    data class SetTeamName(val teamName: String) : Action()
}

data class State(
    val isLoading: Boolean = false,
    val teamName: String = ""
)

sealed class Mutation {
    data class SetIsLoading(val isLoading: Boolean) : Mutation()
    data class SetTeamName(val teamName: String) : Mutation()
}

// Reactor

fun handleAction(action: Action) = when (action) {
    is Action.SetTeamName -> listOf(
        Mutation.SetIsLoading(true),
        // dependency(action.prop))
        Mutation.SetTeamName(action.teamName),
        Mutation.SetIsLoading(false))
}

fun reduceState(state: State, mutation: Mutation) = when (mutation) {
    is Mutation.SetIsLoading -> state.copy(isLoading = mutation.isLoading)
    is Mutation.SetTeamName -> state.copy(teamName = mutation.teamName)
}

/**
 * Creates a transducer that transforms a reducing function by
 * - performing an "inner reduction"
 * - accumulating an A from Bs via [scanFn] called on successive inputs
 */
fun <A, B> scan(
    initialValue: A,
    scanFn: (A, B) -> A
) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunction<R, B> {
        private var currentValue = initialValue
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R {
            currentValue = scanFn(currentValue, input)
            return rf.apply(result, currentValue, reduced)
        }
    }
}

val actionToMutationTransducer = mapcat(::handleAction)
val mutationToStateTransducer = scan(State(), ::reduceState)
val reactorTransducer = actionToMutationTransducer + mutationToStateTransducer

val mutations = listOf(actionToMutationTransducer, Action.SetTeamName("The Foo Bars"))

val finalState = transduce(
    xf = actionToMutationTransducer,
    rf = ::reduceState,
    init = State(teamName = ""),
    input = listOf(
        Action.SetTeamName("The Foo Bars"),
        Action.SetTeamName("The Fear Bears")))

val listOfStates = listOf(reactorTransducer,
    Action.SetTeamName("The Foo Bar Bears"),
    Action.SetTeamName("The Bad News Bars"))

fun <A, R : ObservableEmitter<A>> observableRf(): ReducingFunction<R, A> {
    return object : ReducingFunction<R, A> {
        override fun apply(result: R,
                           input: A,
                           reduced: AtomicBoolean): R =
            result.apply {
                if (reduced.get()) onComplete()
                else onNext(input)
            }
    }
}

fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>) = Observable.create<B> { emitter ->
    val completed = AtomicBoolean(false)
    val rf = xf.apply(observableRf<B, ObservableEmitter<B>>())
    subscribe(object : Observer<A> {
        override fun onNext(t: A) {
            rf.apply(emitter, t, completed)
        }
        override fun onSubscribe(d: Disposable) =
            emitter.setDisposable(d)
        override fun onComplete() {
            completed.set(true)
            emitter.onComplete()
        }
        override fun onError(e: Throwable) = emitter.onError(e)
    })
}

val obsOfStates = Observable.just<Action>(
    Action.SetTeamName("The RxBar wefhlihsfwj"),
    Action.SetTeamName("RxVHWl WEFl1")
).transduce(reactorTransducer)