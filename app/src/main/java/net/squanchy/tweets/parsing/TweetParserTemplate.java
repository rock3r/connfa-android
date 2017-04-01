package net.squanchy.tweets.parsing;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.squanchy.tweets.model.TweetSpecialTextData;

abstract class TweetParserTemplate<T> {

    abstract Pattern pattern();

    abstract T convertFrom(TweetSpecialTextData data);

    List<T> parseDataFrom(@NonNull String text) {
        Matcher matcher = pattern().matcher(text);
        List<T> matches = new ArrayList<>();

        while (matcher.find()) {
            matches.add(convertFrom(SpannableDataExtractor.extract(text, matcher.start(), matcher.end())));
        }

        return matches;
    }
}