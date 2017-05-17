package ru.c57m7a.db.events

import com.sun.jdi.InternalException
import com.sun.jdi.event.MethodEntryEvent
import ru.c57m7a.db.TMethod
import ru.c57m7a.db.TValue
import java.util.*
import javax.persistence.*

@Entity @Table(name = "method_inv")
class TMethodInvocationEvent(e: MethodEntryEvent) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "method_inv_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "exit_time", nullable = false)
    lateinit var exitTime: Date

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "method_id", nullable = false)
    val method = TMethod[e.method()].also { it.methodInvocations += this }

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "this_object_id", nullable = true)
    val thisObject: TValue.TObjectReference?

    @ManyToMany(cascade = arrayOf(CascadeType.ALL)) @JoinTable(
            name = "method_inv__arg_value",
            joinColumns = arrayOf(JoinColumn(name = "method_inv_id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "argument_value_id"))
    ) val argumentValues: List<TValue?>?

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "return_value_id", nullable = true)
    var returnValue: TValue? = null

    init {
        val frame by lazy { e.thread().frame(0) }

        thread.methodInvocationEvents += this

        thisObject = if (method.isStatic || method.isNative) null else {
            TValue.TObjectReference[frame.thisObject()]
        }

        argumentValues = if (method.isNative) null else try {
            frame.argumentValues.map { value -> value?.let { TValue[it] } }
        } catch (e: InternalException) {
            null
        }
    }
}