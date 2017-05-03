package tty;

import com.sun.jdi.event.*;

interface EventNotifier {
    void vmStartEvent(VMStartEvent e);

    void vmDeathEvent(VMDeathEvent e);

    void vmDisconnectEvent(VMDisconnectEvent e);

    void threadStartEvent(ThreadStartEvent e);

    void threadDeathEvent(ThreadDeathEvent e);

    void classPrepareEvent(ClassPrepareEvent e);

    void classUnloadEvent(ClassUnloadEvent e);

    void breakpointEvent(BreakpointEvent e);

    void fieldWatchEvent(WatchpointEvent e);

    void stepEvent(StepEvent e);

    boolean exceptionEvent(ExceptionEvent e);

    void methodEntryEvent(MethodEntryEvent e);

    boolean methodExitEvent(MethodExitEvent e);

    void vmInterrupted();

    void receivedEvent(Event event);
}
