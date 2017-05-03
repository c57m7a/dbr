package ru.c57m7a.db.events

import com.sun.jdi.event.Event
import java.util.*
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import javax.persistence.Temporal
import javax.persistence.TemporalType

@MappedSuperclass
abstract class TEvent(e: Event) {
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "date", nullable = false)
    val date = Date()
}