package tty;

public class LineNotFoundException extends Exception {
    private static final long serialVersionUID = 3748297722519448995L;

    public LineNotFoundException() {
        super();
    }

    public LineNotFoundException(String s) {
        super(s);
    }
}
