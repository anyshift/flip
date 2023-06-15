package com.flip.utils;

import toolgood.words.*;

import java.util.List;

public class SensitiveWordUtils {

    public static String wordsMatchExFilter(String text, List<String> sensitiveWords) {
        WordsMatchEx wordsMatchEx = new WordsMatchEx();
        try {
            wordsMatchEx.SetKeywords(sensitiveWords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        boolean contain = wordsMatchEx.ContainsAny(text);
        if (contain) {
            return wordsMatchEx.Replace(text, '*');
        }

        return text;
    }

    public static String wordsSearchFilter(String text, List<String> sensitiveWords) {
        WordsSearch wordsSearch = new WordsSearch();
        wordsSearch.SetKeywords(sensitiveWords);

        boolean contain = wordsSearch.ContainsAny(text);
        if (contain) {
            return wordsSearch.Replace(text, '*');
        }

        return text;
    }

    public static String wordsSearchExFilter(String text, List<String> sensitiveWords) {
        WordsSearchEx wordsSearchEx = new WordsSearchEx();
        wordsSearchEx.SetKeywords(sensitiveWords);

        boolean contain = wordsSearchEx.ContainsAny(text);
        if (contain) {
            return wordsSearchEx.Replace(text, '*');
        }

        return text;
    }

    public static String wordsSearchEx2Filter(String text, List<String> sensitiveWords) {
        WordsSearchEx2 wordsSearchEx2 = new WordsSearchEx2();
        wordsSearchEx2.SetKeywords(sensitiveWords);

        boolean contain = wordsSearchEx2.ContainsAny(text);
        if (contain) {
            return wordsSearchEx2.Replace(text, '*');
        }

        return text;
    }

    public static String stringSearchFilter(String text, List<String> sensitiveWords) {
        StringSearch stringSearch = new StringSearch();
        stringSearch.SetKeywords(sensitiveWords);

        boolean contain = stringSearch.ContainsAny(text);
        if (contain) {
            return stringSearch.Replace(text, '*');
        }

        return text;
    }

    public static String stringSearchExFilter(String text, List<String> sensitiveWords) {
        StringSearchEx stringSearchEx = new StringSearchEx();
        stringSearchEx.SetKeywords(sensitiveWords);

        boolean contain = stringSearchEx.ContainsAny(text);
        if (contain) {
            return stringSearchEx.Replace(text, '*');
        }

        return text;
    }

    public static String stringSearchEx2Filter(String text, List<String> sensitiveWords) {
        StringSearchEx2 stringSearchEx2 = new StringSearchEx2();
        stringSearchEx2.SetKeywords(sensitiveWords);

        boolean contain = stringSearchEx2.ContainsAny(text);
        if (contain) {
            return stringSearchEx2.Replace(text, '*');
        }

        return text;
    }
}
