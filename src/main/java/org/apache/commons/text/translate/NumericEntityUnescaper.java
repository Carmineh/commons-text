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
package org.apache.commons.text.translate;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Translates XML numeric entities of the form &amp;#[xX]?\d+;? to
 * the specific code point.
 *
 * Note that the semicolon is optional.
 *
 * @since 1.0
 */
public class NumericEntityUnescaper extends CharSequenceTranslator {

    /** Enumerates NumericEntityUnescaper options for unescaping. */
    public enum OPTION {

        /**
         * Requires a semicolon.
         */
        SEMI_COLON_REQUIRED,

        /**
         * Does not require a semicolon.
         */
        SEMI_COLON_OPTIONAL,

        /**
         * Throws an exception if a semicolon is missing.
         */
        ERROR_IF_NO_SEMI_COLON
    }

    /** Default options. */
    private static final EnumSet<OPTION> DEFAULT_OPTIONS = EnumSet
        .copyOf(Collections.singletonList(OPTION.SEMI_COLON_REQUIRED));

    /** EnumSet of OPTIONS, given from the constructor, read-only. */
    private final EnumSet<OPTION> options;

    /**
     * Creates a UnicodeUnescaper.
     *
     * The constructor takes a list of options, only one type of which is currently
     * available (whether to allow, error or ignore the semicolon on the end of a
     * numeric entity to being missing).
     *
     * For example, to support numeric entities without a ';':
     *    new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional)
     * and to throw an IllegalArgumentException when they're missing:
     *    new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon)
     *
     * Note that the default behavior is to ignore them.
     *
     * @param options to apply to this unescaper
     */
    public NumericEntityUnescaper(final OPTION... options) {
        this.options = ArrayUtils.isEmpty(options) ? DEFAULT_OPTIONS : EnumSet.copyOf(Arrays.asList(options));
    }

    /**
     * Tests whether the passed in option is currently set.
     *
     * @param option to check state of
     * @return whether the option is set
     */
    public boolean isSet(final OPTION option) {
        return options.contains(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer writer) throws IOException {
        final int seqEnd = input.length();
        if (input.charAt(index) == '&' && index < seqEnd - 2 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            if (isHexPrefix(input, start)) {
                start++;
                isHex = true;
                if (start == seqEnd) {
                    return 0;
                }
            }

            int end = findEndIndex(input, start, seqEnd);
            final boolean semiNext = end != seqEnd && input.charAt(end) == ';';

            if (!semiNext && handleMissingSemicolon()) {
                return 0;
            }

            final int entityValue = parseEntityValue(input, start, end, isHex);
            if (entityValue == -1) {
                return 0;
            }

            writeEntityValue(writer, entityValue);
            return calculateReturnLength(start, end, isHex, semiNext);
        }
        return 0;
    }

    private boolean isHexPrefix(final CharSequence input, int start) {
        final char firstChar = input.charAt(start);
        return firstChar == 'x' || firstChar == 'X';
    }

    private int findEndIndex(final CharSequence input, int start, int seqEnd) {
        int end = start;
        while (end < seqEnd && isValidEntityChar(input.charAt(end))) {
            end++;
        }
        return end;
    }

    private boolean isValidEntityChar(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private boolean handleMissingSemicolon() {
        if (isSet(OPTION.SEMI_COLON_REQUIRED)) {
            return true;
        }
        if (isSet(OPTION.ERROR_IF_NO_SEMI_COLON)) {
            throw new IllegalArgumentException("Semi-colon required at end of numeric entity");
        }
        return false;
    }

    private int parseEntityValue(final CharSequence input, int start, int end, boolean isHex) {
        try {
            if (isHex) {
                return Integer.parseInt(input.subSequence(start, end).toString(), 16);
            } else {
                return Integer.parseInt(input.subSequence(start, end).toString(), 10);
            }
        } catch (final NumberFormatException nfe) {
            return -1;
        }
    }

    private void writeEntityValue(final Writer writer, int entityValue) throws IOException {
        if (entityValue > 0xFFFF) {
            final char[] chrs = Character.toChars(entityValue);
            writer.write(chrs[0]);
            writer.write(chrs[1]);
        } else {
            writer.write(entityValue);
        }
    }

    private int calculateReturnLength(int start, int end, boolean isHex, boolean semiNext) {
        return 2 + end - start + (isHex ? 1 : 0) + (semiNext ? 1 : 0);
    }
}
