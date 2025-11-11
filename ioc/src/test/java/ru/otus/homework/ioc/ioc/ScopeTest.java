package ru.otus.homework.ioc.ioc;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import ru.otus.homework.ioc.command.Command;
import ru.otus.homework.ioc.ioc.testObject.TestObject;
import ru.otus.homework.ioc.scopes.InitCommand;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
public class ScopeTest {

    @BeforeEach
    public void init() {
        new InitCommand().execute();
        Object iocScope = Ioc.resolve("IoC.Scope.Create", new Object[]{});
        ((Command) Ioc.resolve("IoC.Scope.Current.Set", new Object[]{iocScope})).execute();
    }

    @AfterEach
    public void afterAll() {
        ((Command) Ioc.resolve("IoC.Scope.Current.Clear", null)).execute();
    }

    @Test
    public void iocShouldResolveRegisteredDependencyInCurrentScopeTest() {
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"someDependency", (Function<Object[], Object>) (Object[] args) -> (Object) 1})).execute();
        assertEquals(1, (Integer) Ioc.resolve("someDependency", new Object[]{}));
    }

    @Test
    public void iocShouldThrowExceptionOnUnregisteredDependencyInCurrentScopeTest() {
        assertThrows(Exception.class, () -> Ioc.resolve("someDependency", null));
    }

    @Test
    public void iocShouldUseParentScopeIfResolvingDependencyIsNotDefinedInCurrentScopeTest() {
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"someDependency", (Function<Object[], Object>) (Object[] args) -> (Object) 1})).execute();

        Ioc.resolve("IoC.Scope.Current", new Object[]{});

        var iocScope = Ioc.resolve("IoC.Scope.Create", new Object[]{});
        ((Command) Ioc.resolve("IoC.Scope.Current.Set", new Object[]{iocScope})).execute();

        assertEquals(iocScope, Ioc.resolve("IoC.Scope.Current", new Object[]{}));
        assertEquals(1, (Integer) Ioc.resolve("someDependency", null));
    }

    @Test
    public void parentScopeCanBeSetManuallyForCreatingScopeTest() {
        var scope1 = Ioc.resolve("IoC.Scope.Create", new Object[]{});
        var scope2 = Ioc.resolve("IoC.Scope.Create", new Object[]{scope1});

        Ioc.resolve("IoC.Scope.Current.Set", new Object[]{scope1});
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"someDependency", (Function<Object[], Object>) (Object[] args) -> (Object) 2})).execute();
        Ioc.resolve("IoC.Scope.Current.Set", new Object[]{scope2});

        assertEquals(2, (Integer) Ioc.resolve("someDependency", null));
    }

    @Test
    public void creatingObjectTest() {
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"testObject", (Function<Object[], Object>) (Object[] args) -> new TestObject((Integer) args[0], (Integer) args[1])})).execute();
        TestObject testObject = Ioc.resolve("testObject", new Object[]{2, 3});
        assertEquals(5, testObject.calculate());
    }

}