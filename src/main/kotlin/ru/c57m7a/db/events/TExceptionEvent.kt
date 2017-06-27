package ru.c57m7a.db.events

import com.sun.jdi.event.ExceptionEvent
import ru.c57m7a.db.TLocation
import ru.c57m7a.db.TValue
import javax.persistence.*

@Entity @Table(name = "exception_event")
class TExceptionEvent(e: ExceptionEvent) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "exception_event_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "catch_location_id", nullable = true)
    val catchLocation = e.catchLocation()?.let { TLocation[it] }

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "catch_location_id", nullable = false)
    val location = TLocation[e.location()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "exception_obj_ref_id", nullable = false)
    val exception = TValue.TObjectReference[e.exception()]
}