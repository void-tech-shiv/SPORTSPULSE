package sportspulse.algorithms;

import java.util.ArrayList;
import java.util.List;

public class StringAlgorithms {

    /**
     * KMP Algorithm for finding all occurrences of a pattern in a text.
     * Works case-insensitively.
     * @return List of starting indices where the pattern is found
     */
    public static List<Integer> kmpSearch(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.isEmpty()) {
            return occurrences;
        }

        String t = text.toLowerCase();
        String p = pattern.toLowerCase();
        
        int[] lps = computeLPSArray(p);
        int i = 0; // index for t
        int j = 0; // index for p

        while (i < t.length()) {
            if (p.charAt(j) == t.charAt(i)) {
                j++;
                i++;
            }
            if (j == p.length()) {
                occurrences.add(i - j);
                j = lps[j - 1];
            } else if (i < t.length() && p.charAt(j) != t.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return occurrences;
    }

    private static int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = len;
                    i++;
                }
            }
        }
        return lps;
    }

    /**
     * Rabin-Karp Algorithm for finding all occurrences of a pattern in a text.
     * Works case-insensitively.
     * @return List of starting indices where the pattern is found
     */
    public static List<Integer> rabinKarpSearch(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return occurrences;
        }

        String t = text.toLowerCase();
        String p = pattern.toLowerCase();

        int d = 256; // Number of characters in the input alphabet
        int q = 101; // A prime number
        int M = p.length();
        int N = t.length();
        int i, j;
        int pHash = 0; // hash value for pattern
        int tHash = 0; // hash value for txt
        int h = 1;

        // The value of h would be "pow(d, M-1)%q"
        for (i = 0; i < M - 1; i++) {
            h = (h * d) % q;
        }

        // Calculate the hash value of pattern and first window of text
        for (i = 0; i < M; i++) {
            pHash = (d * pHash + p.charAt(i)) % q;
            tHash = (d * tHash + t.charAt(i)) % q;
        }

        // Slide the pattern over text one by one
        for (i = 0; i <= N - M; i++) {
            // Check the hash values of current window of text and pattern.
            // If the hash values match, check characters one by one
            if (pHash == tHash) {
                for (j = 0; j < M; j++) {
                    if (t.charAt(i + j) != p.charAt(j)) {
                        break;
                    }
                }
                // if pHash == tHash and pattern[0...M-1] = txt[i, i+1, ...i+M-1]
                if (j == M) {
                    occurrences.add(i);
                }
            }

            // Calculate hash value for next window of text
            if (i < N - M) {
                tHash = (d * (tHash - t.charAt(i) * h) + t.charAt(i + M)) % q;
                // We might get negative value of tHash, converting it to positive
                if (tHash < 0) {
                    tHash = (tHash + q);
                }
            }
        }
        return occurrences;
    }
}
