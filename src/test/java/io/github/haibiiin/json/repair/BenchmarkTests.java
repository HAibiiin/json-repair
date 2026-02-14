/*
 * Copyright 2024 HAibiiin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.haibiiin.json.repair;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class BenchmarkTests {
    
    @Param({
            "{\"f\":\"v\", \"f2\":\"v2\"",
            "{\"f\":\"v\", \"a\":[1",
            "{\"f\":\"v\", \"a\":[1,2], \"o1\":{\"f1\":\"v1\"}, ",
            "\"f\":\"v\", \"a\":[1,2], \"o1\":{\"f1\":\"v1\"}",
            "f:v"
    })
    String anomalyJSON;
    
    @Benchmark
    public void testSimpleRepairStrategy(Blackhole blackhole) {
        JSONRepair repair = new JSONRepair();
        blackhole.consume(repair.handle(anomalyJSON));
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTests.class.getSimpleName())
                .result("./reports/benchmark/benchmark_0.4.0.json")
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opt).run();
    }
    
}
