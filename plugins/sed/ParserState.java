package grzesiek11.aliucordplugins.sed;

enum ParserState {
    COMMAND,
    FIND_SLASH,
    FIND,
    REPLACE,
    FLAGS,
    END;
}
