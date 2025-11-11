package ru.otus.homework.ioc.scopes;

import ru.otus.homework.ioc.command.Command;

public class ClearCurrentScopeCommand implements Command {

    @Override
    public void execute() {
        InitCommand.CURRENT_SCOPES.remove();
    }
}
