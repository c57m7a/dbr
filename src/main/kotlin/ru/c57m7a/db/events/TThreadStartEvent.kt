package ru.c57m7a.db.events

import com.sun.jdi.event.ThreadStartEvent
import ru.c57m7a.db.TLocation
import ru.c57m7a.db.TValue
import javax.persistence.*

@Entity @Table(name = "thread_start_event")
class TThreadStartEvent(e: ThreadStartEvent) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "thread_start_event_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]
}