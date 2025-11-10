package ru.otus.homework.ioc.scopes;

import ru.otus.homework.ioc.command.Command;

public class SetCurrentScopeCommand implements Command {

    private final Object scope;

    public SetCurrentScopeCommand(Object scope) {
        this.scope = scope;
    }

    @Override
    public void execute() {
        InitCommand.currentScopes.set(scope);
    }
}
