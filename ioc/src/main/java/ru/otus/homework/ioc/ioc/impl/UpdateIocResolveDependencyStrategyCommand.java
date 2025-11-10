package ru.otus.homework.ioc.ioc.impl;

import ru.otus.homework.ioc.command.Command;
import ru.otus.homework.ioc.ioc.Ioc;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UpdateIocResolveDependencyStrategyCommand implements Command {

    private final Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> updateIoCStrategy;

    public UpdateIocResolveDependencyStrategyCommand(Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> updateIoCStrategy) {
        this.updateIoCStrategy = updateIoCStrategy;
    }

    @Override
    public void execute() {
        Ioc.strategy = updateIoCStrategy.apply(Ioc.strategy);
    }
}
