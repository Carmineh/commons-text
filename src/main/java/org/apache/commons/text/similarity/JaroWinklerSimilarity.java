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
import java.util.Objects;

/**
 * A similarity algorithm indicating the percentage of matched characters between two character sequences.
 *
 * <p>
 * The Jaro measure is the weighted sum of percentage of matched characters from each file and transposed characters. Winkler increased this measure for
 * matching initial characters.
 * </p>
 * <p>
 * This implementation is based on the Jaro Winkler similarity algorithm from <a href="https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 * https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.
 * </p>
 * <p>
 * This code has been adapted from Apache Commons Lang 3.3.
 * </p>
 *
 * @since 1.7
 */
public class JaroWinklerSimilarity implements SimilarityScore<Double> {

    /**
     * Singleton instance.
     */
    static final JaroWinklerSimilarity INSTANCE = new JaroWinklerSimilarity();

    /**
     * Computes the Jaro-Winkler string matches, half transpositions, prefix array.
     *
     * @param first  the first input to be matched.
     * @param second the second input to be matched.
     * @return mtp array containing: matches, half transpositions, and prefix.
     */
    protected static int[] matches(final CharSequence first, final CharSequence second) {
        return matches(SimilarityInput.input(first), SimilarityInput.input(second));
    }

    /**
     * Computes the Jaro-Winkler string matches, half transpositions, prefix array.
     *
     * @param <E> The type of similarity score unit.
     * @param first  the first input to be matched.
     * @param second the second input to be matched.
     * @return mtp array containing: matches, half transpositions, and prefix.
     * @since 1.13.0
     */
    protected static <E> int[] matches(final SimilarityInput<E> first, final SimilarityInput<E> second) {
    final SimilarityInput<E> max = first.length() > second.length() ? first : second;
    final SimilarityInput<E> min = first.length() > second.length() ? second : first;
    final int range = Math.max(max.length() / 2 - 1, 0);
    final int[] matchIndexes = new int[min.length()];
    Arrays.fill(matchIndexes, -1);
    final boolean[] matchFlags = new boolean[max.length()];
    int matches = findMatches(min, max, range, matchIndexes, matchFlags);
    final Object[] ms1 = extractMatches(min, matchIndexes, matches);
    final Object[] ms2 = extractMatches(max, matchFlags, matches);
    int halfTranspositions = countHalfTranspositions(ms1, ms2);
    int prefix = countPrefix(first, second);
    return new int[] { matches, halfTranspositions, prefix };
}

private static <E> int findMatches(SimilarityInput<E> min, SimilarityInput<E> max, int range, int[] matchIndexes, boolean[] matchFlags) {
    int matches = 0;
    for (int mi = 0; mi < min.length(); mi++) {
        final E c1 = min.at(mi);
        for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
            if (!matchFlags[xi] && c1.equals(max.at(xi))) {
                matchIndexes[mi] = xi;
                matchFlags[xi] = true;
                matches++;
                break;
            }
        }
    }
    return matches;
}

private static <E> Object[] extractMatches(SimilarityInput<E> input, int[] matchIndexes, int matches) {
    final Object[] ms = new Object[matches];
    for (int i = 0, si = 0; i < input.length(); i++) {
        if (matchIndexes[i] != -1) {
            ms[si] = input.at(i);
            si++;
        }
    }
    return ms;
}

private static <E> Object[] extractMatches(SimilarityInput<E> input, boolean[] matchFlags, int matches) {
    final Object[] ms = new Object[matches];
    for (int i = 0, si = 0; i < input.length(); i++) {
        if (matchFlags[i]) {
            ms[si] = input.at(i);
            si++;
        }
    }
    return ms;
}

private static int countHalfTranspositions(Object[] ms1, Object[] ms2) {
    int halfTranspositions = 0;
    for (int mi = 0; mi < ms1.length; mi++) {
        if (!ms1[mi].equals(ms2[mi])) {
            halfTranspositions++;
        }
    }
    return halfTranspositions;
}

private static <E> int countPrefix(SimilarityInput<E> first, SimilarityInput<E> second) {
    int prefix = 0;
    for (int mi = 0; mi < Math.min(4, first.length()); mi++) {
        if (!first.at(mi).equals(second.at(mi))) {
            break;
        }
        prefix++;
    }
    return prefix;
}

    /**
     * Computes the Jaro Winkler Similarity between two character sequences.
     *
     * <pre>
     * sim.apply(null, null)          = IllegalArgumentException
     * sim.apply("foo", null)         = IllegalArgumentException
     * sim.apply(null, "foo")         = IllegalArgumentException
     * sim.apply("", "")              = 1.0
     * sim.apply("foo", "foo")        = 1.0
     * sim.apply("foo", "foo ")       = 0.94
     * sim.apply("foo", "foo  ")      = 0.91
     * sim.apply("foo", " foo ")      = 0.87
     * sim.apply("foo", "  foo")      = 0.51
     * sim.apply("", "a")             = 0.0
     * sim.apply("aaapppp", "")       = 0.0
     * sim.apply("frog", "fog")       = 0.93
     * sim.apply("fly", "ant")        = 0.0
     * sim.apply("elephant", "hippo") = 0.44
     * sim.apply("hippo", "elephant") = 0.44
     * sim.apply("hippo", "zzzzzzzz") = 0.0
     * sim.apply("hello", "hallo")    = 0.88
     * sim.apply("ABC Corporation", "ABC Corp") = 0.91
     * sim.apply("D N H Enterprises Inc", "D &amp; H Enterprises, Inc.") = 0.95
     * sim.apply("My Gym Children's Fitness Center", "My Gym. Childrens Fitness") = 0.92
     * sim.apply("PENNSYLVANIA", "PENNCISYLVNIA") = 0.88
     * </pre>
     *
     * @param left  the first input, must not be null.
     * @param right the second input, must not be null.
     * @return result similarity.
     * @throws IllegalArgumentException if either CharSequence input is {@code null}.
     */
    @Override
    public Double apply(final CharSequence left, final CharSequence right) {
        return apply(SimilarityInput.input(left), SimilarityInput.input(right));
    }

    /**
     * Computes the Jaro Winkler Similarity between two character sequences.
     *
     * <pre>
     * sim.apply(null, null)          = IllegalArgumentException
     * sim.apply("foo", null)         = IllegalArgumentException
     * sim.apply(null, "foo")         = IllegalArgumentException
     * sim.apply("", "")              = 1.0
     * sim.apply("foo", "foo")        = 1.0
     * sim.apply("foo", "foo ")       = 0.94
     * sim.apply("foo", "foo  ")      = 0.91
     * sim.apply("foo", " foo ")      = 0.87
     * sim.apply("foo", "  foo")      = 0.51
     * sim.apply("", "a")             = 0.0
     * sim.apply("aaapppp", "")       = 0.0
     * sim.apply("frog", "fog")       = 0.93
     * sim.apply("fly", "ant")        = 0.0
     * sim.apply("elephant", "hippo") = 0.44
     * sim.apply("hippo", "elephant") = 0.44
     * sim.apply("hippo", "zzzzzzzz") = 0.0
     * sim.apply("hello", "hallo")    = 0.88
     * sim.apply("ABC Corporation", "ABC Corp") = 0.91
     * sim.apply("D N H Enterprises Inc", "D &amp; H Enterprises, Inc.") = 0.95
     * sim.apply("My Gym Children's Fitness Center", "My Gym. Childrens Fitness") = 0.92
     * sim.apply("PENNSYLVANIA", "PENNCISYLVNIA") = 0.88
     * </pre>
     *
     * @param <E> The type of similarity score unit.
     * @param left  the first input, must not be null.
     * @param right the second input, must not be null.
     * @return result similarity.
     * @throws IllegalArgumentException if either CharSequence input is {@code null}.
     * @since 1.13.0
     */
    public <E> Double apply(final SimilarityInput<E> left, final SimilarityInput<E> right) {
        final double defaultScalingFactor = 0.1;
        if (left == null || right == null) {
            throw new IllegalArgumentException("CharSequences must not be null");
        }
        if (Objects.equals(left, right)) {
            return 1d;
        }
        final int[] mtp = matches(left, right);
        final double m = mtp[0];
        if (m == 0) {
            return 0d;
        }
        final double j = (m / left.length() + m / right.length() + (m - (double) mtp[1] / 2) / m) / 3;
        return j < 0.7d ? j : j + defaultScalingFactor * mtp[2] * (1d - j);
    }

}
