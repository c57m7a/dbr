package ru.c57m7a.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.OutputStream
import java.io.PrintStream
import java.util.*
import kotlin.reflect.jvm.jvmName

val logger: Logger = LogManager.getRootLogger()

inline fun <reified T: Any> T.toDefaultString() = T::class.jvmName + "@" + Integer.toHexString(hashCode())

inline operator fun Int.invoke(action: (Int) -> Unit) = repeat(this, action)

operator fun Char.times(times: Int) = CharArray(times) { this }
operator fun CharSequence.times(times: Int) = CharArray(length * times) { this[it % length] }

val clipboard: Clipboard by lazy {
    Toolkit.getDefaultToolkit().systemClipboard
}

fun String.copyToClipboard() {
    val selection = StringSelection(this)
    clipboard.setContents(selection, selection)
}

@Suppress("unused")
fun String.Companion.fromClipboard() = if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
    clipboard.getData(DataFlavor.stringFlavor) as String
} else null

inline fun <R, T : Throwable> tryOrNull(block: () -> R): R? = try {
    block()
} catch (t: T) { null }

fun StringTokenizer.nextTokenOrNull() = if (hasMoreTokens()) nextToken() else null

fun <T> T.applyIf(predicate: Boolean, action: T.() -> T) = if (predicate) action(this) else this

inline fun <T> onEach(vararg values: T, action: T.() -> Unit) = values.forEach(action)

inline fun <T> letEach(vararg values: T, action: (T) -> Unit) = values.forEach(action)

val stdInScanner = Scanner(System.`in`)

val voidOutputStream = object : OutputStream() {
    override fun write(b: Int) {}
}
val voidPrintStream = PrintStream(voidOutputStream)