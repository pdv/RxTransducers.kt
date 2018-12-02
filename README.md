# RxTransducers.kt
Allows transformation of [RxJava](https://github.com/ReactiveX/RxJava) streams with [Transducers](https://github.com/ReactiveX/RxJava) via an [extension](https://github.com/pdv/RxTransducers.kt/blob/master/rxtransducers/src/main/java/io/rstlne/rxtransducers/Observable%2BTransduce.kt) on `Observable`:

```
fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B>
```
Instead of writing
```
Observable.range(0, 10)
    .filter { it % 2 != 0 }
    .map { sqrt(it.toFloat()) }

(0..10)
    .filter { it % 2 != 0 }
    .map { sqrt(it.toFloat())}
```
you can write
```
val transducer =
    filter<Int> { it % 2 != 0} +
    map<Float, Int> { sqrt(it.toFloat()) }

Observable.range(0, 10).transduce(transducer)
listOf(transducer, (0..10))
```
which is
1. more portable, because the transducer is transport-agnostic
2. slightly faster, because the steps of the transducer are composed

Transducers can also have state, which is necessary for many useful Rx operators like `scan`:
```
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
```
