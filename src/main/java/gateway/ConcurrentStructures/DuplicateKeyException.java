package gateway.ConcurrentStructures;

public class DuplicateKeyException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DuplicateKeyException(String errorMsg) {
        super(errorMsg);
    }
}