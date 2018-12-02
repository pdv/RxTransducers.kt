# RxTransducers.kt
Allows transformation of [RxJava](https://github.com/ReactiveX/RxJava) streams with [Transducers](https://github.com/ReactiveX/RxJava) via an extension on `Observable`:

```
fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B>
```
Instead of writing
```
Observable.range(0, 10000000)
    .filter { it % 2 != 0 }
    .map { sqrt(it.toFloat()) }
```
you can write
```
Observable.range(0, 10000000)
    .transduce(
        filter<Int> { it % 2 != 0 } +
        map<Float, Int> { sqrt(it.toFloat()) }
    )
```
which is
1. more portable, because the transducer passed to `transduce` can also be applied to other transportation mechanisms like lists (`listof(xf, items)`)
2. slightly faster, because the steps of the transducer are composed

Many useful Rx operators work as transducers, like `scan`:

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
