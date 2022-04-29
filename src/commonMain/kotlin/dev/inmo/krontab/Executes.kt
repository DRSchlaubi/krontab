package dev.inmo.krontab

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * Execute [block] once at the [KronScheduler.next] time and return result of [block] calculation.
 *
 * WARNING!!! If you want to launch it in parallel, you must do this explicitly.
 *
 * WARNING!!! In case if [KronScheduler.next] of [this] instance will return null, [block] will be called immediately
 */
suspend inline fun <T> KronScheduler.doOnceLocal(noinline block: suspend (DateTime) -> T): T {
    val time = nextOrNow().also {
        delay((it - DateTime.now()).millisecondsLong)
    }
    return block(time)
}

/**
 * Execute [block] once at the [KronScheduler.next] time and return result of [block] calculation.
 *
 * WARNING!!! If you want to launch it in parallel, you must do this explicitly.
 *
 * WARNING!!! In case if [KronScheduler.next] of [this] instance will return null, [block] will be called immediatelly
 */
suspend inline fun <T> KronScheduler.doOnceTz(noinline block: suspend (DateTimeTz) -> T): T {
    val time = when (this) {
        is KronSchedulerTz -> nextOrNowWithOffset()
        else -> nextOrNow().local
    }
    delay((time - DateTimeTz.nowLocal()).millisecondsLong)
    return block(time)
}

/**
 * Execute [block] once at the [KronScheduler.next] time and return result of [block] calculation.
 *
 * WARNING!!! If you want to launch it in parallel, you must do this explicitly.
 *
 * WARNING!!! In case if [KronScheduler.next] of [this] instance will return null, [block] will be called immediately
 */
suspend inline fun <T> KronScheduler.doOnce(noinline block: suspend () -> T): T = doOnceLocal { _ -> block() }

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doOnceLocal] on it
 * @see buildSchedule
 */
suspend inline fun <T> doOnce(
    scheduleConfig: String,
    noinline block: suspend (DateTime) -> T
) = buildSchedule(scheduleConfig).doOnceLocal(block)

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doOnceLocal] on it
 * @see buildSchedule
 */
suspend inline fun <T> doOnceTz(
    scheduleConfig: String,
    noinline block: suspend (DateTimeTz) -> T
) = buildSchedule(scheduleConfig).doOnceTz(block)

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doOnceLocal] on it
 * @see buildSchedule
 */
suspend inline fun <T> doOnce(
    scheduleConfig: String,
    noinline block: suspend () -> T
) = doOnce(scheduleConfig) { _ -> block() }


/**
 * Will execute [block] while it will return true as a result of its calculation
 */
suspend inline fun KronScheduler.doWhileLocal(noinline block: suspend (DateTime) -> Boolean) {
    do {
        delay(1L)
    } while (doOnceLocal(block))
}

/**
 * Will execute [block] while it will return true as a result of its calculation
 */
suspend inline fun KronScheduler.doWhileTz(noinline block: suspend (DateTimeTz) -> Boolean) {
    do {
        delay(1L)
    } while (doOnceTz(block))
}

/**
 * Will execute [block] while it will return true as a result of its calculation
 */
suspend inline fun KronScheduler.doWhile(noinline block: suspend () -> Boolean) = doWhileLocal { block() }

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doWhile] with [block]
 *
 * @see buildSchedule
 */
suspend inline fun doWhileLocal(
    scheduleConfig: String,
    noinline block: suspend (DateTime) -> Boolean
) = buildSchedule(scheduleConfig).doWhileLocal(block)

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doWhile] with [block]
 *
 * @see buildSchedule
 */
suspend inline fun doWhileTz(
    scheduleConfig: String,
    noinline block: suspend (DateTimeTz) -> Boolean
) = buildSchedule(scheduleConfig).doWhileTz(block)

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doWhile] with [block]
 *
 * @see buildSchedule
 */
suspend inline fun doWhile(
    scheduleConfig: String,
    noinline block: suspend () -> Boolean
) = doWhileLocal(scheduleConfig) { block() }


/**
 * Will execute [block] without any checking of result
 */
suspend inline fun KronScheduler.doInfinityLocal(noinline block: suspend (DateTime) -> Unit) = doWhileLocal {
    block(it)
    coroutineContext.isActive
}

/**
 * Will execute [block] without any checking of result
 */
suspend inline fun KronScheduler.doInfinityTz(noinline block: suspend (DateTimeTz) -> Unit) = doWhileTz {
    block(it)
    coroutineContext.isActive
}

/**
 * Will execute [block] without any checking of result
 */
suspend inline fun KronScheduler.doInfinity(noinline block: suspend () -> Unit) = doWhile {
    block()
    coroutineContext.isActive
}

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doInfinity] with [block]
 *
 * @see buildSchedule
 */
suspend inline fun doInfinityLocal(
    scheduleConfig: String,
    noinline block: suspend (DateTime) -> Unit
) = buildSchedule(scheduleConfig).doInfinityLocal(block)

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doInfinity] with [block]
 *
 * @see buildSchedule
 */
suspend inline fun doInfinityTz(
    scheduleConfig: String,
    noinline block: suspend (DateTimeTz) -> Unit
) = buildSchedule(scheduleConfig).doInfinityTz(block)

/**
 * Will [buildSchedule] using [scheduleConfig] and call [doInfinity] with [block]
 *
 * @see buildSchedule
 */
suspend inline fun doInfinity(
    scheduleConfig: String,
    noinline block: suspend () -> Unit
) = buildSchedule(scheduleConfig).doInfinity(block)