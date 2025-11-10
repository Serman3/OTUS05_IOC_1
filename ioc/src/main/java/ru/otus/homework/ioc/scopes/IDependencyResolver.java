package ru.otus.homework.ioc.scopes;

public interface IDependencyResolver {

    Object resolve(String dependency, Object[] args);
}
