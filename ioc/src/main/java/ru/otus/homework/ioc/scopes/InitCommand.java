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

    public static final ThreadLocal<Object> CURRENT_SCOPES = new ThreadLocal<>();

    private static final Map<String, Function<Object[], Object>> ROOT_SCOPE = new ConcurrentHashMap<>();

    private static final AtomicBoolean ALREADY_EXECUTES_SUCCESSFULLY = new AtomicBoolean(false);

    @Override
    public void execute() {
        synchronized (ROOT_SCOPE) {
            if (ALREADY_EXECUTES_SUCCESSFULLY.get()) return;

            ROOT_SCOPE.put(
                    "IoC.Scope.Current.Set",
                    (Object[] args) -> new SetCurrentScopeCommand(args[0])
            );

            ROOT_SCOPE.put(
                    "IoC.Scope.Current.Clear",
                    (Object[] args) -> new ClearCurrentScopeCommand()
            );

            ROOT_SCOPE.put(
                    "IoC.Scope.Current",
                    (Object[] args) -> CURRENT_SCOPES.get() != null ? CURRENT_SCOPES.get() : ROOT_SCOPE
            );

            ROOT_SCOPE.put(
                    "IoC.Scope.Parent",
                    (Object[] args) -> new RuntimeException("The root scope has no a parent scope.")
            );

            ROOT_SCOPE.put(
                    "IoC.Scope.Create.Empty",
                    (Object[] args) -> new HashMap<String, Function<Object[], Object>>()
            );

            ROOT_SCOPE.put(
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

            ROOT_SCOPE.put(
                    "IoC.Register",
                    (Object[] args) -> new RegisterDependencyCommand((String) args[0], (Function<Object[], Object>) args[1])
            );

            Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> oldStrategy =
                    (BiFunction<String, Object[], Object> currentStrategy) -> (String dependency, Object[] args) -> {
                        var scope = CURRENT_SCOPES.get() != null ? CURRENT_SCOPES.get() : ROOT_SCOPE;
                        var dependencyResolver = new DependencyResolver(scope);
                        return dependencyResolver.resolve(dependency, args);
                    };

            ((Command) Ioc.resolve(
                    "Update Ioc Resolve Dependency Strategy",
                    new Object[]{oldStrategy}
            )).execute();

            ALREADY_EXECUTES_SUCCESSFULLY.set(true);
        }
    }
}
