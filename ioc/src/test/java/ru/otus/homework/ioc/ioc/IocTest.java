package ru.otus.homework.ioc.ioc;

import org.junit.jupiter.api.Test;
import ru.otus.homework.ioc.command.Command;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IocTest {

    @Test
    public void iocShouldUpdateResolveDependencyStrategyTest() {
        final boolean[] wasCalled = {false};
        Command command = Ioc.resolve("Update Ioc Resolve Dependency Strategy", new Object[]{(Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>) (BiFunction<String, Object[], Object> currentStrategy) -> {
            wasCalled[0] = true;
            return currentStrategy;
        }});
        command.execute();
        assertTrue(wasCalled[0]);
    }

    @Test
    public void iocShouldThrowInvalidCastExceptionIfDependencyResolvesAnotherTypeTest() {
        assertThrows(ClassCastException.class, () -> {
            Ioc.resolve(
                    "Update Ioc Resolve Dependency Strategy",
                    new Object[]{(BiFunction<String, Object[], Object>) (args, obj) -> args}
            );
        });
    }

    @Test
    public void iocShouldThrowArgumentExceptionIfDependencyIsNotFoundTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ioc.resolve("UnexistingDependency", null);
        });
    }

}