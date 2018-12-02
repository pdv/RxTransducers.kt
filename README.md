# RxTransducers.kt
Allows transformation of [RxJava](https://github.com/ReactiveX/RxJava) streams with [Transducers](https://github.com/ReactiveX/RxJava) via an [extension](https://github.com/pdv/RxTransducers.kt/blob/master/rxtransducers/src/main/java/io/rstlne/rxtransducers/Observable%2BTransduce.kt) on `Observable`:

```
fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B>
```
Motivating example:
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
Transducers are composable transformations that are
1. transport-agnostic and therefore portable
2. faster than method chaining because reduction steps are flattened

Transducers can also have state, which is necessary for many useful Rx operators like `scan`:
```
fun <A, B> scan(initialValue: A, scanFn: (A, B) -> A) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunction<R, B> {
        private var currentValue = initialValue
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R {
            currentValue = scanFn(currentValue, input)
            return rf.apply(result, currentValue, reduced)
        }
    }
}
```
as well as popular iterable methods like `mapIndexed`:
```
fun <A, B> mapIndexed(f: (Int, B) -> A) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunction<R, B> {
        private var index = 0
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R =
            rf.apply(result, f(index++, input), reduced)
    }
}
```
