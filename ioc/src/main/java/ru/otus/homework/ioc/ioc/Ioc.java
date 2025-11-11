package ru.otus.homework.ioc.ioc;

import ru.otus.homework.ioc.ioc.impl.UpdateIocResolveDependencyStrategyCommand;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Ioc {

    public static BiFunction<String, Object[], Object> strategy = (String dependency, Object[] args) -> {
        if ("Update Ioc Resolve Dependency Strategy".equals(dependency)) {
            return new UpdateIocResolveDependencyStrategyCommand((Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>) args[0]);
        } else {
            throw new IllegalArgumentException("Dependency " +  dependency + " is not found.");
        }
    };

    /// <summary>
    /// Разрешение зависимости.
    /// </summary>
    /// <typeparam name="T">Ожидаемый тип объекта, получаемого в результате разрешения зависимости.
    /// Если полученный объект невозможно привести в запрашиваемому типу, то выбрасывается исключение
    /// <see cref="System.InvalidCastException"/>
    /// </typeparam>
    /// <param name="dependency">Строковое имя разрешаемой зависимости. В реализации контейнера
    /// по умолчанию определенп только одна зависимость "Update Ioc Resolve Dependency Strategy",
    /// которая позволяет переопределить стратегию разрешения зависимостей по-умолчанию.</param>
    /// <param name="args">Произвольное количество аргументов, которые получает на вход стратегия
    /// разрешения зависимостей. Для переопределения стратегии разрешения зависимостей по-умолчанию
    /// на вход подается лямбда функция типа Func<Func<string, object[], object>, Func<string, object[], object> >,
    /// которая на вход принимает текущую стратегию разрешения зависмисостей типа Func<string, object[], object>,
    /// на выходе возвращает новую стратегию типа Func<string, object[], object>.
    /// </param>
    /// <returns>Объект, полученный в результате разрешения зависимости.
    /// Если указана несуществующая зависимость, то выбрасывается исключение
    /// <see cref="System.ArgumentException"/>
    /// </returns>
    public static <T> T resolve(String dependency, Object[] args) {
        T apply = (T) strategy.apply(dependency, args);
        return apply;
    }

}
