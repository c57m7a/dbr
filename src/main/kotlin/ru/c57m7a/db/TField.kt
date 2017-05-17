package ru.c57m7a.db

import com.sun.jdi.ClassNotLoadedException
import com.sun.jdi.Field
import ru.c57m7a.db.TType.TReferenceType
import ru.c57m7a.utils.ObjectCache
import ru.c57m7a.utils.tryOrNull
import javax.persistence.*

@Entity @Table(name = "field")
class TField private constructor(field: Field) {
    @Id @GeneratedValue @Column(name = "field_id") val id = 0

    /* Accessible */
    @Column(name = "is_private", nullable = false)
    val isPrivate = field.isPrivate

    @Column(name = "is_package_private", nullable = false)
    val isPackagePrivate = field.isPackagePrivate

    @Column(name = "is_protected", nullable = false)
    val isProtected = field.isProtected

    @Column(name = "is_public", nullable = false)
    val isPublic = field.isPublic

    /* TypeComponent */
    @Column(name = "name", nullable = false)
    val name: String = field.name()

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "declaring_reference_type_id", nullable = false)
    var declaringType: TReferenceType? = null

    @Column(name = "is_static", nullable = false)
    val isStatic = field.isStatic

    @Column(name = "is_final", nullable = false)
    val isFinal = field.isFinal

    @Column(name = "is_synthetic", nullable = false)
    val isSynthetic = field.isSynthetic

    /* Field */
    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "type_id", nullable = true)
    val type = tryOrNull<TType, ClassNotLoadedException> { TType[field.type()] }

    @Column(name = "is_transient", nullable = false)
    val isTransient = field.isTransient

    @Column(name = "is_volatile", nullable = false)
    val isVolatile = field.isVolatile

    @Column(name = "is_enum_constant", nullable = false)
    val isEnumConstant = field.isEnumConstant

    companion object : ObjectCache<Field, TField>(::TField)
}