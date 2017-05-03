@file:Suppress("unused")

package tty

import com.sun.jdi.*
import com.sun.jdi.event.*
import com.sun.jdi.request.EventRequest
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

fun main(argv: Array<String>) {
    var cmdLine = ""
    var javaArgs = ""
    var traceFlags = VirtualMachine.TRACE_NONE
    var launchImmediately = false
    var connectSpec: String? = null
    val evaluator = Commands()
    var threadToSelect: String? = null

    MessageOutput.textResources = ResourceBundle.getBundle("ru.c57m7a.debug.tty.TTYResources", Locale.getDefault())

    var i = 0
    l@ while (i < argv.size) {
        val token = argv[i]
        when (token) {
            "-dbgtrace" -> traceFlags = if (i == argv.lastIndex || !Character.isDigit(argv[i + 1][0]))
                VirtualMachine.TRACE_ALL
            else try {
                val flagStr = argv[++i]
                Integer.decode(flagStr).toInt()
            } catch (nfe: NumberFormatException) {
                usageError("dbgtrace flag value must be an integer:", "")
                return
            }

            "-X" -> {
                usageError("Use java minus X to see")
                return
            }
        // Standard VM options passed on
            "-v", "-noasyncgc", "-prof", "-verify", "-noverify", "-verifyremote", "-verbosegc" -> javaArgs = addArgument(javaArgs, token)

            "-tclassic" -> {
                usageError("Classic VM no longer supported.")
                return
            }

            "-tclient" -> // -client must be the first one
                javaArgs = "-client " + javaArgs

            "-tserver" -> // -server must be the first one
                javaArgs = "-server " + javaArgs

            "-sourcepath" -> {
                if (i == argv.lastIndex) {
                    usageError("No sourcepath specified.")
                    return
                }
                Env.setSourcePath(argv[++i])
            }

            "-classpath" -> {
                if (i == argv.lastIndex) {
                    usageError("No classpath specified.")
                    return
                }
                javaArgs = addArgument(javaArgs, token)
                javaArgs = addArgument(javaArgs, argv[++i])
            }

            "-exclude" -> {
                if (i == argv.lastIndex) {
                    usageError("No excludes specified.")
                    return
                }
                Env.setExcludes(argv[++i])
            }

            "-thread" -> {
                if (i == argv.lastIndex) {
                    usageError("No thread abc specified.")
                    return
                }
                threadToSelect = argv[++i]
            }

            "-attach" -> {
                if (connectSpec != null) {
                    usageError("cannot redefine existing connection", token)
                    return
                }
                if (i == argv.lastIndex) {
                    usageError("No attach address specified.")
                    return
                }
                val address = argv[++i]

                /*
                 * -attach is shorthand for one of the reference implementation's
                 * attaching connectors. Use the shared memory attach if it's
                 * available; otherwise, use sockets. Build a connect
                 * specification string based on this decision.
                 */
                connectSpec = if (supportsSharedMemory()) {
                    "com.sun.jdi.SharedMemoryAttach:abc=" + address
                } else {
                    "com.sun.jdi.SocketAttach:" + addressToSocketArgs(address)
                }
            }

            "-listen", "-listenany" -> {
                if (connectSpec != null) {
                    usageError("cannot redefine existing connection", token)
                    return
                }
                val address = if (token == "-listen") {
                    if (i == argv.lastIndex) {
                        usageError("No attach address specified.")
                        return
                    }
                    argv[++i]
                } else {
                    null
                }

                /*
                 * -listen[any] is shorthand for one of the reference implementation's
                 * listening connectors. Use the shared memory listen if it's
                 * available; otherwise, use sockets. Build a connect
                 * specification string based on this decision.
                 */
                if (supportsSharedMemory()) {
                    connectSpec = "com.sun.jdi.SharedMemoryListen:"
                    if (address != null) {
                        connectSpec += "abc=" + address
                    }
                } else {
                    connectSpec = "com.sun.jdi.SocketListen:"
                    if (address != null) {
                        connectSpec += addressToSocketArgs(address)
                    }
                }
            }

            "-launch" -> launchImmediately = true

            "-listconnectors" -> {
                evaluator.commandConnectors(Bootstrap.virtualMachineManager())
                return
            }

            "-connect" -> {
                /*
                 * -connect allows the user to pick the connector
                 * used in bringing up the target VM. This allows
                 * use of connectors other than those in the reference
                 * implementation.
                 */
                if (connectSpec != null) {
                    usageError("cannot redefine existing connection", token)
                    return
                }
                if (i == argv.lastIndex) {
                    usageError("No connect specification.")
                    return
                }
                connectSpec = argv[++i]
            }

            "-help" -> usage()

            "-version" -> {
                evaluator.commandVersion(programName, Bootstrap.virtualMachineManager())
                System.exit(0)
            }

            else -> {
                if (jArgs.any { token.startsWith(it) }) {
                    javaArgs = addArgument(javaArgs, token)
                    continue@l
                }

                if (token.startsWith("-")) {
                    usageError("invalid option", token)
                    return
                }
                // Everything from here is part of the command line
                cmdLine = addArgument("", token)

                while (++i < argv.size) {
                    cmdLine = addArgument(cmdLine, argv[i])
                }
                break@l
            }
        }
        i++
    }

    /*
 * Unless otherwise specified, set the default connect spec.
 */
    /*
 * Here are examples of jdb command lines and how the options
 * are interpreted as arguments to the program being debugged.
 * arg1       arg2
 * ----       ----
 * jdb hello a b       a          b
 * jdb hello "a b"     a b
 * jdb hello a,b       a,b
 * jdb hello a, b      a,         b
 * jdb hello "a, b"    a, b
 * jdb -connect "com.sun.jdi.CommandLineLaunch:main=hello  a,b"   illegal
 * jdb -connect  com.sun.jdi.CommandLineLaunch:main=hello "a,b"   illegal
 * jdb -connect 'com.sun.jdi.CommandLineLaunch:main=hello "a,b"'  arg1 = a,b
 * jdb -connect 'com.sun.jdi.CommandLineLaunch:main=hello "a b"'  arg1 = a b
 * jdb -connect 'com.sun.jdi.CommandLineLaunch:main=hello  a b'   arg1 = a  arg2 = b
 * jdb -connect 'com.sun.jdi.CommandLineLaunch:main=hello "a," b' arg1 = a, arg2 = b
 */
    if (connectSpec == null) {
        connectSpec = "com.sun.jdi.CommandLineLaunch:"
    } else if (!connectSpec.endsWith(",") && !connectSpec.endsWith(":")) {
        connectSpec += ","
    }

    cmdLine = cmdLine.trim { it <= ' ' }
    javaArgs = javaArgs.trim { it <= ' ' }

    if (cmdLine.isNotEmpty()) {
        if (!connectSpec.startsWith("com.sun.jdi.CommandLineLaunch:")) {
            usageError("Cannot specify command line with connector:", connectSpec)
            return
        }
        connectSpec += "main=$cmdLine,"
    }

    if (javaArgs.isNotEmpty()) {
        if (!connectSpec.startsWith("com.sun.jdi.CommandLineLaunch:")) {
            usageError("Cannot specify target vm arguments with connector:", connectSpec)
            return
        }
        connectSpec += "options=$javaArgs,"
    }

    try {
        if (!connectSpec.endsWith(",")) {
            connectSpec += "," // (Bug ID 4285874)
        }
        Env.init(connectSpec, launchImmediately, traceFlags)
        TTY2(threadToSelect)
    } catch (e: Exception) {
        MessageOutput.printException("Internal exception:", e)
    }
}

class TTY2(var threadToSelect: String? = null) : EventNotifier {
    internal var handler: EventHandler? = null

    /**
     * List of Strings to execute at each stop.
     */
    private val monitorCommands = ArrayList<String>()

    private var monitorCount = 0
    private val dateFormatHH = SimpleDateFormat("HH:mm:ss")
    private val dateFormatSS = SimpleDateFormat("ss's'SS'ms'")

    var silentTrace = false

    init {
        MessageOutput.println("Initializing programName", programName)

        if (Env.connection().isOpen && Env.vm().canBeModified()) {
            /*
             * Connection opened on startup. Start event handler
             * immediately, telling it (through arg 2) to stop on the
             * VM start event.
             */
            this.handler = EventHandler(this, true)
        }
        try {
            val reader = BufferedReader(InputStreamReader(System.`in`))

            Thread.currentThread().priority = Thread.NORM_PRIORITY

            threadToSelect?.let {
                val evaluator = Commands()
                val threadInfo = evaluator.doGetThread(it)
                if (threadInfo == null) {
                    MessageOutput.printDirectln("Thread $threadToSelect not found\nAvailiable threads:")
                    evaluator.commandThreads(StringTokenizer(""))
                } else {
                    ThreadInfo.setCurrentThreadInfo(threadInfo)
                }
            }

            /*
             * Read start up files.  This mimics the behavior
             * of gdb which will read both ~/.gdbinit and then
             * ./.gdbinit if they exist.  We have the twist that
             * we allow two different names, so we do this:
             *  if ~/jdb.ini exists,
             *      read it
             *  else if ~/.jdbrc exists,
             *      read it
             *
             *  if ./jdb.ini exists,
             *      if it hasn't been read, read it
             *      It could have been read above because ~ == .
             *      or because of symlinks, ...
             *  else if ./jdbrx exists
             *      if it hasn't been read, read it
             */

            val userHome = System.getProperty("user.home")
            val canonPath = readStartupCommandFile(userHome, "jdb.ini", null) ?: readStartupCommandFile(userHome, ".jdbrc", null)

            val userDir = System.getProperty("user.dir")
            readStartupCommandFile(userDir, "jdb.ini", canonPath) ?: readStartupCommandFile(userDir, ".jdbrc", canonPath) ?: run {
                if (canonPath == null) {
                    MessageOutput.printPrompt()
                }
            }

            var lastLine: String? = null
            while (true) {
                var line = reader.readLine() ?: run {
                    MessageOutput.println("Input stream closed.")
                    "quit"
                }

                if (line.startsWith("!!") && lastLine != null) {
                    line = lastLine + line.substring(2)
                    MessageOutput.printDirectln(line)// Special case: use printDirectln()
                }

                val t = StringTokenizer(line)
                if (t.hasMoreTokens()) {
                    lastLine = line
                    executeCommand(t)
                } else {
                    MessageOutput.printPrompt()
                }
            }
        } catch (e: VMDisconnectedException) {
            handler?.handleDisconnectedException()
        }
    }

    override fun vmStartEvent(se: VMStartEvent) {
        Thread.`yield`()  // fetch output
        MessageOutput.lnprint("VM Started:")
    }

    override fun vmDeathEvent(e: VMDeathEvent) {
    }

    override fun vmDisconnectEvent(e: VMDisconnectEvent) {
    }

    override fun threadStartEvent(e: ThreadStartEvent) {
    }

    override fun threadDeathEvent(e: ThreadDeathEvent) {
    }

    override fun classPrepareEvent(e: ClassPrepareEvent) {
    }

    override fun classUnloadEvent(e: ClassUnloadEvent) {
    }

    override fun breakpointEvent(be: BreakpointEvent) {
        Thread.`yield`()  // fetch output
        MessageOutput.lnprint("Breakpoint hit:")
    }

    override fun fieldWatchEvent(fwe: WatchpointEvent) {
        val field = fwe.field()
        //val obj = fwe.`object`()
        Thread.`yield`()  // fetch output

        if (fwe is ModificationWatchpointEvent) {
            MessageOutput.lnprint("Field access encountered before after", arrayOf(field, fwe.valueCurrent(), fwe.valueToBe()))
        } else {
            MessageOutput.lnprint("Field access encountered", field.toString())
        }
    }

    override fun stepEvent(se: StepEvent) {
        Thread.`yield`()  // fetch output
        MessageOutput.lnprint("Step completed:")
    }

    override fun exceptionEvent(ee: ExceptionEvent): Boolean {
        Thread.`yield`()  // fetch output
        val catchLocation = ee.catchLocation()
        if (catchLocation == null) {
            MessageOutput.lnprint("Exception occurred uncaught", ee.exception().referenceType().name())
        } else {
            MessageOutput.lnprint("Exception occurred caught",
                    arrayOf(ee.exception().referenceType().name(), Commands.locationString(catchLocation)))
        }
        return true
    }

    override fun methodEntryEvent(methodEntryEvent: MethodEntryEvent) {
        Thread.`yield`()  // fetch output

        /*
         * These can be very numerous, so be as efficient as possible.
         * If we are stopping here, then we will see the normal location
         * info printed.
         */
        if (!silentTrace) {
            if (methodEntryEvent.request().suspendPolicy() != EventRequest.SUSPEND_NONE) {
                // We are stopping; the abc will be shown by the normal mechanism
                MessageOutput.lnprint("Method entered:")
            } else {
                // We aren't stopping, show the abc
                MessageOutput.print("Method entered:")
                printLocationOfEvent(methodEntryEvent)
            }
        }
    }

    override fun methodExitEvent(methodExitEvent: MethodExitEvent): Boolean {
        Thread.`yield`()  // fetch output

        /*
         * These can be very numerous, so be as efficient as possible.
         */
        val exitMethod = Env.atExitMethod()
        val meMethod = methodExitEvent.method()

        if (exitMethod != null && exitMethod != meMethod) {
            // We are tracing a specific method, and this isn't it. Keep going.
            return false
        }
        // Either we are not tracing a specific method, or we are
        // and we are exitting that method.

        val suspendPolicy = methodExitEvent.request().suspendPolicy()
        if (suspendPolicy != EventRequest.SUSPEND_NONE) {
            // We will be stopping here, so do a newline
            MessageOutput.println()
        }

        if (silentTrace) {
            return true
        }

        if (Env.vm().canGetMethodReturnValues()) {
            MessageOutput.print("Method exitedValue:", "${methodExitEvent.returnValue()}")
        } else {
            MessageOutput.print("Method exited:")
        }

        if (suspendPolicy == EventRequest.SUSPEND_NONE) {
            // We won't be stopping here, so show the method abc
            printLocationOfEvent(methodExitEvent)
        }
        return true
    }

    override fun vmInterrupted() {
        Thread.`yield`()  // fetch output
        printCurrentLocation()

        monitorCommands.map(::StringTokenizer).forEach {
            it.nextToken() // get rid of monitor number
            executeCommand(it)
        }

        MessageOutput.printPrompt()
    }

    override fun receivedEvent(event: Event) {
    }

    private fun printBaseLocation(threadName: String, loc: Location) {
        MessageOutput.println("location", arrayOf(threadName, Commands.locationString(loc)))
    }

    private fun printCurrentLocation() {
        val threadInfo = ThreadInfo.getCurrentThreadInfo()
        val frame = try {
            threadInfo.currentFrame
        } catch (exc: IncompatibleThreadStateException) {
            MessageOutput.println("<location unavailable>")
            return
        }

        if (frame == null) {
            MessageOutput.println("No frames on the current call stack")
        } else {
            val loc = frame.location()
            printBaseLocation(threadInfo.thread.name(), loc)
            // Output the current source line, if possible
            if (loc.lineNumber() != -1) {
                try {
                    val line = Env.sourceLine(loc, loc.lineNumber())
                    MessageOutput.println("source line number and line", arrayOf(loc.lineNumber(), line))
                } catch (e: IOException) {
                }
            }
        }
        MessageOutput.println()
    }

    private fun printLocationOfEvent(theEvent: LocatableEvent) {
        printBaseLocation(theEvent.thread().name(), theEvent.location())
    }

    internal fun help() {
        MessageOutput.println("zz help text")
    }

    /*
     * Look up the command string in commandList.
     * If found, return the index.
     * If not found, return index < 0
     */
    private fun isCommand(key: String): Int {
        //Reference: binarySearch() in java/util/Arrays.java
        //           Adapted for use with String[][0].
        var low = 0
        var high = commandList.lastIndex
        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midVal = commandList[mid][0]
            val compare = midVal.compareTo(key)
            when {
                compare < 0 -> low = mid + 1
                compare > 0 -> high = mid - 1
                else -> return mid // key found
            }
        }
        return -(low + 1)  // key not found.
    }

    /*
     * Return true if the command is OK when disconnected.
     */
    private fun isDisconnectCmd(ii: Int): Boolean {
        if (ii < 0 || ii >= commandList.size) {
            return false
        }
        return commandList[ii][1] == "y"
    }

    /*
     * Return true if the command is OK when readonly.
     */
    private fun isReadOnlyCmd(ii: Int): Boolean {
        if (ii < 0 || ii >= commandList.size) {
            return false
        }
        return commandList[ii][2] == "y"
    }


    internal fun executeCommand(t: StringTokenizer) {
        val cmd = t.nextToken().toLowerCase()
        /*
         * Anything starting with # is discarded as a no-op or 'comment'.
         */
        if (cmd.startsWith("#")) {
            MessageOutput.printPrompt()
            return
        }

        /*
         * Next check for an integer repetition prefix.  If found,
         * recursively execute cmd that number of times.
         */
        if (Character.isDigit(cmd[0]) && t.hasMoreTokens()) {
            try {
                val subcom = t.nextToken("")
                repeat(Integer.parseInt(cmd)) {
                    executeCommand(StringTokenizer(subcom))
                }
            } catch (exc: NumberFormatException) {
                MessageOutput.println("Unrecognized command.  Try help...", cmd)
                MessageOutput.printPrompt()
            }
            return
        }

        // Normally, prompt for the next command after this one is done
        var showPrompt = true

        val commandNumber = isCommand(cmd)
        /*
         * Check for an unknown command
         */
        when {
            commandNumber < 0 ->
                MessageOutput.println("Unrecognized command.  Try help...", cmd)
            !Env.connection().isOpen && !isDisconnectCmd(commandNumber) ->
                MessageOutput.println("Command not valid until the VM is started with the run command", cmd)
            Env.connection().isOpen && !Env.vm().canBeModified() && !isReadOnlyCmd(commandNumber) ->
                MessageOutput.println("Command is not supported on a read-only VM connection", cmd)
            else -> try {
                val evaluator = Commands()
                with(evaluator) {
                    when (cmd) {
                        "print" -> {
                            commandPrint(t, false)
                            showPrompt = false        // asynchronous command
                        }
                        "eval" -> {
                            commandPrint(t, false)
                            showPrompt = false        // asynchronous command
                        }
                        "set" -> {
                            commandSet(t)
                            showPrompt = false        // asynchronous command
                        }
                        "dump" -> {
                            commandPrint(t, true)
                            showPrompt = false        // asynchronous command
                        }
                        "locals" -> commandLocals()
                        "classes" -> commandClasses()
                        "class" -> commandClass(t)
                        "connectors" -> commandConnectors(Bootstrap.virtualMachineManager())
                        "methods" -> commandMethods(t)
                        "fields" -> commandFields(t)
                        "threads" -> commandThreads(t)
                        "thread" -> commandThread(t)
                        "suspend" -> commandSuspend(t)
                        "resume" -> commandResume(t)
                        "cont" -> commandCont()
                        "threadgroups" -> commandThreadGroups()
                        "threadgroup" -> commandThreadGroup(t)
                        "catch" -> commandCatchException(t)
                        "ignore" -> commandIgnoreException(t)
                        "step" -> commandStep(t)
                        "stepi" -> commandStepi()
                        "next" -> commandNext()
                        "kill" -> commandKill(t)
                        "interrupt" -> commandInterrupt(t)
                        "trace" -> {
                            silentTrace = commandTrace(t)
                        }
                        "untrace" -> commandUntrace(t)
                        "where" -> commandWhere(t, false)
                        "wherei" -> commandWhere(t, true)
                        "up" -> commandUp(t)
                        "down" -> commandDown(t)
                        "load" -> commandLoad(t)
                        "run" -> {
                            commandRun(t)
                            /*
                             * Fire up an event handler, if the connection was just
                             * opened. Since this was done from the run command
                             * we don't stop the VM on its VM start event (so
                             * arg 2 is false).
                             */
                            if (handler == null && Env.connection().isOpen) {
                                handler = EventHandler(this@TTY2, false)
                            }
                        }
                        "memory" -> commandMemory()
                        "gc" -> commandGC()
                        "stop" -> commandStop(t)
                        "clear" -> commandClear(t)
                        "watch" -> commandWatch(t)
                        "unwatch" -> commandUnwatch(t)
                        "list" -> commandList(t)
                        "lines" -> commandLines(t) // Undocumented command: useful for testing.
                        "classpath" -> commandClasspath(t)
                        "use", "sourcepath" -> commandUse(t)
                        "monitor" -> monitorCommand(t)
                        "unmonitor" -> unmonitorCommand(t)
                        "lock" -> {
                            commandLock(t)
                            showPrompt = false        // asynchronous command
                        }
                        "threadlocks" -> commandThreadlocks(t)
                        "disablegc" -> {
                            commandDisableGC(t)
                            showPrompt = false        // asynchronous command
                        }
                        "enablegc" -> {
                            commandEnableGC(t)
                            showPrompt = false        // asynchronous command
                        }
                        "save" -> { // Undocumented command: useful for testing.
                            commandSave(t)
                            showPrompt = false        // asynchronous command
                        }
                        "bytecodes" -> // Undocumented command: useful for testing.
                            commandBytecodes(t)
                        "redefine" -> commandRedefine(t)
                        "pop" -> commandPopFrames(t, false)
                        "reenter" -> commandPopFrames(t, true)
                        "extension" -> commandExtension(t)
                        "exclude" -> commandExclude(t)
                        "read" -> readCommand(t)
                        "help", "?" -> help()
                        "version" -> commandVersion(programName, Bootstrap.virtualMachineManager())
                        "quit", "exit" -> {
                            handler?.shutdown()
                            Env.shutdown()
                        }
                        else -> MessageOutput.println("Unrecognized command.  Try help...", cmd)
                    }
                }
            } catch (rovm: VMCannotBeModifiedException) {
                MessageOutput.println("Command is not supported on a read-only VM connection", cmd)
            } catch (uoe: UnsupportedOperationException) {
                MessageOutput.println("Command is not supported on the target VM", cmd)
            } catch (vmnse: VMNotConnectedException) {
                MessageOutput.println("Command not valid until the VM is started with the run command", cmd)
            } catch (e: Exception) {
                MessageOutput.printException("Internal exception:", e)
            }
        }
        if (showPrompt) {
            MessageOutput.printPrompt()
        }
    }

    /*
     * Maintain a list of commands to execute each exitTime the VM is suspended.
     */
    internal fun monitorCommand(t: StringTokenizer) {
        if (t.hasMoreTokens()) {
            ++monitorCount
            monitorCommands.add("$monitorCount : ${t.nextToken("")}")
        } else {
            monitorCommands.forEach {
                MessageOutput.printDirectln(it) // Special case: use printDirectln() }
            }
        }
    }

    internal fun unmonitorCommand(t: StringTokenizer) {
        if (t.hasMoreTokens()) {
            val token = t.nextToken()
            try {
                Integer.parseInt(token)
            } catch (exc: NumberFormatException) {
                MessageOutput.println("Not a monitor number:", token)
                return
            }

            val str = token + ":"
            for (cmd in monitorCommands) {
                val ct = StringTokenizer(cmd)
                if (ct.nextToken() == str) {
                    monitorCommands.remove(cmd)
                    MessageOutput.println("Unmonitoring", cmd)
                    return
                }
            }
            MessageOutput.println("No monitor numbered:", token)
        } else {
            MessageOutput.println("Usage: unmonitor <monitor#>")
        }
    }


    internal fun readCommand(t: StringTokenizer) {
        if (t.hasMoreTokens()) {
            val cmdfname = t.nextToken()
            if (!readCommandFile(File(cmdfname))) {
                MessageOutput.println("Could not open:", cmdfname)
            }
        } else {
            MessageOutput.println("Usage: read <command-filename>")
        }
    }

    /**
     * Read and execute a command file.  Return true if the file was read
     * else false;
     */
    internal fun readCommandFile(f: File): Boolean {
        var inFile: BufferedReader? = null
        try {
            if (f.canRead()) {
                // Process initial commands.
                MessageOutput.println("*** Reading commands from", f.path)
                inFile = BufferedReader(FileReader(f))
                var ln = inFile.readLine()
                while (ln != null) {
                    val t = StringTokenizer(ln)
                    if (t.hasMoreTokens()) {
                        executeCommand(t)
                    }
                    ln = inFile.readLine()
                }
            }
        } catch (e: IOException) {
        } finally {
            inFile?.close()
        }
        return inFile != null
    }

    /**
     * Try to read commands from dir/fname, unless
     * the canonical path passed in is the same as that
     * for dir/fname.
     * Return null if that file doesn't exist,
     * else return the canonical path of that file.
     */
    internal fun readStartupCommandFile(dir: String, fname: String, canonPath: String?): String? {
        val dotInitFile = File(dir, fname)
        if (!dotInitFile.exists()) {
            return null
        }

        val myCanonFile = try {
            dotInitFile.canonicalPath
        } catch (e: IOException) {
            MessageOutput.println("Could not open:", dotInitFile.path)
            null
        }

        if (canonPath == null || canonPath != myCanonFile) {
            if (!readCommandFile(dotInitFile)) {
                MessageOutput.println("Could not open:", dotInitFile.path)
            }
        }
        return myCanonFile
    }
}

private val programName = "jdb"

private val jArgs = arrayOf("-v:", "-verbose", "-D", "-X", "-ms", "-mx", "-ss", "-oss")

private val commandList = arrayOf(
        //<editor-fold desc="">
        /*
 * NOTE: this list must be kept sorted in ascending ASCII
 *       order by element [0].  Ref: isCommand() below.
 *
 * Command      OK when        OK when
 *  abc      disconnected?   readonly?
 *------------------------------------
 */
        arrayOf("!!", "n", "y"),
        arrayOf("?", "y", "y"),
        arrayOf("bytecodes", "n", "y"),
        arrayOf("catch", "y", "n"),
        arrayOf("class", "n", "y"),
        arrayOf("classes", "n", "y"),
        arrayOf("classpath", "n", "y"),
        arrayOf("clear", "y", "n"),
        arrayOf("connectors", "y", "y"),
        arrayOf("cont", "n", "n"),
        arrayOf("disablegc", "n", "n"),
        arrayOf("down", "n", "y"),
        arrayOf("dump", "n", "y"),
        arrayOf("enablegc", "n", "n"),
        arrayOf("eval", "n", "y"),
        arrayOf("exclude", "y", "n"),
        arrayOf("exit", "y", "y"),
        arrayOf("extension", "n", "y"),
        arrayOf("fields", "n", "y"),
        arrayOf("gc", "n", "n"),
        arrayOf("help", "y", "y"),
        arrayOf("ignore", "y", "n"),
        arrayOf("interrupt", "n", "n"),
        arrayOf("kill", "n", "n"),
        arrayOf("lines", "n", "y"),
        arrayOf("list", "n", "y"),
        arrayOf("load", "n", "y"),
        arrayOf("locals", "n", "y"),
        arrayOf("lock", "n", "n"),
        arrayOf("memory", "n", "y"),
        arrayOf("methods", "n", "y"),
        arrayOf("monitor", "n", "n"),
        arrayOf("next", "n", "n"),
        arrayOf("pop", "n", "n"),
        arrayOf("print", "n", "y"),
        arrayOf("quit", "y", "y"),
        arrayOf("read", "y", "y"),
        arrayOf("redefine", "n", "n"),
        arrayOf("reenter", "n", "n"),
        arrayOf("resume", "n", "n"),
        arrayOf("run", "y", "n"),
        arrayOf("save", "n", "n"),
        arrayOf("set", "n", "n"),
        arrayOf("sourcepath", "y", "y"),
        arrayOf("step", "n", "n"),
        arrayOf("stepi", "n", "n"),
        arrayOf("stop", "y", "n"),
        arrayOf("suspend", "n", "n"),
        arrayOf("thread", "n", "y"),
        arrayOf("threadgroup", "n", "y"),
        arrayOf("threadgroups", "n", "y"),
        arrayOf("threadlocks", "n", "y"),
        arrayOf("threads", "n", "y"),
        arrayOf("trace", "n", "n"),
        arrayOf("unmonitor", "n", "n"),
        arrayOf("untrace", "n", "n"),
        arrayOf("unwatch", "y", "n"),
        arrayOf("up", "n", "y"),
        arrayOf("use", "y", "y"),
        arrayOf("version", "y", "y"),
        arrayOf("watch", "y", "n"),
        arrayOf("where", "n", "y"),
        arrayOf("wherei", "n", "y")
        //</editor-fold>))
)

private fun usage() {
    MessageOutput.println("zz usage text", arrayOf(programName, File.pathSeparator))
    System.exit(1)
}

internal fun usageError(messageKey: String) {
    MessageOutput.println(messageKey)
    MessageOutput.println()
    usage()
}

internal fun usageError(messageKey: String, argument: String) {
    MessageOutput.println(messageKey, argument)
    MessageOutput.println()
    usage()
}

private fun supportsSharedMemory(): Boolean = Bootstrap
        .virtualMachineManager()
        .allConnectors()
        .mapNotNull { it.transport() }
        .any { it.name() == "dt_shmem" }

private fun addressToSocketArgs(address: String): String {
    val index = address.indexOf(':')
    return if (index != -1) {
        val host = address.take(index)
        val port = address.drop(index + 1)
        "hostname=$host,port=$port"
    } else {
        "port=$address"
    }
}

private fun addArgument(string: String, argument: String): String {
    return if (argument.any { it == ' ' || it == ',' }) {
        // Quotes were stripped out for this argument, add them back.
        buildString {
            append('"')
            argument.forEach {
                if (it == '"') {
                    append('\\')
                }
                append(it)
            }
            append("\" ")
        }
    } else {
        "$string$argument "
    }
}