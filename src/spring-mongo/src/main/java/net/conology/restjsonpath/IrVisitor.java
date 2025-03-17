package net.conology.restjsonpath;

public interface IrVisitor<T> {
    void accept(T ast);
}
