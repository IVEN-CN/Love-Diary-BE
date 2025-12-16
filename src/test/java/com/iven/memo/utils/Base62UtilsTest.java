package com.iven.memo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base62Utils 单元测试类
 * 测试工具类的编码和解码功能
 */
class Base62UtilsTest {

    // ============= 基础功能测试 =============

    @Test
    @DisplayName("测试0的编码和解码")
    void testEncodeDecodeZero() {
        // Given
        long originalId = 0L;

        // When
        String encoded = Base62Utils.encode(originalId);
        long decoded = Base62Utils.decode(encoded);

        // Then
        assertEquals("0", encoded, "0的编码应该是'0'");
        assertEquals(originalId, decoded, "解码后应该等于原始值");
    }

    @Test
    @DisplayName("测试1的编码和解码")
    void testEncodeDecodeOne() {
        // Given
        long originalId = 1L;

        // When
        String encoded = Base62Utils.encode(originalId);
        long decoded = Base62Utils.decode(encoded);

        // Then
        assertEquals("1", encoded, "1的编码应该是'1'");
        assertEquals(originalId, decoded, "解码后应该等于原始值");
    }

    @Test
    @DisplayName("测试边界值61和62的编码")
    void testEncodeBoundaryValues() {
        // 61 = "Z" (0-9:10个 + A-Z:26个 = 36个, 36+25=61)
        assertEquals("z", Base62Utils.encode(61L), "61应该编码为'Z'");

        // 62 = "10" (因为62进制的进位)
        assertEquals("10", Base62Utils.encode(62L), "62应该编码为'10'");
    }

    // ============= 往返测试：编码后解码应该得到原始值 =============

    @ParameterizedTest
    @ValueSource(longs = {1L, 10L, 100L, 1000L, 10000L, 100000L, 999999L,
            123456789L, 987654321L, Long.MAX_VALUE})
    @DisplayName("往返测试：编码后解码应该得到原始值")
    void testEncodeDecodeRoundTrip(long originalId) {
        // When
        String encoded = Base62Utils.encode(originalId);
        long decoded = Base62Utils.decode(encoded);

        // Then
        assertEquals(originalId, decoded,
                String.format("原始值 %d -> 编码 '%s' -> 解码 %d 应该一致",
                        originalId, encoded, decoded));
    }

    // ============= 异常情况测试 =============

    @Test
    @DisplayName("测试负数编码抛出异常")
    void testEncodeNegativeNumberThrowsException() {
        // Given
        long negativeId = -1L;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62Utils.encode(negativeId),
                "负数编码应该抛出IllegalArgumentException"
        );

        assertEquals("ID must be non-negative", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("测试空字符串或空白字符串解码抛出异常")
    void testDecodeEmptyOrBlankStringThrowsException(String input) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62Utils.decode(input),
                "空字符串或空白字符串解码应该抛出IllegalArgumentException"
        );

        assertEquals("Input string cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("测试null字符串解码抛出异常")
    void testDecodeNullStringThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62Utils.decode(null),
                "null字符串解码应该抛出IllegalArgumentException"
        );

        assertEquals("Input string cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"@", "#", "$", "%", "!", "ABC@123", "test#", "123+"})
    @DisplayName("测试包含非法字符的字符串解码抛出异常")
    void testDecodeInvalidCharactersThrowsException(String invalidString) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62Utils.decode(invalidString),
                "包含非法字符的字符串解码应该抛出IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Invalid character"),
                "异常消息应该包含'Invalid character'");
    }

    // ============= 性能和行为测试 =============

    @Test
    @DisplayName("测试编码结果不包含前导零（除了0本身）")
    void testEncodeNoLeadingZeros() {
        // 测试一些数字，确保编码结果没有前导0
        assertFalse(Base62Utils.encode(1L).startsWith("0"));
        assertFalse(Base62Utils.encode(62L).startsWith("0")); // "10"
        assertFalse(Base62Utils.encode(12345L).startsWith("0"));

        // 但是0本身编码为"0"是OK的
        assertEquals("0", Base62Utils.encode(0L));
    }

    @Test
    @DisplayName("测试大小写敏感性")
    void testCaseSensitivity() {
        // Base62是大小写敏感的，'A'和'a'代表不同的值
        long valueA = Base62Utils.decode("A"); // 10
        long valuea = Base62Utils.decode("a"); // 36

        assertNotEquals(valueA, valuea, "'A'和'a'应该解码为不同的值");

        // 验证编码结果保持原始大小写
        String encoded10 = Base62Utils.encode(10L); // 应该是"A"
        assertEquals("A", encoded10);
    }

    // ============= 批量测试 =============

    @ParameterizedTest
    @MethodSource("provideIdsForBatchTest")
    @DisplayName("批量测试：一系列连续ID的编码解码")
    void testBatchEncodeDecode(long id) {
        String encoded = Base62Utils.encode(id);
        long decoded = Base62Utils.decode(encoded);
        assertEquals(id, decoded,
                String.format("ID %d 的往返测试失败", id));
    }

    private static Stream<Arguments> provideIdsForBatchTest() {
        return Stream.of(
                Arguments.of(0L),
                Arguments.of(1L),
                Arguments.of(61L),
                Arguments.of(62L),
                Arguments.of(63L),
                Arguments.of(100L),
                Arguments.of(1000L),
                Arguments.of(10000L),
                Arguments.of(100000L),
                Arguments.of(1000000L),
                Arguments.of(10000000L)
        );
    }

    // ============= 长整型最大值测试 =============

    @Test
    @DisplayName("测试Long.MAX_VALUE的编码和解码")
    void testLongMaxValue() {
        // Given
        long maxValue = Long.MAX_VALUE; // 9223372036854775807

        // When
        String encoded = Base62Utils.encode(maxValue);
        long decoded = Base62Utils.decode(encoded);

        // Then
        assertEquals(maxValue, decoded, "Long.MAX_VALUE 的往返测试应该通过");

        // 验证编码结果不为空且长度合理
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        // Long.MAX_VALUE 的Base62编码长度大约是11个字符
        // 因为 62^10 ≈ 8.39e17 < 9.22e18 < 62^11 ≈ 5.20e19
        assertTrue(encoded.length() >= 10 && encoded.length() <= 12,
                "Long.MAX_VALUE 的编码长度应该在10-12个字符之间");
    }

    // ============= 字符表完整性测试 =============

    @Test
    @DisplayName("测试字符表所有字符都能正确解码")
    void testAllCharactersInAlphabet() {
        // 测试所有62个字符都能正确解码
        for (int i = 0; i < 62; i++) {
            char c = Base62UtilsTest.getBase62Char(i);
            String singleChar = String.valueOf(c);

            // 解码应该成功
            long decoded = Base62Utils.decode(singleChar);

            // 编码回去应该得到相同的字符
            String encoded = Base62Utils.encode(decoded);
            assertEquals(singleChar, encoded,
                    String.format("字符 '%c' (值 %d) 的往返测试失败", c, i));
        }
    }

    // 辅助方法：根据索引获取Base62字符
    private static char getBase62Char(int index) {
        if (index >= 0 && index <= 9) {
            return (char) ('0' + index);
        } else if (index <= 35) { // 10-35 -> A-Z
            return (char) ('A' + (index - 10));
        } else if (index <= 61) { // 36-61 -> a-z
            return (char) ('a' + (index - 36));
        }
        throw new IllegalArgumentException("索引超出范围: " + index);
    }

    // ============= 一致性测试 =============

    @Test
    @DisplayName("测试编码结果的一致性")
    void testEncodeConsistency() {
        long[] testIds = {123L, 456L, 789L, 1000L, 9999L};

        for (long id : testIds) {
            // 多次编码同一个ID应该得到相同的结果
            String firstEncode = Base62Utils.encode(id);
            String secondEncode = Base62Utils.encode(id);

            assertEquals(firstEncode, secondEncode,
                    String.format("ID %d 的多次编码结果应该一致", id));
        }
    }
}