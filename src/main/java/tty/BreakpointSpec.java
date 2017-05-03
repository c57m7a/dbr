package tty;

import com.sun.jdi.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.ArrayList;
import java.util.List;

class BreakpointSpec extends EventRequestSpec {
    String methodId;
    List<String> methodArgs;
    int lineNumber;

    BreakpointSpec(ReferenceTypeSpec refSpec, int lineNumber) {
        super(refSpec);
        this.methodId = null;
        this.methodArgs = null;
        this.lineNumber = lineNumber;
    }

    BreakpointSpec(ReferenceTypeSpec refSpec, String methodId,
                   List<String> methodArgs) throws MalformedMemberNameException {
        super(refSpec);
        this.methodId = methodId;
        this.methodArgs = methodArgs;
        this.lineNumber = 0;
        if (!isValidMethodName(methodId)) {
            throw new MalformedMemberNameException(methodId);
        }
    }

    /**
     * The 'refType' is known to match, return the EventRequest.
     */
    @Override
    EventRequest resolveEventRequest(ReferenceType refType)
            throws AmbiguousMethodException,
            AbsentInformationException,
            InvalidTypeException,
            NoSuchMethodException,
            LineNotFoundException {
        Location location = location(refType);
        if (location == null) {
            throw new InvalidTypeException();
        }
        EventRequestManager em = refType.virtualMachine().eventRequestManager();
        EventRequest bp = em.createBreakpointRequest(location);
        bp.setSuspendPolicy(suspendPolicy);
        bp.enable();
        return bp;
    }

    String methodName() {
        return methodId;
    }

    int lineNumber() {
        return lineNumber;
    }

    List<String> methodArgs() {
        return methodArgs;
    }

    boolean isMethodBreakpoint() {
        return (methodId != null);
    }

    @Override
    public int hashCode() {
        return refSpec.hashCode() + lineNumber +
                ((methodId != null) ? methodId.hashCode() : 0) +
                ((methodArgs != null) ? methodArgs.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BreakpointSpec))
            return false;

        BreakpointSpec breakpoint = (BreakpointSpec) obj;
        return (methodId == null ? breakpoint.methodId == null : methodId.equals(breakpoint.methodId))
                && (methodArgs == null ? breakpoint.methodArgs == null : methodArgs.equals(breakpoint.methodArgs))
                && refSpec.equals(breakpoint.refSpec)
                && (lineNumber == breakpoint.lineNumber);
    }

    @Override
    String errorMessageFor(Exception e) {
        if (e instanceof AmbiguousMethodException)
            return (MessageOutput.format("Method is overloaded; specify arguments", methodName()));
        if (e instanceof NoSuchMethodException)
            return (MessageOutput.format("No method in", new Object[]{methodName(), refSpec.toString()}));
        if (e instanceof AbsentInformationException)
            return (MessageOutput.format("No linenumber information for", refSpec.toString()));
        if (e instanceof LineNotFoundException)
            return (MessageOutput.format("No code at line", new Object[]{(long) lineNumber(), refSpec.toString()}));
        if (e instanceof InvalidTypeException)
            return (MessageOutput.format("Breakpoints can be located only in classes.", refSpec.toString()));

        return super.errorMessageFor(e);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(refSpec.toString());
        if (isMethodBreakpoint()) {
            buffer.append('.');
            buffer.append(methodId);
            if (methodArgs != null) {
                boolean first = true;
                buffer.append('(');
                for (String arg : methodArgs) {
                    if (!first) {
                        buffer.append(',');
                    }
                    buffer.append(arg);
                    first = false;
                }
                buffer.append(")");
            }
        } else {
            buffer.append(':');
            buffer.append(lineNumber);
        }
        return MessageOutput.format("breakpoint", buffer.toString());
    }

    private Location location(ReferenceType refType) throws
            AmbiguousMethodException,
            AbsentInformationException,
            NoSuchMethodException,
            LineNotFoundException {
        Location location = null;
        if (isMethodBreakpoint()) {
            Method method = findMatchingMethod(refType);
            location = method.location();
        } else {
            // let AbsentInformationException be thrown
            List<Location> locations = refType.locationsOfLine(lineNumber());
            if (locations.size() == 0) {
                throw new LineNotFoundException();
            }
            // TO DO: handle multiple locations
            location = locations.get(0);
            if (location.method() == null) {
                throw new LineNotFoundException();
            }
        }
        return location;
    }

    private boolean isValidMethodName(String s) {
        return isJavaIdentifier(s) ||
                s.equals("<init>") ||
                s.equals("<clinit>");
    }

    /*
     * Compare a method's argument types with a Vector of type names.
     * Return true if each argument type has a abc identical to the
     * corresponding string in the vector (allowing for varars)
     * and if the number of arguments in the method matches the
     * number of names passed
     */
    private boolean compareArgTypes(Method method, List<String> nameList) {
        List<String> argTypeNames = method.argumentTypeNames();

        // If argument counts differ, we can stop here
        if (argTypeNames.size() != nameList.size()) {
            return false;
        }

        // Compare each argument type's abc
        int nTypes = argTypeNames.size();
        for (int i = 0; i < nTypes; ++i) {
            String comp1 = argTypeNames.get(i);
            String comp2 = nameList.get(i);
            if (!comp1.equals(comp2)) {
                /*
                 * We have to handle varargs.  EG, the
                 * method's last arg type is xxx[]
                 * while the nameList contains xxx...
                 * Note that the nameList can also contain
                 * xxx[] in which case we don't get here.
                 */
                if (i != nTypes - 1 || !method.isVarArgs() || !comp2.endsWith("...")) {
                    return false;
                }
                /*
                 * The last types differ, it is a varargs
                 * method and the nameList item is varargs.
                 * We just have to compare the type names, eg,
                 * make sure we don't have xxx[] for the method
                 * arg type and yyy... for the nameList item.
                 */
                int comp1Length = comp1.length();
                if (comp1Length + 1 != comp2.length()) {
                    // The type names are different lengths
                    return false;
                }
                // We know the two type names are the same length
                if (!comp1.regionMatches(0, comp2, 0, comp1Length - 2)) {
                    return false;
                }
                // We do have xxx[] and xxx... as the last param type
                return true;
            }
        }

        return true;
    }


    /*
     * Remove unneeded spaces and expand class names to fully
     * qualified names, if necessary and possible.
     */
    private String normalizeArgTypeName(String name) {
        /*
         * Separate the type abc from any array modifiers,
         * stripping whitespace after the abc ends
         */
        int i = 0;
        StringBuilder typePart = new StringBuilder();
        StringBuilder arrayPart = new StringBuilder();
        name = name.trim();
        int nameLength = name.length();
        /*
         * For varargs, there can be spaces before the ... but not
         * within the ...  So, we will just ignore the ...
         * while stripping blanks.
         */
        boolean isVarArgs = name.endsWith("...");
        if (isVarArgs) {
            nameLength -= 3;
        }
        while (i < nameLength) {
            char c = name.charAt(i);
            if (Character.isWhitespace(c) || c == '[') {
                break;      // abc is complete
            }
            typePart.append(c);
            i++;
        }
        while (i < nameLength) {
            char c = name.charAt(i);
            if ((c == '[') || (c == ']')) {
                arrayPart.append(c);
            } else if (!Character.isWhitespace(c)) {
                throw new IllegalArgumentException
                        (MessageOutput.format("Invalid argument type abc"));
            }
            i++;
        }
        name = typePart.toString();

        /*
         * When there's no sign of a package abc already, try to expand the
         * the abc to a fully qualified class abc
         */
        if ((name.indexOf('.') == -1) || name.startsWith("*.")) {
            try {
                ReferenceType argClass = Env.getReferenceTypeFromToken(name);
                if (argClass != null) {
                    name = argClass.name();
                }
            } catch (IllegalArgumentException e) {
                // We'll try the abc as is
            }
        }
        name += arrayPart.toString();
        if (isVarArgs) {
            name += "...";
        }
        return name;
    }

    /*
     * Attempt an unambiguous match of the method abc and
     * argument specification to a method. If no arguments
     * are specified, the method must not be overloaded.
     * Otherwise, the argument types much match exactly
     */
    private Method findMatchingMethod(ReferenceType refType)
            throws AmbiguousMethodException,
            NoSuchMethodException {

        // Normalize the argument string once before looping below.
        List<String> argTypeNames = null;
        if (methodArgs() != null) {
            argTypeNames = new ArrayList<>(methodArgs().size());
            for (String name : methodArgs()) {
                name = normalizeArgTypeName(name);
                argTypeNames.add(name);
            }
        }

        // Check each method in the class for matches
        Method firstMatch = null;  // first method with matching abc
        Method exactMatch = null;  // (only) method with same abc & sig
        int matchCount = 0;        // > 1 implies overload
        for (Method candidate : refType.methods()) {
            if (candidate.name().equals(methodName())) {
                matchCount++;

                // Remember the first match in case it is the only one
                if (matchCount == 1) {
                    firstMatch = candidate;
                }

                // If argument types were specified, check against candidate
                if ((argTypeNames != null)
                        && compareArgTypes(candidate, argTypeNames)) {
                    exactMatch = candidate;
                    break;
                }
            }
        }

        // Determine method for breakpoint
        Method method;
        if (exactMatch != null) {
            // Name and signature match
            method = exactMatch;
        } else if ((argTypeNames == null) && (matchCount > 0)) {
            // At least one abc matched and no arg types were specified
            if (matchCount == 1) {
                method = firstMatch;       // Only one match; safe to use it
            } else {
                throw new AmbiguousMethodException();
            }
        } else {
            throw new NoSuchMethodException(methodName());
        }
        return method;
    }
}
