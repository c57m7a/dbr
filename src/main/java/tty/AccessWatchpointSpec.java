package tty;

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

class AccessWatchpointSpec extends WatchpointSpec {

    AccessWatchpointSpec(ReferenceTypeSpec refSpec, String fieldId)
            throws MalformedMemberNameException {
        super(refSpec, fieldId);
    }

    /**
     * The 'refType' is known to match, return the EventRequest.
     */
    @Override
    EventRequest resolveEventRequest(ReferenceType refType)
            throws NoSuchFieldException {
        Field field = refType.fieldByName(fieldId);
        EventRequestManager em = refType.virtualMachine().eventRequestManager();
        EventRequest wp = em.createAccessWatchpointRequest(field);
        wp.setSuspendPolicy(suspendPolicy);
        wp.enable();
        return wp;
    }

    @Override
    public String toString() {
        return MessageOutput.format("watch accesses of",
                new Object[]{refSpec.toString(),
                        fieldId});
    }
}
