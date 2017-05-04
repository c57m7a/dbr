package ru.c57m7a.db.events

import com.sun.jdi.event.AccessWatchpointEvent
import com.sun.jdi.event.ModificationWatchpointEvent
import com.sun.jdi.event.WatchpointEvent
import ru.c57m7a.db.TField
import ru.c57m7a.db.TLocation
import ru.c57m7a.db.TValue
import javax.persistence.*

@Entity @Table(name = "field_event")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("?") //default
open class TFieldEvent private constructor(e: WatchpointEvent): TEvent(e) {
    @Id @GeneratedValue @Column(name = "field_event_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "location_id", nullable = false)
    val location = TLocation[e.location()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "field_id", nullable = false)
    val field = TField[e.field()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "object_id", nullable = true)
    val obj = e.`object`()?.let { TValue.TObjectReference[it] }

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "value_id", nullable = true)
    val value = e.valueCurrent()?.let { TValue[it] }

    @Entity @DiscriminatorValue(value = "r")
    class TFieldAccessEvent(e: AccessWatchpointEvent) : TFieldEvent(e)

    @Entity @DiscriminatorValue(value = "w")
    class TFieldModificationEvent(e: ModificationWatchpointEvent) : TFieldEvent(e) {
        @ManyToOne(cascade = arrayOf(CascadeType.ALL))
        @JoinColumn(name = "new_value_id", nullable = true)
        val newValue = e.valueToBe()?.let { TValue[it] }
    }

    companion object {
        operator fun get(e: WatchpointEvent) = when (e) {
            is AccessWatchpointEvent -> TFieldAccessEvent(e)
            is ModificationWatchpointEvent -> TFieldModificationEvent(e)
            else -> throw ClassCastException("Unknown WatchpointEvent type ${e::class.qualifiedName}")
        }
    }
}