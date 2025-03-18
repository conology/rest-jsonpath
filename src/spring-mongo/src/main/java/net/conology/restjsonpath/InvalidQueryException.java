package net.conology.restjsonpath;

public class InvalidQueryException extends IllegalArgumentException {

    private final String detail;

    public InvalidQueryException(Exception cause) {
        super(
            "Invalid user query",
            cause
        );
        detail = null;
    }

    public InvalidQueryException(String detail) {
        super("Invalid user query");
        this.detail = detail;
    }

    @Override
  public String getMessage() {
    if (detail != null) {
        return super.getMessage() + ": "  + detail;
    }

    if (getCause().getMessage() != null) {
        return super.getMessage() + ": "  + this.getCause().getMessage();
    }

    return super.getMessage();
  }
}
