package net.conology.restjsonpath;

public interface PostProcessor<T> {
    void accept(T ast);
}
