package ru.c57m7a.db

import com.sun.jdi.*
import ru.c57m7a.db.TType.TReferenceType
import ru.c57m7a.db.TValue.*
import ru.c57m7a.db.TValue.TObjectReference.TThreadReference.*
import ru.c57m7a.db.TValue.TObjectReference.TThreadReference.Companion
import ru.c57m7a.db.events.TMethodInvocationEvent
import ru.c57m7a.utils.ObjectCache
import ru.c57m7a.utils.tryOrNull
import java.util.*
import javax.persistence.*

@Entity @Table(name = "method")
class TMethod private constructor(method: Method) {
    @Id @GeneratedValue @Column(name = "method_id") val id = 0

    /* TypeComponent */
    @Column(name = "name", nullable = false)
    val name: String = method.name()
    
    @Column(name = "signature", nullable = false)
    val signature: String = method.signature()
    
    @Column(name = "generic_signature", nullable = true)
    val genericSignature: String? = method.genericSignature()

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "declaring_reference_type_id", nullable = false)
    var declaringType: TReferenceType? = null

    @Column(name = "is_static", nullable = false)
    val isStatic = method.isStatic
    
    @Column(name = "is_final", nullable = false)
    val isFinal = method.isFinal
    
    @Column(name = "is_synthetic", nullable = false)
    val isSynthetic = method.isSynthetic

    /* Locatable */
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "location_id", nullable = false)
    lateinit var location: TLocation

    /* Method */
    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "return_type_id", nullable = false)
    val returnType = tryOrNull<TType, ClassNotLoadedException> { TType[method.returnType()] }

    @Column(name = "is_abstract", nullable = false)
    val isAbstract = method.isAbstract

    @Column(name = "is_synchronized", nullable = false)
    val isSynchronized = method.isSynchronized

    @Column(name = "is_native", nullable = false)
    val isNative = method.isNative

    @Column(name = "is_varargs", nullable = false)
    val isVarArgs = method.isVarArgs

    @Column(name = "is_bridge", nullable = false)
    val isBridge = method.isBridge

    @Column(name = "is_constructor")
    val isConstructor = method.isConstructor

    @Column(name = "is_static_initializer")
    val isStaticInitializer = method.isStaticInitializer

    @Column(name = "is_obsolete")
    val isObsolete = method.isObsolete

    @OneToMany(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "variables_id")
    val variables = tryOrNull<List<TLocalVariable>, AbsentInformationException> {
        method.variables().map { TLocalVariable.Companion[it].also { it.declaringMethod = this } }
    }

    @OneToMany(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "arguments_id")
    val arguments = tryOrNull<List<TLocalVariable>, AbsentInformationException> {
        method.arguments().map { TLocalVariable.Companion[it] }
    }

    @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "method")
    val methodInvocations = mutableListOf<TMethodInvocationEvent>()

    companion object : ObjectCache<Method, TMethod>(::TMethod)

/*
    override fun toString() = "TMethod(id=$id, name='$name', signature='$signature', genericSignature=$genericSignature," +
            " declaringType=$declaringType, " + "isStatic=$isStatic, isFinal=$isFinal, isSynthetic=$isSynthetic, " +
            "location=$location, returnType=$returnType, isAbstract=$isAbstract, isSynchronized=$isSynchronized, " +
            "isNative=$isNative, isVarArgs=$isVarArgs, isBridge=$isBridge, isConstructor=$isConstructor, " +
            "isStaticInitializer=$isStaticInitializer, isObsolete=$isObsolete, variables=$variables, " +
            "arguments=$arguments, methodInvocationEvents=$methodInvocationEvents)"
*/
}