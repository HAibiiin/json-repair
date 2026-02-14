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

import io.github.haibiiin.json.repair.antlr.DefaultErrorStrategyWrapper;
import io.github.haibiiin.json.repair.antlr.SyntaxErrorListener;
import io.github.haibiiin.json.repair.antlr.autogen.JSONLexer;
import io.github.haibiiin.json.repair.antlr.autogen.JSONParser;
import io.github.haibiiin.json.repair.strategy.SimpleExtractStrategy;
import io.github.haibiiin.json.repair.strategy.SimpleRepairStrategy;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

public class JSONRepair {
    
    private final RepairStrategy repairStrategy;
    
    private final ExtractStrategy extractStrategy;
    
    private final JSONRepairConfig properties;
    
    public JSONRepair() {
        this.repairStrategy = new SimpleRepairStrategy();
        this.extractStrategy = new SimpleExtractStrategy();
        this.properties = new JSONRepairConfig();
    }
    
    public JSONRepair(RepairStrategy repairStrategy) {
        this.repairStrategy = repairStrategy;
        this.extractStrategy = new SimpleExtractStrategy();
        this.properties = new JSONRepairConfig();
    }
    
    public JSONRepair(JSONRepairConfig config) {
        this.repairStrategy = new SimpleRepairStrategy();
        this.extractStrategy = new SimpleExtractStrategy();
        this.properties = config;
    }
    
    public JSONRepair(RepairStrategy repairStrategy, JSONRepairConfig config) {
        this.repairStrategy = repairStrategy;
        this.extractStrategy = new SimpleExtractStrategy();
        this.properties = config;
    }
    
    public JSONRepair(RepairStrategy repairStrategy, ExtractStrategy extractStrategy, JSONRepairConfig properties) {
        this.repairStrategy = repairStrategy;
        this.extractStrategy = extractStrategy;
        this.properties = properties;
    }
    
    public String handle(String beRepairJSON) throws RepairFailureException {
        beRepairJSON = preprocessJSON(beRepairJSON);
        
        CharStream charStream = CharStreams.fromString(beRepairJSON);
        JSONLexer lexer = new JSONLexer(charStream);
        JSONParser parser = new JSONParser(new CommonTokenStream(lexer));
        Expecting expecting = new Expecting();
        SyntaxErrorListener syntaxErrorListener = new SyntaxErrorListener(new DefaultErrorStrategyWrapper(), expecting);
        
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorListener);
        parser.addErrorListener(syntaxErrorListener);
        
        JSONParser.JsonContext ctx = parser.json();
        
        if (correct(expecting)) {
            return beRepairJSON;
        }
        
        int maxTryTimes = Math.max(expecting.sum(), this.properties.maxTryTimes());
        List<ParseTree> beRepairParseList = ParserListBuilder.build(ctx);
        
        if (this.properties.extractJSON()) {
            String repairJSON = extractStrategy.extract(beRepairJSON, beRepairParseList, expecting);
            return subHandle(repairJSON, maxTryTimes, 0);
        } else {
            String repairJSON = repairStrategy.repair(beRepairJSON, beRepairParseList, expecting);
            return subHandle(repairJSON, maxTryTimes, 0);
        }
    }
    
    private @NotNull String preprocessJSON(String beRepairJSON) {
        if (!this.properties.lineFeed()) {
            beRepairJSON = beRepairJSON.replaceAll("\\s*[\\r\\n]+\\s*", "");
        }
        // Fix redundant leading quote in JSON keys. This pattern matches empty string followed by key name: ""key" and replaces it with "key"
        beRepairJSON = beRepairJSON.replaceAll("\"\"([^\",:{}\\[\\]\\s]+)\"", "\"$1\"");
        // Remove non-JSON characters (e.g. parentheses) appearing after structural tokens
        beRepairJSON = beRepairJSON.replaceAll("(?<=[]},])\\s*[^\\s\\w\"{}\\[\\]:,.\\-]+\\s*(?=[]}]|$)", "");
        return beRepairJSON;
    }
    
    public String subHandle(String beRepairJSON, int maxTryTimes, int tryTimes) {
        if (tryTimes == maxTryTimes) {
            throw new OverstepTryTimesException();
        }
        CharStream charStream = CharStreams.fromString(beRepairJSON);
        JSONLexer lexer = new JSONLexer(charStream);
        JSONParser parser = new JSONParser(new CommonTokenStream(lexer));
        Expecting expecting = new Expecting();
        SyntaxErrorListener syntaxErrorListener = new SyntaxErrorListener(new DefaultErrorStrategyWrapper(), expecting);
        
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorListener);
        parser.addErrorListener(syntaxErrorListener);
        
        JSONParser.JsonContext ctx = parser.json();
        
        if (correct(expecting)) {
            return beRepairJSON;
        }
        List<ParseTree> beRepairParseList = ParserListBuilder.build(ctx);
        String repairJSON = repairStrategy.repair(beRepairJSON, beRepairParseList, expecting);
        tryTimes++;
        return subHandle(repairJSON, maxTryTimes, tryTimes);
    }
    
    private boolean correct(Expecting expecting) {
        return expecting.none();
    }
    
}
