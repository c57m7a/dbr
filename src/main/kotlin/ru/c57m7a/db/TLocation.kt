package ru.c57m7a.db

import com.sun.jdi.AbsentInformationException
import com.sun.jdi.Location
import ru.c57m7a.utils.ObjectCache
import ru.c57m7a.utils.tryOrNull
import javax.persistence.*

@Entity @Table(name="location")
class TLocation private constructor(location: Location) {
    @Id @GeneratedValue @Column(name = "location_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "reference_type_id", nullable = false)
    lateinit var declaringType: TType.TReferenceType

    @OneToOne(cascade = arrayOf(CascadeType.ALL), mappedBy = "location", optional = false)
    val method = TMethod[location.method()].also { it.location = this }

    @Column(name = "code_index", nullable = false)
    val codeIndex = location.codeIndex()

    @Column(name = "source_path", nullable = false)
    val sourcePath = tryOrNull<String, AbsentInformationException> { location.sourcePath() }

    @Column(name = "line_number", nullable = false)
    val lineNumber = location.lineNumber()

    companion object : ObjectCache<Location, TLocation>(::TLocation)
}