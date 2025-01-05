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
package org.apache.commons.text;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * BenchmarkRunner is used to test the performance of the wrap method from the WorldUtils class.
 */
@Fork(value = 0)
@Warmup(iterations = BenchmarkRunner.WARMUP_ITERATIONS)
@Measurement(iterations = BenchmarkRunner.ITERATIONS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkRunner {
    /** The number of iterations to run the benchmark. */
    static final int ITERATIONS = 5;
    /** The number of warmup iterations to run the benchmark. */
    static final int WARMUP_ITERATIONS = 3;
    /** The length of the string to wrap. */
    static final int WRAP_LENGTH = 20;

    /**
     * State for testing Really Long Strings that are not wrapped.
     *
     */
    @State(Scope.Thread)
    public static class ReallyLongStringState {
        /** Really long string that is not wrapped.  */
        String reallyLongString = "Click here, https://commons.apache.org, to jump to the commons website";
        /** Wrap length is greater than the string length.  */
        int wrapLength = WRAP_LENGTH;
        /** New line string.  */
        String newLineStr = "\n";
        /** Ensures the string is not wrapped.  */
        boolean wrapLongWords;
        /** Wrap on space.  */
        String wrapOn = " ";
    }
    /**
     * State for testing Long Strings that will be wrapped.
     */
    @State(Scope.Thread)
    public static class LongStringState {
        /** Long string that will be wrapped.  */
        String longString = "Click here to jump to the commons website - https://commons.apache.org";
        /** Wrap length is greater than the string length.  */
        int wrapLength = WRAP_LENGTH;
        /** New line string.  */
        String newLineStr = "\n";
        /** Ensures the string is wrapped.  */
        boolean wrapLongWords = true;
        /** Wrap on space.  */
        String wrapOn = " ";
    }

    /**
     * State for testing Normal Strings that fit within the wrap length.
     */
    @State(Scope.Thread)
    public static class NormalStringState {
        /** Normal string that fits within the wrap length.  */
        String normalString = "Here is one line of text that is going to be wrapped after 20 columns.";
        /** Wrap length is greater than the string length.  */
        int wrapLength = WRAP_LENGTH;
        /** New line string.  */
        String newLineStr = "\n";
        /** Ensures the string is wrapped.  */
        boolean wrapLongWords = true;
        /** Wrap on space.  */
        String wrapOn = " ";
    }

    /**
     * Benchmark for Really Long Strings (Not Wrapped).
     */
    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public void testReallyLongString(ReallyLongStringState state) {
        WordUtils.wrap(
                state.reallyLongString,
                state.wrapLength,
                state.newLineStr,
                state.wrapLongWords,
                state.wrapOn
        );
    }

    /**
     * Benchmark for Long Strings (Wrapped).
     */
    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public void testLongString(LongStringState state) {
        WordUtils.wrap(
                state.longString,
                state.wrapLength,
                state.newLineStr,
                state.wrapLongWords,
                state.wrapOn
        );
    }

    /**
     * Benchmark for Normal Strings (Short Enough).
     */
    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public void testNormalString(NormalStringState state) {
        WordUtils.wrap(
                state.normalString,
                state.wrapLength,
                state.newLineStr,
                state.wrapLongWords,
                state.wrapOn
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Measures the time to run a single benchmark method without any warm-up iterations.
    public void testReallyLongStringCold(ReallyLongStringState state) {
        WordUtils.wrap(
                state.reallyLongString,
                state.wrapLength,
                state.newLineStr,
                state.wrapLongWords,
                state.wrapOn
        );
    }

    /**
     * Benchmark for Long Strings (Wrapped).
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Measures the time to run a single benchmark method without any warm-up iterations.
    public void testLongStringCold(LongStringState state) {
        WordUtils.wrap(
                state.longString,
                state.wrapLength,
                state.newLineStr,
                state.wrapLongWords,
                state.wrapOn
        );
    }

    /**
     * Benchmark for Normal Strings (Short Enough).
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime) //Measures the time to run a single benchmark method without any warm-up iterations.
    public void testNormalStringCold(NormalStringState state) {
        WordUtils.wrap(
                state.normalString,
                state.wrapLength,
                state.newLineStr,
                state.wrapLongWords,
                state.wrapOn
        );
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
