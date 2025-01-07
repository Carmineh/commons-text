/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.text.similarity;

import java.util.Arrays;

/**
 * An algorithm for measuring the difference between two character sequences.
 *
 * <p>
 * This is the number of changes needed to change one sequence into another, where each change is a single character modification (deletion, insertion or
 * substitution).
 * </p>
 * <p>
 * This code has been adapted from Apache Commons Lang 3.3.
 * </p>
 *
 * @since 1.0
 */
public class LevenshteinDistance implements EditDistance<Integer> {

    /**
     * Singleton instance.
     */
    private static final LevenshteinDistance INSTANCE = new LevenshteinDistance();

    /**
     * Gets the default instance.
     *
     * @return The default instance
     */
    public static LevenshteinDistance getDefaultInstance() {
        return INSTANCE;
    }

    /**
     * Find the Levenshtein distance between two CharSequences if it's less than or equal to a given threshold.
     *
     * <p>
     * This implementation follows from Algorithms on Strings, Trees and Sequences by Dan Gusfield and Chas Emerick's implementation of the Levenshtein distance
     * algorithm from <a href="http://www.merriampark.com/ld.htm" >http://www.merriampark.com/ld.htm</a>
     * </p>
     *
     * <pre>
     * limitedCompare(null, *, *)             = IllegalArgumentException
     * limitedCompare(*, null, *)             = IllegalArgumentException
     * limitedCompare(*, *, -1)               = IllegalArgumentException
     * limitedCompare("","", 0)               = 0
     * limitedCompare("aaapppp", "", 8)       = 7
     * limitedCompare("aaapppp", "", 7)       = 7
     * limitedCompare("aaapppp", "", 6))      = -1
     * limitedCompare("elephant", "hippo", 7) = 7
     * limitedCompare("elephant", "hippo", 6) = -1
     * limitedCompare("hippo", "elephant", 7) = 7
     * limitedCompare("hippo", "elephant", 6) = -1
     * </pre>
     *
     * @param left      the first SimilarityInput, must not be null
     * @param right     the second SimilarityInput, must not be null
     * @param threshold the target threshold, must not be negative
     * @return result distance, or -1
     */
    private static <E> int limitedCompare(SimilarityInput<E> left, SimilarityInput<E> right, final int threshold) { // NOPMD
        validateInputs(left, right, threshold);

        int n = left.length(); // length of left
        int m = right.length(); // length of right

        if (n == 0 || m == 0) {
            return handleEmptyInputs(n, m, threshold);
        }

        if (n > m) {
            final SimilarityInput<E> tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
        }

        if (m - n > threshold) {
            return -1;
        }

        int[] p = new int[n + 1]; // 'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally

        initializeArrays(p, d, n, threshold);

        for (int j = 1; j <= m; j++) {
            final E rightJ = right.at(j - 1); // jth character of right
            d[0] = j;

            final int min = Math.max(1, j - threshold);
            final int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(n, j + threshold);

            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE;
            }

            if (processRow(left, rightJ, p, d, min, max) > threshold) {
                return -1;
            }

            int[] tempD = p;
            p = d;
            d = tempD;
        }

        return p[n] <= threshold ? p[n] : -1;
    }

    private static <E> void validateInputs(SimilarityInput<E> left, SimilarityInput<E> right, int threshold) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("CharSequences must not be null");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
        }
    }

    private static int handleEmptyInputs(int n, int m, int threshold) {
        if (n == 0) {
            return m <= threshold ? m : -1;
        }
        if (m == 0) {
            return n <= threshold ? n : -1;
        }
        return -1;
    }

    private static void initializeArrays(int[] p, int[] d, int n, int threshold) {
        final int boundary = Math.min(n, threshold) + 1;
        for (int i = 0; i < boundary; i++) {
            p[i] = i;
        }
        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
        Arrays.fill(d, Integer.MAX_VALUE);
    }


    private static <E> int processRow(SimilarityInput<E> left, E rightJ, int[] p, int[] d, int min, int max) {
        int lowerBound = Integer.MAX_VALUE;
        for (int i = min; i <= max; i++) {
            if (left.at(i - 1).equals(rightJ)) {
                d[i] = p[i - 1];
            } else {
                d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
            }
            lowerBound = Math.min(lowerBound, d[i]);
        }
        return lowerBound;
    }

    /**
     * Finds the Levenshtein distance between two Strings.
     *
     * <p>
     * A higher score indicates a greater distance.
     * </p>
     *
     * <p>
     * The previous implementation of the Levenshtein distance algorithm was from
     * <a href="https://web.archive.org/web/20120526085419/http://www.merriampark.com/ldjava.htm">
     * https://web.archive.org/web/20120526085419/http://www.merriampark.com/ldjava.htm</a>
     * </p>
     *
     * <p>
     * This implementation only need one single-dimensional arrays of length s.length() + 1
     * </p>
     *
     * <pre>
     * unlimitedCompare(null, *)             = IllegalArgumentException
     * unlimitedCompare(*, null)             = IllegalArgumentException
     * unlimitedCompare("","")               = 0
     * unlimitedCompare("","a")              = 1
     * unlimitedCompare("aaapppp", "")       = 7
     * unlimitedCompare("frog", "fog")       = 1
     * unlimitedCompare("fly", "ant")        = 3
     * unlimitedCompare("elephant", "hippo") = 7
     * unlimitedCompare("hippo", "elephant") = 7
     * unlimitedCompare("hippo", "zzzzzzzz") = 8
     * unlimitedCompare("hello", "hallo")    = 1
     * </pre>
     *
     * @param left  the first CharSequence, must not be null
     * @param right the second CharSequence, must not be null
     * @return result distance, or -1
     * @throws IllegalArgumentException if either CharSequence input is {@code null}
     */
    private static <E> int unlimitedCompare(SimilarityInput<E> left, SimilarityInput<E> right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("CharSequences must not be null");
        }
        /*
         * This implementation use two variable to record the previous cost counts, So this implementation use less memory than previous impl.
         */
        int n = left.length(); // length of left
        int m = right.length(); // length of right

        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        if (n > m) {
            // swap the input strings to consume less memory
            final SimilarityInput<E> tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
        }
        final int[] p = new int[n + 1];
        // indexes into strings left and right
        int i; // iterates through left
        int j; // iterates through right
        int upperLeft;
        int upper;
        E rightJ; // jth character of right
        int cost; // cost
        for (i = 0; i <= n; i++) {
            p[i] = i;
        }
        for (j = 1; j <= m; j++) {
            upperLeft = p[0];
            rightJ = right.at(j - 1);
            p[0] = j;

            for (i = 1; i <= n; i++) {
                upper = p[i];
                cost = left.at(i - 1).equals(rightJ) ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upperLeft + cost);
                upperLeft = upper;
            }
        }

        if (n >= 0 && n < p.length) {
            return p[n];
        } else {
            throw new IllegalStateException("Index out of bounds: n = " + n + ", p.length = " + p.length);
        }
    }

    /**
     * Threshold.
     */
    private final Integer threshold;

    /**
     * This returns the default instance that uses a version of the algorithm that does not use a threshold parameter.
     *
     * @see LevenshteinDistance#getDefaultInstance()
     * @deprecated Use {@link #getDefaultInstance()}.
     */
    @Deprecated
    public LevenshteinDistance() {
        this(null);
    }

    /**
     * If the threshold is not null, distance calculations will be limited to a maximum length. If the threshold is null, the unlimited version of the algorithm
     * will be used.
     *
     * @param threshold If this is null then distances calculations will not be limited. This may not be negative.
     */
    public LevenshteinDistance(final Integer threshold) {
        if (threshold != null && threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
        }
        this.threshold = threshold;
    }

    /**
     * Computes the Levenshtein distance between two Strings.
     *
     * <p>
     * A higher score indicates a greater distance.
     * </p>
     *
     * <p>
     * The previous implementation of the Levenshtein distance algorithm was from
     * <a href="http://www.merriampark.com/ld.htm">http://www.merriampark.com/ld.htm</a>
     * </p>
     *
     * <p>
     * Chas Emerick has written an implementation in Java, which avoids an OutOfMemoryError which can occur when my Java implementation is used with very large
     * strings.<br>
     * This implementation of the Levenshtein distance algorithm is from
     * <a href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.com/ldjava.htm</a>
     * </p>
     *
     * <pre>
     * distance.apply(null, *)             = IllegalArgumentException
     * distance.apply(*, null)             = IllegalArgumentException
     * distance.apply("","")               = 0
     * distance.apply("","a")              = 1
     * distance.apply("aaapppp", "")       = 7
     * distance.apply("frog", "fog")       = 1
     * distance.apply("fly", "ant")        = 3
     * distance.apply("elephant", "hippo") = 7
     * distance.apply("hippo", "elephant") = 7
     * distance.apply("hippo", "zzzzzzzz") = 8
     * distance.apply("hello", "hallo")    = 1
     * </pre>
     *
     * @param left  the first input, must not be null
     * @param right the second input, must not be null
     * @return result distance, or -1
     * @throws IllegalArgumentException if either String input {@code null}
     */
    @Override
    public Integer apply(final CharSequence left, final CharSequence right) {
        return apply(SimilarityInput.input(left), SimilarityInput.input(right));
    }

    /**
     * Computes the Levenshtein distance between two inputs.
     *
     * <p>
     * A higher score indicates a greater distance.
     * </p>
     *
     * <pre>
     * distance.apply(null, *)             = IllegalArgumentException
     * distance.apply(*, null)             = IllegalArgumentException
     * distance.apply("","")               = 0
     * distance.apply("","a")              = 1
     * distance.apply("aaapppp", "")       = 7
     * distance.apply("frog", "fog")       = 1
     * distance.apply("fly", "ant")        = 3
     * distance.apply("elephant", "hippo") = 7
     * distance.apply("hippo", "elephant") = 7
     * distance.apply("hippo", "zzzzzzzz") = 8
     * distance.apply("hello", "hallo")    = 1
     * </pre>
     *
     * @param <E>   The type of similarity score unit.
     * @param left  the first input, must not be null.
     * @param right the second input, must not be null.
     * @return result distance, or -1.
     * @throws IllegalArgumentException if either String input {@code null}.
     * @since 1.13.0
     */
    public <E> Integer apply(final SimilarityInput<E> left, final SimilarityInput<E> right) {
        if (threshold != null) {
            return limitedCompare(left, right, threshold);
        }
        return unlimitedCompare(left, right);
    }

    /**
     * Gets the distance threshold.
     *
     * @return The distance threshold
     */
    public Integer getThreshold() {
        return threshold;
    }

}
