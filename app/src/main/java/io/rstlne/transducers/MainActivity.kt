package io.rstlne.transducers

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.rstlne.rxtransducers.transduce
import net.onedaybeard.transducers.Transducer
import net.onedaybeard.transducers.filter
import net.onedaybeard.transducers.map
import net.onedaybeard.transducers.plus
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Observable.range(0, 10)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat()) }

        (0..10)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat())}

        val transducer =
            filter<Int> { it % 2 != 0} +
            map<Float, Int> { sqrt(it.toFloat()) }

        Observable.range(0, 10).transduce(transducer)
        listOf(transducer, (0..10))
    }
}
