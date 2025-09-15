package com.example.palindrome;

import java.util.*;

public final class PalindromeFinderEertree {
    public List<String> findPalindromes(String sentence) {
        Objects.requireNonNull(sentence, "sentence must not be null");
        validateContainsOnlyLettersOrDigits(sentence);
        if (sentence.length() < 3) {
            return List.of();
        }
        char[] characters = sentence.toCharArray();
        PalindromicTree palindromicTree = new PalindromicTree(characters);
        List<SubstringInterval> candidateIntervals = new ArrayList<>();
        for (int position = 0; position < characters.length; position++) {
            int nodeIndex = palindromicTree.addCharacterAt(position);
            int palindromeLength = palindromicTree.lengthOfNode(nodeIndex);
            if (palindromeLength >= 3) {
                int startIndexInclusive = position - palindromeLength + 1;
                candidateIntervals.add(new SubstringInterval(startIndexInclusive, position));
            }
        }
        if (candidateIntervals.isEmpty()) {
            return List.of();
        }
        candidateIntervals.sort(Comparator.comparingInt(SubstringInterval::length)
                .reversed()
                .thenComparingInt(interval -> interval.startIndexInclusive));
        boolean[] occupiedPositions = new boolean[characters.length];
        List<SubstringInterval> selectedIntervals = new ArrayList<>();
        for (SubstringInterval interval : candidateIntervals) {
            if (!overlapsWithOccupiedPositions(occupiedPositions, interval)) {
                markAsOccupied(occupiedPositions, interval);
                selectedIntervals.add(interval);
            }
        }
        selectedIntervals.sort(Comparator.comparingInt(i -> i.startIndexInclusive));
        List<String> palindromes = new ArrayList<>(selectedIntervals.size());
        for (SubstringInterval interval : selectedIntervals) {
            palindromes.add(sentence.substring(interval.startIndexInclusive, interval.endIndexInclusive + 1));
        }
        return Collections.unmodifiableList(palindromes);
    }

    private static void validateContainsOnlyLettersOrDigits(String text) {
        for (int position = 0; position < text.length(); position++) {
            char character = text.charAt(position);
            if (!Character.isLetterOrDigit(character)) {
                throw new IllegalArgumentException(
                        "Input must contain only letters and digits. Invalid character at index " + position + "."
                );
            }
        }
    }

    private static final class PalindromicTree {
        private static final class PalindromeNode {
            final int palindromeLength;
            int suffixLinkNodeIndex;
            final Map<Character, Integer> transitionsByCharacter = new HashMap<>();

            PalindromeNode(int palindromeLength, int suffixLinkNodeIndex) {
                this.palindromeLength = palindromeLength;
                this.suffixLinkNodeIndex = suffixLinkNodeIndex;
            }
        }

        private final char[] source;
        private final List<PalindromeNode> nodes = new ArrayList<>();
        private int indexOfLongestSuffixPalindromeNode;

        PalindromicTree(char[] source) {
            this.source = source;
            nodes.add(new PalindromeNode(-1, 0));
            nodes.add(new PalindromeNode(0, 0));
            indexOfLongestSuffixPalindromeNode = 1;
        }

        int addCharacterAt(int position) {
            char appendedCharacter = source[position];
            int currentNodeIndex = indexOfLongestSuffixPalindromeNode;
            while (true) {
                int currentPalindromeLength = nodes.get(currentNodeIndex).palindromeLength;
                int mirroredLeftPosition = position - 1 - currentPalindromeLength;
                if (mirroredLeftPosition >= 0 && source[mirroredLeftPosition] == appendedCharacter) {
                    break;
                }
                currentNodeIndex = nodes.get(currentNodeIndex).suffixLinkNodeIndex;
            }
            Integer transitionNodeIndex = nodes.get(currentNodeIndex).transitionsByCharacter.get(appendedCharacter);
            if (transitionNodeIndex != null) {
                indexOfLongestSuffixPalindromeNode = transitionNodeIndex;
                return indexOfLongestSuffixPalindromeNode;
            }
            int newlyCreatedNodeIndex = nodes.size();
            int newPalindromeLength = nodes.get(currentNodeIndex).palindromeLength + 2;
            nodes.add(new PalindromeNode(newPalindromeLength, 0));
            nodes.get(currentNodeIndex).transitionsByCharacter.put(appendedCharacter, newlyCreatedNodeIndex);
            if (newPalindromeLength == 1) {
                nodes.get(newlyCreatedNodeIndex).suffixLinkNodeIndex = 1;
            } else {
                int suffixCandidateIndex = nodes.get(currentNodeIndex).suffixLinkNodeIndex;
                while (true) {
                    int suffixCandidateLength = nodes.get(suffixCandidateIndex).palindromeLength;
                    int mirroredLeftPosition = position - 1 - suffixCandidateLength;
                    if (mirroredLeftPosition >= 0 && source[mirroredLeftPosition] == appendedCharacter) {
                        int link = nodes.get(suffixCandidateIndex).transitionsByCharacter.get(appendedCharacter);
                        nodes.get(newlyCreatedNodeIndex).suffixLinkNodeIndex = link;
                        break;
                    }
                    suffixCandidateIndex = nodes.get(suffixCandidateIndex).suffixLinkNodeIndex;
                }
            }
            indexOfLongestSuffixPalindromeNode = newlyCreatedNodeIndex;
            return indexOfLongestSuffixPalindromeNode;
        }

        int lengthOfNode(int nodeIndex) {
            return nodes.get(nodeIndex).palindromeLength;
        }
    }

    private record SubstringInterval(int startIndexInclusive, int endIndexInclusive) {
        int length() {
            return endIndexInclusive - startIndexInclusive + 1;
        }
    }

    private static boolean overlapsWithOccupiedPositions(boolean[] occupiedPositions, SubstringInterval interval) {
        for (int index = interval.startIndexInclusive; index <= interval.endIndexInclusive; index++) {
            if (occupiedPositions[index]) {
                return true;
            }
        }
        return false;
    }

    private static void markAsOccupied(boolean[] occupiedPositions, SubstringInterval interval) {
        for (int index = interval.startIndexInclusive; index <= interval.endIndexInclusive; index++) {
            occupiedPositions[index] = true;
        }
    }
}
