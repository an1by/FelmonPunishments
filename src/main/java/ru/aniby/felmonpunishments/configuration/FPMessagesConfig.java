package ru.aniby.felmonpunishments.configuration;

import ru.aniby.felmonapi.category.FelmonComponent;

public class FPMessagesConfig {
    public static FelmonComponent errorInProcessing = new FelmonComponent("&cПроизошла ошибка при обработке!");
    public static FelmonComponent punishmentNotExists = new FelmonComponent("&cНаказание не найдено!");
    public static FelmonComponent existsSimilar = new FelmonComponent("&cВ базе данных уже есть подобная запись!");
    public static FelmonComponent alreadyBanned = new FelmonComponent("&cИгрок уже находится в бане!");
    public static FelmonComponent alreadyMuted = new FelmonComponent("&cУ игрока уже присутствует мут!");
    public static FelmonComponent dayOrMore = new FelmonComponent("&cЭто наказание можно выдать только с минимальным сроком в 1 день!");
    public static FelmonComponent playerNotFound = new FelmonComponent("&cИгрок с такими данными не найден!");
    public static FelmonComponent canNotPunish = new FelmonComponent("&cВы не можете наказать этого игрока!");
    public static FelmonComponent notEnoughPermissions = new FelmonComponent("&cНедостаточно прав!");
    public static FelmonComponent wrongArguments = new FelmonComponent("&cНеверные аргументы для команды!");
    public static FelmonComponent disabledCommand = new FelmonComponent("&cКоманда отключена!");
}
