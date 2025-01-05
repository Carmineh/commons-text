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

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunnerTest {

    @Test
    public void testBenchmarkRunner() throws Exception {
        Options options = new OptionsBuilder()
                .include("org.apache.commons.text.BenchmarkRunner") // Specify the benchmark class
                .forks(0) // Disable JVM forking
                .warmupIterations(1) // Adjust warm-up iterations for testing
                .measurementIterations(1) // Adjust measurement iterations for testing
                .build();

        new Runner(options).run(); // Run benchmarks programmatically
    }

}