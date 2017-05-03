package tty;

public class AmbiguousMethodException extends Exception {
    private static final long serialVersionUID = -5372629264936918654L;

    public AmbiguousMethodException() {
        super();
    }

    public AmbiguousMethodException(String s) {
        super(s);
    }
}
