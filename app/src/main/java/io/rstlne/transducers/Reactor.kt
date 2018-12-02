package io.rstlne.transducers

import io.reactivex.Observable
import io.rstlne.rxtransducers.scan
import io.rstlne.rxtransducers.transduce
import net.onedaybeard.transducers.mapcat
import net.onedaybeard.transducers.plus
import net.onedaybeard.transducers.transduce

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

fun handleAction(action: Action) = when (action) {
    is Action.SetTeamName -> listOf(
        Mutation.SetIsLoading(true),
        Mutation.SetTeamName(action.teamName),
        Mutation.SetIsLoading(false))
}

fun reduceState(state: State, mutation: Mutation) = when (mutation) {
    is Mutation.SetIsLoading -> state.copy(isLoading = mutation.isLoading)
    is Mutation.SetTeamName -> state.copy(teamName = mutation.teamName)
}

val actionToMutationTransducer = mapcat(::handleAction)
val mutationToStateTransducer = scan(State(), ::reduceState)
val reactorTransducer = actionToMutationTransducer + mutationToStateTransducer

val actions = listOf<Action>(
    Action.SetTeamName("The Foo Bars"),
    Action.SetTeamName("The Fear Bears"),
    Action.SetTeamName("The Bar Foo Bears")
)

val finalState = transduce(
    xf = actionToMutationTransducer,
    rf = ::reduceState,
    init = State(teamName = ""),
    input = listOf(
        Action.SetTeamName("The Foo Bars"),
        Action.SetTeamName("The Fear Bears")))


val states = net.onedaybeard.transducers.listOf(reactorTransducer, actions)

val obsOfStates = Observable.fromIterable(actions).transduce(reactorTransducer)