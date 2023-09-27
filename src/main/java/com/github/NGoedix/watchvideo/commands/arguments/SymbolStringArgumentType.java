package com.github.NGoedix.watchvideo.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class SymbolStringArgumentType implements ArgumentType<String> {

    public static SymbolStringArgumentType symbolString() {
        return new SymbolStringArgumentType();
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedSymbol(reader.peek())) {
            reader.skip();
        }
        if (reader.getCursor() == start) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().createWithContext(reader, ":/?=");
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private boolean isAllowedSymbol(final char c) {
        return Character.isLetterOrDigit(c) || c == ':' || c == '/' || c == '?' || c == '=' || c == '.' || c == '%' || c == '&' || c == '_';
    }
}
