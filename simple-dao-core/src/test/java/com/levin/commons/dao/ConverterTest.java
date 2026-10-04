package com.levin.commons.dao;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConverterTest {

    @Test
    void shouldRemainAConverterAndWorkAsAFunction() {
        AtomicInteger calls = new AtomicInteger();
        Converter<String, Integer> converter = value -> {
            calls.incrementAndGet();
            return value.length();
        };
        Function<String, Integer> function = converter;

        assertEquals(3, converter.convert("dao"));
        assertEquals(4, function.apply("info"));
        List<Integer> lengths = Arrays.asList("a", "bb").stream()
                .map(converter).collect(Collectors.toList());
        assertEquals(Arrays.asList(1, 2), lengths);
        assertEquals(4, calls.get());
    }
}
