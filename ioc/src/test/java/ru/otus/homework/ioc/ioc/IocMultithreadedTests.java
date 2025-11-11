package ru.otus.homework.ioc.ioc;


import org.junit.jupiter.api.*;
import ru.otus.homework.ioc.command.Command;
import ru.otus.homework.ioc.scopes.InitCommand;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class IocMultithreadedTests {

    private static final int NUM_THREADS = 100;

    @BeforeEach
    public void setup() {
        new InitCommand().execute();
        // Создаем новый скоуп для изоляции этого теста
        Object iocScope = Ioc.resolve("IoC.Scope.Create", new Object[]{});
        // Устанавливаем его как текущий в ThreadLocal JUnit-потока
        ((Command) Ioc.resolve("IoC.Scope.Current.Set", new Object[]{iocScope})).execute();
    }

    @AfterEach
    public void teardown() {
        // Очищаем ThreadLocal для текущего потока
        ((Command) Ioc.resolve("IoC.Scope.Current.Clear", null)).execute();
    }

    @Test
    public void concurrentRegistrationAndResolutionInRootScopeTest() throws InterruptedException {
        // В этом тесте рабочие потоки используют rootScope, так как их ThreadLocal пуст.
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        List<Throwable> threadErrors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    String dependencyName = "dependency_" + threadId;
                    int expectedValue = threadId;

                    // Регистрация происходит в rootScope (потокобезопасный ConcurrentHashMap)
                    ((Command) Ioc.resolve("IoC.Register", new Object[]{
                            dependencyName,
                            (Function<Object[], Object>) (Object[] args) -> expectedValue
                    })).execute();

                    assertEquals(expectedValue, (Integer) Ioc.resolve(dependencyName, new Object[]{}),
                            "Thread " + threadId + ": Should resolve its own registered dependency from rootScope");
                } catch (Throwable e) {
                    threadErrors.add(e);
                }
            });
        }

        executor.shutdownNow();

        if (!threadErrors.isEmpty()) fail("Errors occurred in threads: " + threadErrors.get(0).getMessage(), threadErrors.get(0));

        // Проверяем, что все зависимости доступны из основного потока (из rootScope)
        for (int i = 0; i < NUM_THREADS; i++) {
            String dependencyName = "dependency_" + i;
            assertEquals(i, (Integer) Ioc.resolve(dependencyName, new Object[]{}),
                    "Main thread: Should resolve dependency " + dependencyName + " from rootScope");
        }
    }

    @Test
    public void threadLocalScopesWithConcurrentRegistrationAndResolutionTest() throws InterruptedException {
        // Этот тест проверяет, что каждый поток может создать и изолировать свой собственный скоуп.
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        List<Throwable> threadErrors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                Object threadScope = null;
                try {

                    // 1. Каждый поток создает свой собственный скоуп
                    threadScope = Ioc.resolve("IoC.Scope.Create", new Object[]{});
                    // 2. Каждый поток устанавливает свой скоуп как текущий (в своем ThreadLocal)
                    ((Command) Ioc.resolve("IoC.Scope.Current.Set", new Object[]{threadScope})).execute();

                    // 3. Регистрирует и разрешает зависимость только в своем скоупе
                    String dependencyName = "threadLocalDependency_" + threadId;
                    int expectedValue = threadId * 100;
                    ((Command) Ioc.resolve("IoC.Register", new Object[]{
                            dependencyName,
                            (Function<Object[], Object>) (Object[] args) -> expectedValue
                    })).execute();

                    assertEquals(expectedValue, (Integer) Ioc.resolve(dependencyName, new Object[]{}),
                            "Thread " + threadId + ": Should resolve its own thread-local dependency");

                    // 4. Проверяем изоляцию
                    boolean exceptionThrown = false;
                    try {
                        Ioc.resolve("threadLocalDependency_" + ((threadId + 1) % NUM_THREADS), null);
                    } catch (Exception e) {
                        exceptionThrown = true;
                    }
                    assertTrue(exceptionThrown, "Thread " + threadId + ": Should throw exception for unregistered dependency from other thread's scope");

                } catch (Throwable e) {
                    threadErrors.add(e);
                } finally {
                    // 5. Очистка ThreadLocal
                    if (threadScope != null) {
                        try {
                            ((Command) Ioc.resolve("IoC.Scope.Current.Clear", null)).execute();
                        } catch (Exception e) {
                            threadErrors.add(e);
                        }
                    }
                }
            });
        }

        executor.shutdownNow();

        if (!threadErrors.isEmpty()) fail("Errors occurred in threads: " + threadErrors.get(0).getMessage(), threadErrors.get(0));

        // Проверяем, что основной поток не видит никаких зарегистрированных потоками зависимостей
        for (int i = 0; i < NUM_THREADS; i++) {
            String dependencyName = "threadLocalDependency_" + i;
            assertThrows(Exception.class, () -> Ioc.resolve(dependencyName, null),
                    "Main thread: Should not resolve thread-local dependency " + dependencyName);
        }
    }
}

