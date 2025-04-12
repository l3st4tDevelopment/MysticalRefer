package com.mysticalkingdoms.mysticalrefer.enums;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public enum GeneratorType {
    LETTERS("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"),
    NUMBERS("0123456789"),
    BOTH("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");



    private final String characters;
    GeneratorType(String characters) {
        this.characters = characters;
    }

    public String generateCode(int length) {
        return IntStream.generate(() -> characters.charAt(ThreadLocalRandom.current().nextInt(characters.length())))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}