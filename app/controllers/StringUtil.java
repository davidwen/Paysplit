package controllers;

public class StringUtil {

    public static String normalizeWhitespace(String str) {
        return str.replaceAll("\\s+", " ");
    }

}
