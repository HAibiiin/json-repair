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
package io.github.haibiiin.json.repair.antlr;

import org.antlr.v4.runtime.Vocabulary;

public class VocabularyWrapper implements Vocabulary {
    
    private final Vocabulary vocabulary;
    
    public VocabularyWrapper(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }
    
    @Override
    public int getMaxTokenType() {
        return this.vocabulary.getMaxTokenType();
    }
    
    @Override
    public String getLiteralName(int i) {
        return this.vocabulary.getLiteralName(i);
    }
    
    @Override
    public String getSymbolicName(int i) {
        return this.vocabulary.getSymbolicName(i);
    }
    
    @Override
    public String getDisplayName(int i) {
        String defaultDisplayName = this.vocabulary.getDisplayName(i);
        if (defaultDisplayName.startsWith("'") && defaultDisplayName.endsWith("'")) {
            return defaultDisplayName.substring(1, defaultDisplayName.length() - 1);
        }
        return defaultDisplayName;
    }
}
