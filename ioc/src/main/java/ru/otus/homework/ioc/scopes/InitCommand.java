package ru.otus.homework.ioc.scopes;

import ru.otus.homework.ioc.command.Command;
import ru.otus.homework.ioc.ioc.Ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InitCommand implements Command {

    public static final ThreadLocal<Object> currentScopes = new ThreadLocal<>();

    private static final Map<String, Function<Object[], Object>> rootScope = new ConcurrentHashMap<>();

    private static final AtomicBoolean alreadyExecutesSuccessfully = new AtomicBoolean(false);

    @Override
    public void execute() {
        if (alreadyExecutesSuccessfully.get()) return;

        synchronized (rootScope) {
            rootScope.put(
                    "IoC.Scope.Current.Set",
                    (Object[] args) -> new SetCurrentScopeCommand(args[0])
            );

            rootScope.put(
                    "IoC.Scope.Current.Clear",
                    (Object[] args) -> new ClearCurrentScopeCommand()
            );

            rootScope.put(
                    "IoC.Scope.Current",
                    (Object[] args) -> currentScopes.get() != null ? currentScopes.get() : rootScope
            );

            rootScope.put(
                    "IoC.Scope.Parent",
                    (Object[] args) -> new RuntimeException("The root scope has no a parent scope.")
            );

            rootScope.put(
                    "IoC.Scope.Create.Empty",
                    (Object[] args) -> new HashMap<String, Function<Object[], Object>>()
            );

            rootScope.put(
                    "IoC.Scope.Create",
                    (Object[] args) -> {
                        Map<String, Function<Object[], Object>> creatingScope = Ioc.resolve("IoC.Scope.Create.Empty", new Object[]{});

                        if (args.length > 0) {
                            var parentScope = args[0];
                            creatingScope.put("IoC.Scope.Parent", (Object[] arg) -> parentScope);
                        } else {
                            var parentScope = Ioc.resolve("IoC.Scope.Current", new Object[]{});
                            creatingScope.put("IoC.Scope.Parent", (Object[] arg) -> parentScope);
                        }
                        return creatingScope;
                    }
            );

            rootScope.put(
                    "IoC.Register",
                    (Object[] args) -> new RegisterDependencyCommand((String) args[0], (Function<Object[], Object>) args[1])
            );

            Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> oldStrategy =
                    (BiFunction<String, Object[], Object> currentStrategy) -> (String dependency, Object[] args) -> {
                        var scope = currentScopes.get() != null ? currentScopes.get() : rootScope;
                        var dependencyResolver = new DependencyResolver(scope);
                        return dependencyResolver.resolve(dependency, args);
                    };

            ((Command) Ioc.resolve(
                    "Update Ioc Resolve Dependency Strategy",
                    new Object[]{oldStrategy}
            )).execute();

            alreadyExecutesSuccessfully.set(true);
        }
    }
}
