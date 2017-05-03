package tty

import com.sun.jdi.ThreadReference
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.*
import com.sun.jdi.request.EventRequest
import com.sun.jdi.request.EventRequestManager
import com.sun.tools.jdi.IntegerTypeImpl
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.cfg.Configuration
import ru.c57m7a.db.*
import ru.c57m7a.db.events.*
import ru.c57m7a.utils.logger
import ru.c57m7a.utils.onEach
import ru.c57m7a.utils.stdInScanner
import java.util.*
import kotlin.collections.ArrayList

class Debugger(sessionFactory: SessionFactory) : EventNotifier, AutoCloseable {
    val session: Session = sessionFactory.openSession()
    val transaction: Transaction = session.beginTransaction()
    val eventHandler = EventHandler(this, true)
    val erm: EventRequestManager = Env.vm().eventRequestManager()
    val threadsStacks = HashMap<ThreadReference, Stack<TMethodInvocationEvent>>()
    private val objectsToPersist = ArrayList<Any>()

    override fun close() {
        eventHandler.shutdown()
        transaction.commit()
        session.close()
    }

    fun persist() {
        objectsToPersist.forEach {
            logger.warn("Saving $it")
            session.persist(it)
        }
    }

    override fun vmStartEvent(e: VMStartEvent) {}

    override fun vmDeathEvent(e: VMDeathEvent) {}

    override fun vmDisconnectEvent(e: VMDisconnectEvent) {}

    override fun threadStartEvent(e: ThreadStartEvent) {
        objectsToPersist += TThreadStartEvent(e)
    }

    override fun threadDeathEvent(e: ThreadDeathEvent) {
        objectsToPersist += TThreadDeathEvent(e)
    }

    override fun classPrepareEvent(e: ClassPrepareEvent) {
        val referenceType = e.referenceType()
        TType.TReferenceType[referenceType]
        referenceType.allFields().forEach { field ->
            onEach(erm.createModificationWatchpointRequest(field), erm.createAccessWatchpointRequest(field)) {
                setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
                enable()
            }
        }
    }

    override fun classUnloadEvent(e: ClassUnloadEvent) {}

    override fun breakpointEvent(e: BreakpointEvent) {
        objectsToPersist += TBreakpointEvent(e)
    }

    override fun fieldWatchEvent(e: WatchpointEvent) {
        objectsToPersist += TFieldEvent[e]
    }

    override fun stepEvent(e: StepEvent) {
        objectsToPersist += TStepEvent(e)
    }

    override fun exceptionEvent(e: ExceptionEvent): Boolean {
        objectsToPersist += TExceptionEvent(e)
        return false
    }

    override fun methodEntryEvent(e: MethodEntryEvent) {
        val entryTime = Date()
        val tMethod = TMethod[e.method()]
        val tMethodInvocation = TMethodInvocationEvent(e).also {
            it.entryTime = entryTime
            tMethod.methodInvocations += it
        }
        val eventThread = e.thread()
        val stack = threadsStacks.getOrPut(eventThread) { Stack() }
        stack += tMethodInvocation

        objectsToPersist += tMethodInvocation
    }

    override fun methodExitEvent(e: MethodExitEvent): Boolean {
        val exitTime = Date()
        val eventThread = e.thread()
        val threadStack = threadsStacks[eventThread]?.takeIf { it.isNotEmpty() } ?: run {
            logger.error("No active methods for thread $eventThread, event $e")
            return false
        }
        val method = TMethod[e.method()]

        val methodInvocation = threadStack.last()
        if (methodInvocation.method !== method) {
            logger.error("$threadsStacks\ncurrent method: $method")
            return false
        } else {
            threadStack.remove(methodInvocation)
            logger.debug("[$eventThread] -= $methodInvocation, all: $threadStack")
        }

        methodInvocation.exitTime = exitTime
        methodInvocation.returnValue = e.returnValue()?.let { TValue[it] }
        return false
    }

    override fun vmInterrupted() {}

    override fun receivedEvent(event: Event) {
        logger.warn("event $event")
    }
}

fun main(args: Array<String>) {
    hibernateConfiguration.buildSessionFactory().use { sessionFactory ->
        initDebugger()
        Debugger(sessionFactory).use { debugger ->
            Env.vm().resume()
            logger.error("press enter to close")
            stdInScanner.nextLine()
            debugger.persist()
        }
    }
}

private val hibernateConfiguration
    get() = Configuration().apply {
        setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
        setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/postgres:5432")
        setProperty("hibernate.connection.username", "nikita")
        setProperty("hibernate.connection.password", "")
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        setProperty("hibernate.hbm2ddl.auto", "create")

        addAnnotatedClass(TField::class.java)
        addAnnotatedClass(TLocalVariable::class.java)
        addAnnotatedClass(TLocation::class.java)
        addAnnotatedClass(TMethod::class.java)
        addAnnotatedClass(TType::class.java)
        addAnnotatedClass(TType.TReferenceType::class.java)
        addAnnotatedClass(TType.TReferenceType.TClassType::class.java)
        addAnnotatedClass(TType.TReferenceType.TArrayType::class.java)
        addAnnotatedClass(TType.TReferenceType.TInterfaceType::class.java)
        addAnnotatedClass(TType.TPrimitiveType::class.java)
        addAnnotatedClass(TValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TBooleanValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TByteValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TCharValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TDoubleValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TFloatValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TIntegerValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TLongValue::class.java)
        addAnnotatedClass(TValue.TPrimitiveValue.TShortValue::class.java)
        addAnnotatedClass(TValue.TObjectReference::class.java)
        addAnnotatedClass(TValue.TObjectReference.TArrayReference::class.java)
        addAnnotatedClass(TValue.TObjectReference.TClassLoaderReference::class.java)
        addAnnotatedClass(TValue.TObjectReference.TClassObjectReference::class.java)
        addAnnotatedClass(TValue.TObjectReference.TStringReference::class.java)
        addAnnotatedClass(TValue.TObjectReference.TThreadReference::class.java)
        addAnnotatedClass(TValue.TObjectReference.TThreadGroupReference::class.java)
        addAnnotatedClass(TBreakpointEvent::class.java)
        addAnnotatedClass(TEvent::class.java)
        addAnnotatedClass(TExceptionEvent::class.java)
        addAnnotatedClass(TStepEvent::class.java)
        addAnnotatedClass(TThreadStartEvent::class.java)
        addAnnotatedClass(TThreadDeathEvent::class.java)
        addAnnotatedClass(TFieldEvent::class.java)
        addAnnotatedClass(TFieldEvent.TFieldAccessEvent::class.java)
        addAnnotatedClass(TFieldEvent.TFieldModificationEvent::class.java)
        addAnnotatedClass(TMethodInvocationEvent::class.java)
    }

private fun initDebugger() {
    MessageOutput.textResources = ResourceBundle.getBundle("tty.TTYResources", Locale.getDefault())
    Env.init("com.sun.jdi.SocketAttach:port=8000,", false, VirtualMachine.TRACE_NONE)

    Env.vm().eventRequestManager().run {
        onEach(
                createThreadStartRequest(),
                createThreadDeathRequest(),
                createMethodExitRequest().apply {
                    addClassExclusionFilter("java.*")
                    addClassExclusionFilter("sun.*")
                },
                //createMonitorContendedEnterRequest(),
//              createMonitorWaitRequest(),
//              createMonitorWaitedRequest(),
                createExceptionRequest(null, true, true),
                //createClassUnloadRequest(),
                createVMDeathRequest()
        ) {
            setSuspendPolicy(EventRequest.SUSPEND_NONE)
            enable()
        }
        createClassPrepareRequest().apply {
            setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
            addClassExclusionFilter("java.*")
            addClassExclusionFilter("sun.*")
            enable()
        }
        createMethodEntryRequest().apply {
            setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
            addClassExclusionFilter("java.*")
            addClassExclusionFilter("sun.*")
            enable()
        }
    }
}