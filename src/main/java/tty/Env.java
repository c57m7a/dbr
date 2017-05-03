package tty;

import com.sun.jdi.*;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


class Env {

    static EventRequestSpecList specList = new EventRequestSpecList();

    private static VMConnection connection;

    private static SourceMapper sourceMapper = new SourceMapper("");
    private static List<String> excludes;

    private static final int SOURCE_CACHE_SIZE = 5;
    private static List<SourceCode> sourceCache = new LinkedList<>();

    private static HashMap<String, Value> savedValues = new HashMap<>();
    private static Method atExitMethod;

    static void init(String connectSpec, boolean openNow, int flags) {
        connection = new VMConnection(connectSpec, flags);
        if (!connection.isLaunch() || openNow) {
            connection.open();
        }
    }

    static VMConnection connection() {
        return connection;
    }

    static VirtualMachine vm() {
        return connection.vm();
    }

    static void shutdown() {
        shutdown(null);
    }

    static void shutdown(String message) {
        if (connection != null) {
            try {
                connection.disposeVM();
            } catch (VMDisconnectedException e) {
                // Shutting down after the VM has gone away. This is
                // not an error, and we just ignore it.
            }
        }
        if (message != null) {
            MessageOutput.lnprint(message);
            MessageOutput.println();
        }
//        System.exit(0);
    }

    static void setSourcePath(String srcPath) {
        sourceMapper = new SourceMapper(srcPath);
        sourceCache.clear();
    }

    static void setSourcePath(List<String> srcList) {
        sourceMapper = new SourceMapper(srcList);
        sourceCache.clear();
    }

    static String getSourcePath() {
        return sourceMapper.getSourcePath();
    }

    static private List<String> excludes() {
        if (excludes == null) {
            setExcludes("java.*, javax.*, sun.*, com.sun.*, android.*, com.android.*");
        }
        return excludes;
    }

    static String excludesString() {
        StringBuilder buffer = new StringBuilder();
        for (String pattern : excludes()) {
            buffer.append(pattern);
            buffer.append(",");
        }
        return buffer.toString();
    }

    static void addExcludes(StepRequest request) {
        excludes().forEach(request::addClassExclusionFilter);
    }

    static void addExcludes(MethodEntryRequest request) {
        excludes().forEach(request::addClassExclusionFilter);
    }

    static void addExcludes(MethodExitRequest request) {
        excludes().forEach(request::addClassExclusionFilter);
    }

    static void setExcludes(String excludeString) {
        StringTokenizer t = new StringTokenizer(excludeString, " ,;");
        List<String> list = new ArrayList<>();
        while (t.hasMoreTokens()) {
            list.add(t.nextToken());
        }
        excludes = list;
    }

    static Method atExitMethod() {
        return atExitMethod;
    }

    static void setAtExitMethod(Method mmm) {
        atExitMethod = mmm;
    }

    /**
     * Return a Reader cooresponding to the source of this location.
     * Return null if not available.
     * Note: returned reader must be closed.
     */
    static BufferedReader sourceReader(Location location) {
        return sourceMapper.sourceReader(location);
    }

    static synchronized String sourceLine(Location location, int lineNumber)
            throws IOException {
        if (lineNumber == -1) {
            throw new IllegalArgumentException();
        }

        try {
            String fileName = location.sourceName();

            Iterator<SourceCode> iter = sourceCache.iterator();
            SourceCode code = null;
            while (iter.hasNext()) {
                SourceCode candidate = iter.next();
                if (candidate.fileName().equals(fileName)) {
                    code = candidate;
                    iter.remove();
                    break;
                }
            }
            if (code == null) {
                BufferedReader reader = sourceReader(location);
                if (reader == null) {
                    throw new FileNotFoundException(fileName);
                }
                code = new SourceCode(fileName, reader);
                if (sourceCache.size() == SOURCE_CACHE_SIZE) {
                    sourceCache.remove(sourceCache.size() - 1);
                }
            }
            sourceCache.add(0, code);
            return code.sourceLine(lineNumber);
        } catch (AbsentInformationException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return a description of an object.
     */
    static String description(ObjectReference ref) {
        ReferenceType clazz = ref.referenceType();
        long id = ref.uniqueID();
        if (clazz == null) {
            return toHex(id);
        } else {
            return MessageOutput.format("object description and hex id",
                    new Object[]{clazz.name(),
                            toHex(id)});
        }
    }

    /**
     * Convert a long to a hexadecimal string.
     */
    static String toHex(long n) {
        char s1[] = new char[16];
        char s2[] = new char[18];

        /* Store digits in reverse order. */
        int i = 0;
        do {
            long d = n & 0xf;
            s1[i++] = (char) ((d < 10) ? ('0' + d) : ('a' + d - 10));
        } while ((n >>>= 4) > 0);

        /* Now reverse the array. */
        s2[0] = '0';
        s2[1] = 'x';
        int j = 2;
        while (--i >= 0) {
            s2[j++] = s1[i];
        }
        return new String(s2, 0, j);
    }

    /**
     * Convert hexadecimal strings to longs.
     */
    static long fromHex(String hexStr) {
        String str = hexStr.startsWith("0x") ?
                hexStr.substring(2).toLowerCase() : hexStr.toLowerCase();
        if (hexStr.length() == 0) {
            throw new NumberFormatException();
        }

        long ret = 0;
        for (int i = 0; i < str.length(); i++) {
            int c = str.charAt(i);
            if (c >= '0' && c <= '9') {
                ret = (ret * 16) + (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                ret = (ret * 16) + (c - 'a' + 10);
            } else {
                throw new NumberFormatException();
            }
        }
        return ret;
    }

    static ReferenceType getReferenceTypeFromToken(String idToken) {
        ReferenceType cls = null;
        if (Character.isDigit(idToken.charAt(0))) {
            cls = null;
        } else if (idToken.startsWith("*.")) {
            // This notation saves typing by letting the user omit leading
            // package names. The first
            // loaded class whose name matches this limited regular
            // expression is selected.
            idToken = idToken.substring(1);
            for (ReferenceType type : Env.vm().allClasses()) {
                if (type.name().endsWith(idToken)) {
                    cls = type;
                    break;
                }
            }
        } else {
            // It's a class name
            List<ReferenceType> classes = Env.vm().classesByName(idToken);
            if (classes.size() > 0) {
                // TO DO: handle multiples
                cls = classes.get(0);
            }
        }
        return cls;
    }

    static Set<String> getSaveKeys() {
        return savedValues.keySet();
    }

    static Value getSavedValue(String key) {
        return savedValues.get(key);
    }

    static void setSavedValue(String key, Value value) {
        savedValues.put(key, value);
    }

    static class SourceCode {
        private String fileName;
        private List<String> sourceLines = new ArrayList<>();

        SourceCode(String fileName, BufferedReader reader) throws IOException {
            this.fileName = fileName;
            try {
                String line = reader.readLine();
                while (line != null) {
                    sourceLines.add(line);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        }

        String fileName() {
            return fileName;
        }

        String sourceLine(int number) {
            int index = number - 1; // list is 0-indexed
            if (index >= sourceLines.size()) {
                return null;
            } else {
                return sourceLines.get(index);
            }
        }
    }
}
