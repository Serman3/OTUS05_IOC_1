package ru.otus.homework.ioc.scopes;

import ru.otus.homework.ioc.command.Command;
import ru.otus.homework.ioc.ioc.Ioc;

import java.util.Map;
import java.util.function.Function;

public class RegisterDependencyCommand implements Command {

    private final String dependency;
    private final Function<Object[], Object> dependencyResolverStrategy;

    public RegisterDependencyCommand(String dependency, Function<Object[], Object> dependencyResolverStratgey) {
        this.dependency = dependency;
        this.dependencyResolverStrategy = dependencyResolverStratgey;
    }

    @Override
    public void execute() {
        Map<String, Function<Object[], Object>> currentScope = Ioc.resolve("IoC.Scope.Current", new Object[]{});
        currentScope.put(dependency, dependencyResolverStrategy);
    }
}
