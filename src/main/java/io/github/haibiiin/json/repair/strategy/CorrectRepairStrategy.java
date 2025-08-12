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
package io.github.haibiiin.json.repair.strategy;

import io.github.haibiiin.json.repair.Expecting;
import io.github.haibiiin.json.repair.ParserListBuilder;
import io.github.haibiiin.json.repair.RepairStrategy;
import io.github.haibiiin.json.repair.UnableHandleException;
import io.github.haibiiin.json.repair.antlr.KeySymbol;
import io.github.haibiiin.json.repair.antlr.autogen.JSONLexer;
import io.github.haibiiin.json.repair.antlr.autogen.JSONParser;
import java.util.List;
import java.util.function.BiPredicate;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class CorrectRepairStrategy implements RepairStrategy {
    
    private final List<ParseTree> correctJSONParseList;
    
    public CorrectRepairStrategy(String correctJSONRef) {
        CharStream charStream = CharStreams.fromString(correctJSONRef);
        JSONLexer lexer = new JSONLexer(charStream);
        JSONParser parser = new JSONParser(new CommonTokenStream(lexer));
        
        JSONParser.JsonContext ctx = parser.json();
        correctJSONParseList = ParserListBuilder.build(ctx);
    }
    
    @Override
    public String repair(String json, List<ParseTree> beRepairParseList, Expecting expecting) {
        Expecting.Node node = expecting.first();
        SimpleNodeWrapper nodeWrapper = new SimpleNodeWrapper(node);
        
        FixStrategy strategy = FixStrategy.get(nodeWrapper, beRepairParseList);
        if (strategy == null) {
            throw new UnableHandleException();
        }
        
        return strategy.fixStrategy.fix(json, nodeWrapper, beRepairParseList, correctJSONParseList);
    }
    
    static class SimpleNodeWrapper {
        
        private final Expecting.Node node;
        
        private SimpleNodeWrapper(Expecting.Node node) {
            this.node = node;
        }
        
        public String key() {
            return node.key();
        }
        
        public boolean isEOF() {
            return KeySymbol.EOF.val().equalsIgnoreCase(node.key());
        }
        
        public boolean endObjOrPair() {
            return node.expectingList().size() == 2
                    && node.expectingList().contains(KeySymbol.COMMA.val())
                    && node.expectingList().contains(KeySymbol.R_BRACE.val());
        }
        
        public boolean endArrOrValue() {
            return node.expectingList().size() == 2
                    && node.expectingList().contains(KeySymbol.COMMA.val())
                    && node.expectingList().contains(KeySymbol.R_BRACKET.val());
        }
        
        public boolean startPair() {
            return node.expectingList().size() == 1
                    && node.expectingList().contains(KeySymbol.STRING.val());
        }
        
        public boolean expectingValue() {
            return node.expectingList().size() == 7
                    && node.expectingList().contains(KeySymbol.L_BRACE.val())
                    && node.expectingList().contains(KeySymbol.L_BRACKET.val())
                    && node.expectingList().contains(KeySymbol.TRUE.val())
                    && node.expectingList().contains(KeySymbol.FALSE.val())
                    && node.expectingList().contains(KeySymbol.NULL.val())
                    && node.expectingList().contains(KeySymbol.STRING.val())
                    && node.expectingList().contains(KeySymbol.NUMBER.val());
        }
        
        public boolean expectingObj() {
            return node.expectingList().size() == 11;
        }
        
        public boolean expectingArr() {
            return node.expectingList().size() == 7;
        }
        
        public boolean expectingEOF() {
            return KeySymbol.COLON.val().equalsIgnoreCase(node.key())
                    && node.expectingList().size() == 1
                    && node.expectingList().contains(KeySymbol.EOF.val());
        }
        
        public boolean expectingToken() {
            return node.expectingList().size() == 1 && node.expectingList().contains(KeySymbol.TOKEN.val());
        }
    }
    
    enum FixStrategy {
        
        OBJ_OR_PAIR_EOF(
                (node, beRepairParseList) -> node.isEOF() && node.endObjOrPair(),
                (json, node, beRepairParseList, correctParseList) -> {
                    int index = -1;
                    for (int i = 0; i < beRepairParseList.size(); i++) {
                        if (beRepairParseList.get(i).getText().equalsIgnoreCase(node.key())) {
                            index = i;
                            break;
                        }
                    }
                    if (index == -1 || index > correctParseList.size()) {
                        throw new UnableHandleException();
                    }
                    ParseTree correctParseTree = correctParseList.get(index);
                    if (correctParseTree == null) {
                        throw new UnableHandleException();
                    }
                    return json + correctParseTree.getText();
                }),
                ;
        
        final BiPredicate<SimpleNodeWrapper, List<ParseTree>> expectingFixStrategy;
        final BaseFixStrategy fixStrategy;
        
        FixStrategy(BiPredicate<CorrectRepairStrategy.SimpleNodeWrapper, List<ParseTree>> expectingFixStrategy, BaseFixStrategy fixStrategy) {
            this.expectingFixStrategy = expectingFixStrategy;
            this.fixStrategy = fixStrategy;
        }
        
        public static FixStrategy get(SimpleNodeWrapper node, List<ParseTree> parseTrees) {
            for (FixStrategy strategy : values()) {
                if (strategy.expectingFixStrategy.test(node, parseTrees)) {
                    return strategy;
                }
            }
            return null;
        }
    }
    
    interface BaseFixStrategy {
        
        String fix(String json, SimpleNodeWrapper node, List<ParseTree> beRepairParseList, List<ParseTree> correctParseList);
    }
}
