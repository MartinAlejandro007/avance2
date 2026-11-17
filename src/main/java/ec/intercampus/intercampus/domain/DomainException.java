package ec.intercampus.intercampus.domain;

public class DomainException extends RuntimeException {
    public enum Code { NOT_FOUND, INVALID_INPUT, DUPLICATE, IN_USE }
    private final Code code;
    public DomainException(Code code, String message) {
        super(message);
        this.code = code;
    }
    public Code getCode() { return code; }
}
