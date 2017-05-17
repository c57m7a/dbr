package ru.c57m7a.db.events

import com.sun.jdi.ThreadReference
import com.sun.jdi.event.Event
import com.sun.jdi.event.ThreadDeathEvent
import com.sun.jdi.event.ThreadStartEvent
import ru.c57m7a.db.TValue
import javax.persistence.*

@Entity @Table(name = "thread_event")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("?") //default
open class TThreadEvent private constructor(e: Event, thread: ThreadReference) : TEvent(e) {
    @Id @GeneratedValue @Column(name = "thread_event_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "thread_id", nullable = false)
    val thread = TValue.TObjectReference.TThreadReference[thread]

    @Entity @DiscriminatorValue("s")
    class TThreadStartEvent(e: ThreadStartEvent) : TThreadEvent(e, e.thread())

    @Entity @DiscriminatorValue("d")
    class TThreadDeathEvent(e: ThreadDeathEvent) : TThreadEvent(e, e.thread())
}