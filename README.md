# RxTransducers.kt
Allows transformation of [RxJava](https://github.com/ReactiveX/RxJava) streams with [Transducers](https://clojure.org/reference/transducers) via an extension on `Observable` using [transducers-kotlin](https://github.com/junkdog/transducers-kotlin):

```
fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B>
```
### Usage
```
val list = (0 until 10)
    .filter { it % 2 == 0 }
    .scan(0) { a, b -> a + b }
    .mapIndexed { index, i: Int -> "$index: $i" }

val rxChain = Observable.range(0, 10)
    .filter { it % 2 == 0 }
    .scan(0) { a, b -> a + b }
    .skip(1)
    .mapIndexed { index, i: Int -> "$index: $i" }
    .blockingIterable()
    .toList()

fun transducer() =
    filter<Int> { it % 2 == 0 } +
    scan(0) { a, b -> a + b } +
    mapIndexed { index, i: Int -> "$index: $i" }

val rxTransduced = Observable.range(0, 10)
    .transduce(transducer())
    .blockingIterable()
    .toList()

val listTransduced = listOf(transducer(), (0 until 10))

assertEquals(list, rxChain)
assertEquals(list, rxTransduced)
assertEquals(list, listTransduced)
=> [0: 0, 1: 2, 2: 6, 3: 12, 4: 20]
```
## Additional Transducers

- `scan` - like RxJava [`scan`](http://reactivex.io/RxJava/javadoc/rx/Observable.html#scan(R,%20rx.functions.Func2)) but skipping the seed emission
- `mapIndexed` - like Kotlin [`mapIndexed`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/map-indexed.html)
