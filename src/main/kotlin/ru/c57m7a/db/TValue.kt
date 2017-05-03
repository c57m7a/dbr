package ru.c57m7a.db

import com.sun.jdi.*
import ru.c57m7a.db.TType.TPrimitiveType
import ru.c57m7a.db.TType.TReferenceType
import ru.c57m7a.db.events.TMethodInvocationEvent
import ru.c57m7a.utils.ObjectCache
import java.util.*
import javax.persistence.*

@Entity @Table(name = "value")
@Inheritance(strategy = InheritanceType.JOINED)
open class TValue private constructor(
        @ManyToOne(cascade = arrayOf(CascadeType.ALL))
        @JoinColumn(name = "type_id")
        val type: TType
) {
    @Id @GeneratedValue @Column(name = "value_id") val id = 0

    sealed class TPrimitiveValue(primitiveType: TPrimitiveType) : TValue(primitiveType) {
        @Entity @Table(name = "int_value")
        @PrimaryKeyJoinColumn(name = "int_value_id", referencedColumnName = "value_id")
        class TIntegerValue private constructor(intValue: IntegerValue) : TPrimitiveValue(TPrimitiveType.INT_TYPE) {
            companion object : ObjectCache<IntegerValue, TIntegerValue>(::TIntegerValue)

            @Column val value: Int = intValue.value()
        }

        @Entity @Table(name = "short_value")
        @PrimaryKeyJoinColumn(name = "short_value_id", referencedColumnName = "value_id")
        class TShortValue private constructor(shortValue: ShortValue) : TPrimitiveValue(TPrimitiveType.SHORT_TYPE) {
            companion object : ObjectCache<ShortValue, TShortValue>(::TShortValue)

            @Column val value: Short = shortValue.value()
        }

        @Entity @Table(name = "long_value")
        @PrimaryKeyJoinColumn(name = "long_value_id", referencedColumnName = "value_id")
        class TLongValue private constructor(longValue: LongValue) : TPrimitiveValue(TPrimitiveType.LONG_TYPE) {
            companion object : ObjectCache<LongValue, TLongValue>(::TLongValue)

            @Column val value: Long = longValue.value()
        }

        @Entity @Table(name = "byte_value")
        @PrimaryKeyJoinColumn(name = "byte_value_id", referencedColumnName = "value_id")
        class TByteValue private constructor(byteValue: ByteValue) : TPrimitiveValue(TPrimitiveType.BYTE_TYPE) {
            companion object : ObjectCache<ByteValue, TByteValue>(::TByteValue)

            @Column val value: Byte = byteValue.value()
        }

        @Entity @Table(name = "boolean_value")
        @PrimaryKeyJoinColumn(name = "boolean_value_id", referencedColumnName = "value_id")
        class TBooleanValue private constructor(booleanValue: BooleanValue) : TPrimitiveValue(TPrimitiveType.BOOLEAN_TYPE) {
            companion object : ObjectCache<BooleanValue, TBooleanValue>(::TBooleanValue)

            @Column val value: Boolean = booleanValue.value()
        }

        @Entity @Table(name = "char_value")
        @PrimaryKeyJoinColumn(name = "char_value_id", referencedColumnName = "value_id")
        class TCharValue private constructor(charValue: CharValue) : TPrimitiveValue(TPrimitiveType.CHAR_TYPE) {
            companion object : ObjectCache<CharValue, TCharValue>(::TCharValue)

            @Column val value: Char = charValue.value()
        }

        @Entity @Table(name = "float_value")
        @PrimaryKeyJoinColumn(name = "float_value_id", referencedColumnName = "value_id")
        class TFloatValue private constructor(floatValue: FloatValue) : TPrimitiveValue(TPrimitiveType.FLOAT_TYPE) {
            companion object : ObjectCache<FloatValue, TFloatValue>(::TFloatValue)

            @Column val value: Float = floatValue.value()
        }

        @Entity @Table(name = "double_value")
        @PrimaryKeyJoinColumn(name = "double_value_id", referencedColumnName = "value_id")
        class TDoubleValue private constructor(doubleValue: DoubleValue) : TPrimitiveValue(TPrimitiveType.DOUBLE_TYPE) {
            companion object : ObjectCache<DoubleValue, TDoubleValue>(::TDoubleValue)

            @Column val value: Double = doubleValue.value()
        }

        companion object {
            operator fun get(primitiveValue: PrimitiveValue) = when (primitiveValue) {
                is IntegerValue -> TIntegerValue[primitiveValue]
                is ShortValue -> TShortValue[primitiveValue]
                is LongValue -> TLongValue[primitiveValue]
                is ByteValue -> TByteValue[primitiveValue]
                is BooleanValue -> TBooleanValue[primitiveValue]
                is CharValue -> TCharValue[primitiveValue]
                is FloatValue -> TFloatValue[primitiveValue]
                is DoubleValue -> TDoubleValue[primitiveValue]
                else -> throw ClassCastException("Unknown primitive type")
            }
        }
    }

    @Entity @Table(name = "obj_ref")
    @PrimaryKeyJoinColumn(name = "obj_ref_id", referencedColumnName = "value_id")
    open class TObjectReference private constructor(objectReference: ObjectReference)
        : TValue(TReferenceType[objectReference.referenceType()]) {

        @Column(name = "unique_id")
        val uniqueID = objectReference.uniqueID()

        @Entity @Table(name = "thread_group_ref")
        @PrimaryKeyJoinColumn(name = "thread_group_ref_id", referencedColumnName = "obj_ref_id")
        class TThreadGroupReference private constructor(threadGroupReference: ThreadGroupReference) : TObjectReference(threadGroupReference) {
            companion object : ObjectCache<ThreadGroupReference, TThreadGroupReference>(::TThreadGroupReference)

            @Column(name = "name")
            val name: String = threadGroupReference.name()

            @ManyToOne(cascade = arrayOf(CascadeType.ALL))
            @JoinColumn(name = "parent")
            var parent: TThreadGroupReference? = null

            @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "parent")
            val threadGroups = threadGroupReference.threadGroups().map { TThreadGroupReference[it].also { it.parent = this } }

            @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "threadGroup")
            val threads = threadGroupReference.threads().map { TThreadReference[it].also { it.threadGroup = this } }
        }

        @Entity @Table(name = "array_ref")
        @PrimaryKeyJoinColumn(name = "array_ref_id", referencedColumnName = "obj_ref_id")
        class TArrayReference private constructor(arrayReference: ArrayReference) : TObjectReference(arrayReference) {
            companion object : ObjectCache<ArrayReference, TArrayReference>(::TArrayReference)

            @Column(name = "length")
            val length = arrayReference.length()

            @ManyToMany(cascade = arrayOf(CascadeType.ALL)) @JoinTable(
                    name = "array_ref__value",
                    joinColumns = arrayOf(JoinColumn(name = "array_ref_id")),
                    inverseJoinColumns = arrayOf(JoinColumn(name = "value_id"))
            )
            val values = arrayReference.values.map { TValue[it] }
        }

        @Entity @Table(name = "class_obj_ref")
        @PrimaryKeyJoinColumn(name = "class_obj_ref_id", referencedColumnName = "obj_ref_id")
        class TClassObjectReference private constructor(classObjectReference: ClassObjectReference) : TObjectReference(classObjectReference) {
            companion object : ObjectCache<ClassObjectReference, TClassObjectReference>(::TClassObjectReference)

            @OneToOne(cascade = arrayOf(CascadeType.ALL))
            @JoinColumn(name = "reflected_type_reference_type_id")
            val reflectedType = TReferenceType[classObjectReference.reflectedType()].also { it.classObject = this }
        }

        @Entity @Table(name = "string_ref")
        @PrimaryKeyJoinColumn(name = "string_ref_id", referencedColumnName = "obj_ref_id")
        class TStringReference private constructor(stringReference: StringReference) : TObjectReference(stringReference) {
            companion object : ObjectCache<StringReference, TStringReference>(::TStringReference)

            @Column(name = "value", columnDefinition = "TEXT")
            val value: String = stringReference.value()
        }

        @Entity @Table(name = "thread_ref")
        @PrimaryKeyJoinColumn(name = "thread_ref_id", referencedColumnName = "obj_ref_id")
        class TThreadReference private constructor(threadReference: ThreadReference) : TObjectReference(threadReference) {
            @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "thread")
            val methodInvocationEvents: MutableList<TMethodInvocationEvent> = LinkedList()

            @Column(name = "name")
            val name: String = threadReference.name()

            @ManyToOne(cascade = arrayOf(CascadeType.ALL))
            var threadGroup: TThreadGroupReference? = null

            companion object : ObjectCache<ThreadReference, TThreadReference>(::TThreadReference)
        }

        @Entity @Table(name = "class_loader_ref")
        @PrimaryKeyJoinColumn(name = "class_loader_ref_id", referencedColumnName = "obj_ref_id")
        class TClassLoaderReference private constructor(classLoaderReference: ClassLoaderReference) : TObjectReference(classLoaderReference) {
            companion object : ObjectCache<ClassLoaderReference, TClassLoaderReference>(::TClassLoaderReference)

            @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "classLoader")
            val definedClasses = classLoaderReference.definedClasses().map { TReferenceType[it].also { it.classLoader = this } }
        }

        companion object {
            val objectCache = ObjectCache(::TObjectReference)

            operator fun get(objectReference: ObjectReference) = when (objectReference) {
                is ThreadGroupReference -> TThreadGroupReference[objectReference]
                is ArrayReference -> TArrayReference[objectReference]
                is ClassObjectReference -> TClassObjectReference[objectReference]
                is StringReference -> TStringReference[objectReference]
                is ThreadReference -> TThreadReference[objectReference]
                is ClassLoaderReference -> TClassLoaderReference[objectReference]
                else -> objectCache[objectReference]
            }
        }
    }

    companion object {
        val voidValue = TValue(TType.VOID_TYPE)

        operator fun get(value: Value) = when (value) {
            is PrimitiveValue -> TValue.TPrimitiveValue[value]
            is ObjectReference -> TValue.TObjectReference[value]
            is VoidValue -> voidValue
            else -> throw ClassCastException("Unknown value type ${value::class.qualifiedName}")
        }
    }
}