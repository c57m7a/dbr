package ru.c57m7a.db

import com.sun.jdi.LocalVariable
import ru.c57m7a.utils.ObjectCache
import javax.persistence.*

@Entity @Table(name="local_variable")
class TLocalVariable private constructor (localVariable: LocalVariable) {
    @Id @GeneratedValue @Column(name = "local_variable_id") val id = 0

    @Column(name = "name", nullable = false)
    val name: String = localVariable.name()

    @ManyToOne(cascade = arrayOf(CascadeType.ALL), optional = false)
    @JoinColumn(name = "declaring_method_id")
    lateinit var declaringMethod: TMethod

    @ManyToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "type_id", nullable = false)
    val type = TType[localVariable.type()]

    @Column(name = "is_argument", nullable = false)
    val isArgument = localVariable.isArgument

    companion object : ObjectCache<LocalVariable, TLocalVariable>(::TLocalVariable)
}