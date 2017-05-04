package ru.c57m7a.db

import com.sun.jdi.*
import ru.c57m7a.utils.ForeignKeyObjectCache
import ru.c57m7a.utils.ObjectCache
import ru.c57m7a.utils.tryOrNull
import java.util.*
import javax.persistence.*

@Entity @Table(name = "type")
@Inheritance(strategy = InheritanceType.JOINED)
open class TType private constructor(
        @Column(name = "name", nullable = false)
        val name: String,

        @Column(name = "signature", nullable = false)
        val signature: String
) {
    constructor(type: Type) : this(type.name(), type.signature())

    companion object : ObjectCache<Type, TType>(::TType) {
        val VOID_TYPE = TType("void", "V")
    }

    @Id @GeneratedValue @Column(name = "type_id") val id = 0

    @Entity @Table(name = "reference_type")
    @Inheritance(strategy = InheritanceType.JOINED)
    @PrimaryKeyJoinColumn(name = "reference_type_id", referencedColumnName = "type_id")
    sealed class TReferenceType(referenceType: ReferenceType) : TType(referenceType) {
        companion object {
            operator fun get(referenceType: ReferenceType) = when (referenceType) {
                is ClassType -> TType.TReferenceType.TClassType[referenceType]
                is InterfaceType -> TType.TReferenceType.TInterfaceType[referenceType]
                is ArrayType -> TType.TReferenceType.TArrayType[referenceType]
                else -> throw ClassCastException("Unknown declaringType type ${referenceType::class.qualifiedName}")
            }
        }

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "loaded_at", nullable = false)
        val loadedAt = Date()

        /* Accessible */

        @Column(name = "is_private", nullable = false)
        val isPrivate = referenceType.isPrivate

        @Column(name = "is_package_private", nullable = false)
        val isPackagePrivate = referenceType.isPackagePrivate

        @Column(name = "is_protected", nullable = false)
        val isProtected = referenceType.isProtected

        @Column(name = "is_public", nullable = false)
        val isPublic = referenceType.isPublic

        /* ReferenceType */
        @ManyToOne(cascade = arrayOf(CascadeType.ALL), optional = true)
        @JoinColumn(name = "class_loader_id")
        var classLoader: TValue.TObjectReference.TClassLoaderReference? = null

        @Column(name = "source_name", nullable = true)
        val sourceName = tryOrNull<String, AbsentInformationException> { referenceType.sourceName() }

        @Column(name = "source_debug_extension", nullable = true)
        val sourceDebugExtension = tryOrNull<String, AbsentInformationException> { referenceType.sourceDebugExtension() }

        @Column(name = "is_static", nullable = false)
        val isStatic = referenceType.isStatic

        @Column(name = "is_abstract", nullable = false)
        val isAbstract = referenceType.isAbstract

        @Column(name = "is_final", nullable = false)
        val isFinal = referenceType.isFinal

        @Column(name = "is_prepared", nullable = false)
        val isPrepared = referenceType.isPrepared

        @Column(name = "is_verified", nullable = false)
        val isVerified = referenceType.isVerified

        @Column(name = "is_initialized", nullable = false)
        val isInitialized = referenceType.isInitialized

        @Column(name = "is_failed_to_initialize", nullable = false)
        val isFailedToInitialize = referenceType.failedToInitialize()

        @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "declaringType")
        val fields = tryOrNull<List<TField>, ClassNotPreparedException> { referenceType.fields().map { TField[it].also { it.declaringType = this } } }

        @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "declaringType")
        val methods = tryOrNull<List<TMethod>, ClassNotPreparedException> { referenceType.methods().map { TMethod[it].also { it.declaringType = this } } }

        @ManyToOne(cascade = arrayOf(CascadeType.ALL), optional = true)
        @JoinColumn(name = "base_reference_type_id")
        var baseType: TType.TReferenceType? = null

        @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "baseType")
        val nestedTypes = referenceType.nestedTypes().map { TType.TReferenceType[it].also { it.baseType = this } }

        @OneToOne(cascade = arrayOf(CascadeType.ALL), mappedBy = "reflectedType", optional = true)
        var classObject: TValue.TObjectReference.TClassObjectReference? = null

        @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "declaringType")
        val lineLocations = tryOrNull<List<TLocation>, ClassNotPreparedException> { referenceType.allLineLocations().map { TLocation[it].apply { assert(referenceType == this@TReferenceType) } } }

        open class SignatureObjectCache<in K : ReferenceType, V : TType.TReferenceType> protected constructor(default: (K) -> V)
            : ForeignKeyObjectCache<K, String, V>(default, ReferenceType::signature)

        @Entity @Table(name = "class_type")
        @PrimaryKeyJoinColumn(name = "class_type_id", referencedColumnName = "reference_type_id")
        class TClassType private constructor(classType: ClassType) : TReferenceType(classType) {
            companion object : SignatureObjectCache<ClassType, TClassType>(::TClassType)

            @ManyToOne(cascade = arrayOf(CascadeType.ALL), optional = true)
            @JoinColumn(name = "superclass_class_type_id")
            var superclass: TClassType? = null

            @ManyToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "implementors")
            val interfaces = tryOrNull<List<TInterfaceType>, ClassNotPreparedException> { classType.interfaces().map { TInterfaceType[it].also { it.implementors += this } } }

            @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "superclass")
            val subclasses = tryOrNull<List<TClassType>, ClassNotPreparedException> { classType.subclasses().map { TClassType[it].also { it.superclass = this } } }

            @Column(name = "is_enum", nullable = false)
            val isEnum = classType.isEnum
        }

        @Entity @Table(name = "interface_type")
        @PrimaryKeyJoinColumn(name = "interface_type_id", referencedColumnName = "reference_type_id")
        class TInterfaceType private constructor(interfaceType: InterfaceType) : TReferenceType(interfaceType) {
            companion object : SignatureObjectCache<InterfaceType, TInterfaceType>(::TInterfaceType)

            @ManyToMany(cascade = arrayOf(CascadeType.ALL)) @JoinTable(
                    name = "interface__superinterface",
                    joinColumns = arrayOf(JoinColumn(name = "interface_id")),
                    inverseJoinColumns = arrayOf(JoinColumn(name = "superinterface_id"))
            )
            val superinterfaces = interfaceType.superinterfaces().map { TInterfaceType[it].also { it.subinterfaces += this } }

            @ManyToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "superinterfaces")
            val subinterfaces = mutableListOf<TInterfaceType>()

            @ManyToMany(cascade = arrayOf(CascadeType.ALL)) @JoinTable(
                    name = "interface__implementor",
                    joinColumns = arrayOf(JoinColumn(name = "interface_id")),
                    inverseJoinColumns = arrayOf(JoinColumn(name = "implementor_class_type_id"))
            )
            val implementors = mutableListOf<TClassType>()
        }

        @Entity @Table(name = "array_type")
        @PrimaryKeyJoinColumn(name = "array_type_id", referencedColumnName = "reference_type_id")
        class TArrayType private constructor(arrayType: ArrayType) : TReferenceType(arrayType) {
            companion object : SignatureObjectCache<ArrayType, TArrayType>(::TArrayType)

            @Column(name = "component_signature", nullable = false)
            val componentSignature: String = arrayType.componentSignature()

            @Column(name = "component_type_name", nullable = false)
            val componentTypeName: String = arrayType.componentTypeName()
        }
    }

    @Entity @Table(name = "primitive_type")
    @PrimaryKeyJoinColumn(name = "primitive_type_id", referencedColumnName = "type_id")
    class TPrimitiveType private constructor(name: String, signature: String) : TType(name, signature) {
        companion object {
            val BOOLEAN_TYPE = TPrimitiveType("boolean", "Z")
            val BYTE_TYPE = TPrimitiveType("byte", "B")
            val CHAR_TYPE = TPrimitiveType("char", "C")
            val SHORT_TYPE = TPrimitiveType("short", "S")
            val INT_TYPE = TPrimitiveType("int", "I")
            val LONG_TYPE = TPrimitiveType("long", "J")
            val FLOAT_TYPE = TPrimitiveType("float", "F")
            val DOUBLE_TYPE = TPrimitiveType("double", "D")
        }
    }
}