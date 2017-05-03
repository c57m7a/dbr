package tty;

class MalformedMemberNameException extends Exception {
    private static final long serialVersionUID = 7759071468833196630L;

    public MalformedMemberNameException() {
        super();
    }

    public MalformedMemberNameException(String s) {
        super(s);
    }
}
