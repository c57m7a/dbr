package ru.c57m7a.db.events

import com.sun.jdi.event.BreakpointEvent
import com.sun.jdi.event.ExceptionEvent
import ru.c57m7a.db.TLocation
import ru.c57m7a.db.TValue
import javax.persistence.*

@Entity @Table(name = "breakpoint_event")
class TBreakpointEvent(e: BreakpointEvent) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "breakpoint_event_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "location_id", nullable = false)
    val location = TLocation[e.location()]

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]
}