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

import io.github.haibiiin.json.repair.strategy.CorrectRepairStrategy;
import io.github.haibiiin.json.repair.test.kits.FixerStrategy;
import io.github.haibiiin.json.repair.test.kits.TestCaseArgumentsProvider;
import io.github.haibiiin.json.repair.test.kits.TestCaseSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class FixerTests {
    
    @ParameterizedTest(name = "{0} > {1}")
    @TestCaseSource(path = "/case/simple.xml", type = FixerStrategy.SIMPLE)
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void testSimpleRepair(String anomaly, String correct) {
        JSONRepair repair = new JSONRepair();
        Assertions.assertEquals(correct.trim(), repair.handle(anomaly).trim());
    }
    
    @ParameterizedTest(name = "{0} > {1}")
    @TestCaseSource(path = "/case/extract.xml", type = FixerStrategy.SIMPLE)
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void testSimpleExtract(String anomaly, String correct) {
        JSONRepairConfig config = new JSONRepairConfig();
        config.enableExtractJSON();
        JSONRepair repair = new JSONRepair(config);
        Assertions.assertEquals(correct.trim(), repair.handle(anomaly).trim());
    }
    
    @ParameterizedTest(name = "{0} > {1}")
    @TestCaseSource(path = "/case/correct.xml", type = FixerStrategy.CORRECT)
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void testCorrectRepair(String anomaly, String correct, String reference) {
        JSONRepair repair = new JSONRepair(new CorrectRepairStrategy(reference));
        Assertions.assertEquals(correct.trim(), repair.handle(anomaly).trim());
    }
}
