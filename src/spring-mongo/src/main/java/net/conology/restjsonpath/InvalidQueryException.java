package net.conology.restjsonpath;

public class InvalidQueryException extends IllegalArgumentException {
    public InvalidQueryException(Exception cause) {
        super(
            "Invalid user query",
            cause
        );
    }

  @Override
  public String getMessage() {
    return super.getMessage() + ": "  + this.getCause().getMessage();
  }
}
