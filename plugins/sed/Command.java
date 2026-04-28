package grzesiek11.aliucordplugins.sed;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Command {
    private String replace;
    private Pattern findRegex;
    private boolean gFlag;

    private Command(String replace, Pattern findRegex, boolean gFlag) {
        this.replace = replace;
        this.findRegex = findRegex;
        this.gFlag = gFlag;
    }

    public static Optional<Command> parse(String commandString, boolean advanced) {
        var findStringBuilder = new StringBuilder();
        var replaceStringBuilder = new StringBuilder();
        boolean gFlag = false;
        var state = ParserState.COMMAND;
        boolean escape = false;
        int charIndex = 0;
        loop: while (true) {
            Optional<Character> ch;
            if (charIndex < commandString.length()) {
                ch = Optional.of(commandString.charAt(charIndex));
            } else {
                ch = Optional.empty();
            }

            switch (state) {
                case COMMAND: {
                    if (ch.isPresent() && ch.get() == 's') {
                        state = ParserState.FIND_SLASH;
                        ++charIndex;
                        break;
                    } else {
                        return Optional.empty();
                    }
                }

                case FIND_SLASH: {
                    if (ch.isPresent() && ch.get() == '/') {
                        state = ParserState.FIND;
                        ++charIndex;
                        break;
                    } else {
                        return Optional.empty();
                    }
                }

                case FIND: {
                    if (!ch.isPresent()) {
                        return Optional.empty();
                    }
                    if (!escape) {
                        if (ch.get() == '\\') {
                            escape = true;
                            if (!advanced) {
                                findStringBuilder.append(ch.get());
                                ++charIndex;
                                break;
                            }
                        } else if (ch.get() == '/') {
                            state = ParserState.REPLACE;
                            ++charIndex;
                            break;
                        }
                    } else {
                        escape = false;
                    }
                    findStringBuilder.append(ch.get());
                    ++charIndex;
                    break;
                }

                case REPLACE: {
                    if (!ch.isPresent()) {
                        state = ParserState.END;
                        break;
                    }
                    if (!escape) {
                        if (ch.get() == '\\' && advanced) {
                            escape = true;
                        } else if (ch.get() == '/' && advanced) {
                            state = ParserState.FLAGS;
                            ++charIndex;
                            break;
                        }
                    } else {
                        escape = false;
                    }
                    replaceStringBuilder.append(ch.get());
                    ++charIndex;
                    break;
                }

                case FLAGS: {
                    if (ch.isPresent()) {
                        if (ch.get() == 'g') {
                            gFlag = true;
                            state = ParserState.END;
                            ++charIndex;
                            break;
                        } else {
                            return Optional.empty();
                        }
                    } else {
                        state = ParserState.END;
                    }
                }

                case END: {
                    if (!ch.isPresent()) {
                        break loop;
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }

        var findString = findStringBuilder.toString();
        var replaceString = replaceStringBuilder.toString();

        String findRegexString;
        String replace;
        if (advanced) {
            findRegexString = findString;
            replace = replaceString;
        } else {
            findRegexString = Pattern.quote(findString);
            replace = Matcher.quoteReplacement(replaceString);
        }
        var findRegex = Pattern.compile(findRegexString);
        return Optional.of(new Command(replace, findRegex, gFlag));
    }

    public String replace(String text) {
        var findMatcher = this.findRegex.matcher(text);
        if (this.gFlag) {
            return findMatcher.replaceAll(this.replace);
        } else {
            return findMatcher.replaceFirst(this.replace);
        }
    }
}
