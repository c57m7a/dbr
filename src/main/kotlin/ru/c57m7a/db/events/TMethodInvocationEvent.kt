package ru.c57m7a.db.events

import com.sun.jdi.AbsentInformationException
import com.sun.jdi.InternalException
import com.sun.jdi.event.MethodEntryEvent
import ru.c57m7a.db.TLocalVariable
import ru.c57m7a.db.TLocation
import ru.c57m7a.db.TMethod
import ru.c57m7a.db.TValue
import java.util.*
import javax.persistence.*

@Entity @Table(name = "method_inv")
class TMethodInvocationEvent(e: MethodEntryEvent) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "method_inv_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "location_id", nullable = false)
    val location = TLocation[e.location()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "entry_time", nullable = false)
    lateinit var entryTime: Date

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
            name = "method__local_variable",
            joinColumns = arrayOf(JoinColumn(name = "method_inv_id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "local_variable_id"))
    )
    val variables: List<TLocalVariable>?

    @ManyToMany(cascade = arrayOf(CascadeType.ALL)) @JoinTable(
            name = "method_inv__arg_values",
            joinColumns = arrayOf(JoinColumn(name = "method_inv_id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "argument_value_id"))
    )
    val argumentValues: List<TValue?>?

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "return_value_id", nullable = true)
    var returnValue: TValue? = null

    init {
        val frame by lazy { e.thread().frame(0) }

        thread.methodInvocationEvents += this

        thisObject = if (!method.isStatic && !method.isNative) {
            TValue.TObjectReference[frame.thisObject()]
        } else null

        variables = if (method.isNative) null else try {
            frame.visibleVariables().map { TLocalVariable[it] }
        } catch (e: AbsentInformationException) {
            null
        }

        argumentValues = if (method.isNative) null else try {
            frame.argumentValues.map { value -> value?.let { TValue[it] } }
        } catch (e: InternalException) {
            null
        }
    }
}