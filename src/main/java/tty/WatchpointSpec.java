package tty;

abstract class WatchpointSpec extends EventRequestSpec {
    final String fieldId;

    WatchpointSpec(ReferenceTypeSpec refSpec, String fieldId)
            throws MalformedMemberNameException {
        super(refSpec);
        this.fieldId = fieldId;
        if (!isJavaIdentifier(fieldId)) {
            throw new MalformedMemberNameException(fieldId);
        }
    }

    @Override
    public int hashCode() {
        return refSpec.hashCode() + fieldId.hashCode() +
                getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WatchpointSpec) {
            WatchpointSpec watchpoint = (WatchpointSpec) obj;

            return fieldId.equals(watchpoint.fieldId) &&
                    refSpec.equals(watchpoint.refSpec) &&
                    getClass().equals(watchpoint.getClass());
        } else {
            return false;
        }
    }

    @Override
    String errorMessageFor(Exception e) {
        if (e instanceof NoSuchFieldException) {
            return (MessageOutput.format("No field in",
                    new Object[]{fieldId, refSpec.toString()}));
        } else {
            return super.errorMessageFor(e);
        }
    }
}
