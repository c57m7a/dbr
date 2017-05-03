package ru.c57m7a.db.events

import com.sun.jdi.event.ThreadDeathEvent
import ru.c57m7a.db.TLocation
import ru.c57m7a.db.TValue
import javax.persistence.*

@Entity @Table(name = "thread_death_event")
class TThreadDeathEvent(e: ThreadDeathEvent) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "thread_death_event_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[e.thread()]
}