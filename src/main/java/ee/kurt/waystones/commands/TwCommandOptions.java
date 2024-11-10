package ee.kurt.waystones.commands;

import java.util.List;

public class TwCommandOptions {
    public static final String setName = "setname";
    public static final String setPublic = "setpublic";
    public static final String openUI = "openui";
    public static final String list = "list";
    public static final String loadFromFile = "loadfromfile";
    public static final String saveToFile = "savetofile";
    public static final String clearAll = "clearall";

    public static final List<String> all = List.of(setName, setPublic, openUI, list, loadFromFile, saveToFile, clearAll);
}
