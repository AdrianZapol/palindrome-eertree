package com.example.palindrome;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.Normalizer;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PalindromeFinderEertreeTest {

    private PalindromeFinderEertree finder;

    @BeforeEach
    void setUp() {
        // Given
        finder = new PalindromeFinderEertree();
        // Then (brak warunków do weryfikacji w setup)
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("throws NPE when input is null with clear message")
        void throwsNpeWhenInputIsNull() {
            // Given
            String input = null;

            // When + Then
            assertThatThrownBy(() -> finder.findPalindromes(input))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("sentence must not be null");
        }

        @ParameterizedTest(name = "rejects non-alphanumeric: \"{0}\"")
        @ValueSource(strings = {
                "abc def", "abc_def", "abc-def", "abc!", "tab\tchar", "new\nline"
        })
        void rejectsNonAlphanumeric(String input) {
            // Given
            // input z niedozwolonym znakiem

            // When + Then
            assertThatThrownBy(() -> finder.findPalindromes(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Input must contain only letters and digits.")
                    .hasMessageContaining("Invalid character at index ");
        }

        @Test
        @DisplayName("error message includes exact index of offending character")
        void errorMessageContainsExactIndex() {
            // Given
            final String input = "abc-def";

            // When + Then
            assertThatThrownBy(() -> finder.findPalindromes(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid character at index 3");
        }

        @ParameterizedTest(name = "short inputs return empty list: \"{0}\"")
        @ValueSource(strings = {"", "a", "Z", "1", "ab", "1a"})
        void shortInputsReturnEmptyList(String input) {
            // Given
            // krótkie wejście (< 3)

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Finding Palindromes")
    class FindingPalindromes {

        @ParameterizedTest(name = "finds a single palindrome in: \"{0}\" -> \"{1}\"")
        @CsvSource({
                "aba, aba",
                "abba, abba",
                "abac, aba",
                "xyzzyx, xyzzyx",
                "racecarxx, racecar",
                "xxracecar, racecar",
                "x12321y, 12321",
                "ąśćśą, ąśćśą"
        })
        void findsSinglePalindrome(String rawInput, String rawExpected) {
            // Given
            String input = nfc(rawInput);
            String expected = nfc(rawExpected);

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly(expected);
        }

        @Test
        @DisplayName("finds multiple disjoint palindromes ordered left-to-right")
        void findsMultipleDisjointPalindromesInOrder() {
            // Given
            final String input = "abcba12321xyzzyx";

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly("abcba", "12321", "xyzzyx");
        }

        @Test
        @DisplayName("handles adjacent palindromes without overlap")
        void handlesAdjacentPalindromes() {
            // Given
            final String input = "abacdcxy";

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly("aba", "cdc");
        }

        @ParameterizedTest(name = "mixed alpha-numeric cases: \"{0}\" -> {1}{2}")
        @CsvSource({
                "ab11211cd, 11211, ''",
                "1abccba2aba3, abccba, aba",
                "abcdefg12345, '', ''"
        })
        void mixedAlphaNumericCases(String input, String expected1, String expected2) {
            // Given
            // różne warianty znaków

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            if (expected1.isEmpty() && expected2.isEmpty()) {
                assertThat(result).isEmpty();
            } else if (expected2.isEmpty()) {
                assertThat(result).containsExactly(expected1);
            } else {
                assertThat(result).containsExactly(expected1, expected2);
            }
        }

        private static String nfc(String s) {
            return s == null ? null : Normalizer.normalize(s, Normalizer.Form.NFC);
        }
    }

    @Nested
    @DisplayName("Selection Strategy (No Overlap, Prefer Longest)")
    class SelectionStrategy {

        @Test
        @DisplayName("when palindromes overlap, the longest is selected (ababa)")
        void prefersLongestWhenOverlapping() {
            // Given
            final String input = "ababa";

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly("ababa");
        }

        @Test
        @DisplayName("highly repetitive input results in a single longest palindrome")
        void selectsSingleLongestInRepetitiveInput() {
            // Given
            final String input = "aaaaa";

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly("aaaaa");
        }

        @Test
        @DisplayName("results are ordered by start index despite greedy length selection")
        void resultsAreOrderedByStartIndex() {
            // Given
            final String input = "zzaba123321qq";

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly("aba", "123321");
        }
    }

    @Nested
    @DisplayName("Result Semantics")
    class ResultSemantics {

        @Test
        @DisplayName("returned list is unmodifiable")
        void returnsUnmodifiableList() {
            // Given
            final String input = "aba";

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThatThrownBy(() -> result.add("should-fail"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Performance/Scale (Smoke)")
    class PerformanceScale {

        @Test
        @DisplayName("handles large input and finds the embedded long palindrome")
        void handlesLargeInput() {
            // Given
            final String base = repeat("abcd", 25_000);
            final String embedded = "12345678987654321";
            final int mid = base.length() / 2;
            final String input = base.substring(0, mid) + embedded + base.substring(mid);

            // When
            List<String> result = finder.findPalindromes(input);

            // Then
            assertThat(result).containsExactly(embedded);
        }
    }

    private static String repeat(String pattern, int times) {
        return pattern.repeat(Math.max(0, times));
    }
}
