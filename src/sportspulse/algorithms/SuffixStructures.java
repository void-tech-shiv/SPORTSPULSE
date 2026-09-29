package sportspulse.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class SuffixStructures {

    /**
     * Builds a suffix array for the given text.
     * Time Complexity: O(N^2 log N) using standard sorting.
     * @param text the input text
     * @return the suffix array containing starting indices of sorted suffixes
     */
    public static int[] buildSuffixArray(String text) {
        int n = text.length();
        Integer[] suffixes = new Integer[n];
        for (int i = 0; i < n; i++) {
            suffixes[i] = i;
        }

        Arrays.sort(suffixes, new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                // To avoid creating many substrings, compare character by character
                int lenI = n - i;
                int lenJ = n - j;
                int minLen = Math.min(lenI, lenJ);
                for (int k = 0; k < minLen; k++) {
                    char ci = text.charAt(i + k);
                    char cj = text.charAt(j + k);
                    if (ci != cj) {
                        return ci - cj;
                    }
                }
                return lenI - lenJ;
            }
        });

        int[] suffixArray = new int[n];
        for (int i = 0; i < n; i++) {
            suffixArray[i] = suffixes[i];
        }
        return suffixArray;
    }

    /**
     * Builds the Longest Common Prefix (LCP) array using Kasai's algorithm.
     * Time Complexity: O(N)
     * @param text the input text
     * @param suffixArray the suffix array for the text
     * @return the LCP array
     */
    public static int[] buildLCPArray(String text, int[] suffixArray) {
        int n = text.length();
        int[] lcp = new int[n];
        int[] invSuff = new int[n];

        // Fill values in invSuff[]
        for (int i = 0; i < n; i++) {
            invSuff[suffixArray[i]] = i;
        }

        int k = 0;
        for (int i = 0; i < n; i++) {
            if (invSuff[i] == n - 1) {
                k = 0;
                continue;
            }

            int j = suffixArray[invSuff[i] + 1];

            // Directly matching characters
            while (i + k < n && j + k < n && text.charAt(i + k) == text.charAt(j + k)) {
                k++;
            }

            lcp[invSuff[i]] = k; // lcp for the present suffix.

            if (k > 0) {
                k--;
            }
        }
        return lcp;
    }

    /**
     * Searches for a pattern in the text using its suffix array (Binary Search).
     * Time Complexity: O(M log N) where M is pattern length and N is text length.
     * @param text the input text
     * @param pattern the pattern to search for
     * @param suffixArray the suffix array for the text
     * @return a list of starting indices where the pattern is found
     */
    public static List<Integer> search(String text, String pattern, int[] suffixArray) {
        List<Integer> result = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.isEmpty()) {
            return result;
        }

        int n = text.length();
        int m = pattern.length();
        int left = 0;
        int right = n - 1;
        int startMatch = -1;

        // Binary search for the first occurrence
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int suffixIndex = suffixArray[mid];
            
            // Compare pattern with the suffix starting at suffixIndex
            int cmp = comparePatternToSuffix(pattern, text, suffixIndex);

            if (cmp == 0) {
                startMatch = mid;
                right = mid - 1; // Look left for the very first occurrence
            } else if (cmp < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        // If found, find all consecutive matching suffixes
        if (startMatch != -1) {
            for (int i = startMatch; i < n; i++) {
                int suffixIndex = suffixArray[i];
                if (comparePatternToSuffix(pattern, text, suffixIndex) == 0) {
                    result.add(suffixIndex);
                } else {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Helper method to compare pattern with a suffix.
     * Returns 0 if pattern is a prefix of the suffix.
     * Returns < 0 if pattern is lexicographically smaller.
     * Returns > 0 if pattern is lexicographically larger.
     */
    private static int comparePatternToSuffix(String pattern, String text, int suffixIndex) {
        int m = pattern.length();
        int n = text.length();
        int i = 0;
        while (i < m && suffixIndex + i < n) {
            char pChar = pattern.charAt(i);
            char tChar = text.charAt(suffixIndex + i);
            if (pChar != tChar) {
                return pChar - tChar;
            }
            i++;
        }
        // If we exhausted text before pattern, text suffix is smaller
        if (i < m) {
            return 1;
        }
        return 0; // Pattern is fully matched as prefix
    }
}
