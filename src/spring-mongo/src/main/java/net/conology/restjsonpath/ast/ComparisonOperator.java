package net.conology.restjsonpath.ast;

public enum ComparisonOperator {
    EQ("=="),
    NEQ("!="),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    ;

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }


    @Override
    public String toString() {
        return symbol;
    }
}
