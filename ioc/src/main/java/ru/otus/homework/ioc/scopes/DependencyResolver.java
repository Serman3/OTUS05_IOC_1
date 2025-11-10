package ru.otus.homework.ioc.scopes;

import java.util.Map;
import java.util.function.Function;

public class DependencyResolver implements IDependencyResolver {

    private Map<String, Function<Object[], Object>> dependencies;

    public DependencyResolver(Object scope) {
        this.dependencies = (Map<String, Function<Object[], Object>>) scope;
    }

    @Override
    public Object resolve(String dependency, Object[] args) {
        var dependencies = this.dependencies;
        while (true) {
            Function<Object[], Object> dependencyResolverStrategy = null;
            if (dependencies.containsKey(dependency)) {
                dependencyResolverStrategy = dependencies.get(dependency);
                return dependencyResolverStrategy.apply(args);
            } else {
                dependencies = (Map<String, Function<Object[], Object>>) dependencies.get("IoC.Scope.Parent").apply(args);
            }
        }
    }
}
