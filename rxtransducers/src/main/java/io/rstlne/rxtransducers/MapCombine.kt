package io.rstlne.rxtransducers

import io.reactivex.Observable
import net.onedaybeard.transducers.map
import net.onedaybeard.transducers.plus

val xform =
    map<Observable<Int>, Int> { Observable.fromIterable((it..it + 3)) } +
    scan<Observable<Int>, Observable<Int>>(Observable.empty<Int>()) { a, b ->
        Observable.merge(a, b)
    }
