package ru.c57m7a.db

import com.sun.jdi.AbsentInformationException
import com.sun.jdi.Location
import ru.c57m7a.utils.ObjectCache
import ru.c57m7a.utils.tryOrNull
import javax.persistence.*

@Entity @Table(name="location")
class TLocation private constructor(location: Location) {
    @Id @GeneratedValue @Column(name = "location_id") val id = 0

    @ManyToOne(cascade = arrayOf(CascadeType.ALL), optional = true)
    @JoinColumn(name = "declaring_ref_type_id")
    var declaringType: TType.TReferenceType? = null

    @OneToOne(cascade = arrayOf(CascadeType.ALL), mappedBy = "location", optional = false)
    val method = TMethod[location.method()]

    @Column(name = "code_index", nullable = false)
    val codeIndex = location.codeIndex()

    @Column(name = "source_path", nullable = false)
    val sourcePath = tryOrNull<String, AbsentInformationException> { location.sourcePath() }

    @Column(name = "line_number", nullable = false)
    val lineNumber = location.lineNumber()

    companion object : ObjectCache<Location, TLocation>(::TLocation)
}