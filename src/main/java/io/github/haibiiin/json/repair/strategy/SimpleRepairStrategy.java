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
import io.github.haibiiin.json.repair.RepairStrategy;
import io.github.haibiiin.json.repair.UnableHandleException;
import io.github.haibiiin.json.repair.antlr.KeySymbol;
import java.util.List;
import java.util.function.BiPredicate;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;

public class SimpleRepairStrategy implements RepairStrategy {
    
    @Override
    public String repair(String json, List<ParseTree> beRepairParseList, Expecting expecting) {
        Expecting.Node node = expecting.first();
        SimpleNodeWrapper nodeWrapper = new SimpleNodeWrapper(node);
        
        FixStrategy strategy = FixStrategy.get(nodeWrapper, beRepairParseList);
        if (strategy == null) {
            throw new UnableHandleException();
        }
        
        return strategy.fixStrategy.fix(json, nodeWrapper, beRepairParseList);
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
        
        END_OBJ_OR_PAIR(
                (node, beRepairParseList) -> node.isEOF() && node.endObjOrPair(),
                (json, node, beRepairParseList) -> json + KeySymbol.R_BRACE.val()),
        END_ARR_OR_VALUE(
                (node, beRepairParseList) -> node.isEOF() && node.endArrOrValue(),
                (json, node, beRepairParseList) -> json + KeySymbol.R_BRACKET.val()),
        START_PAIR(
                (node, beRepairParseList) -> node.isEOF() && node.startPair(),
                (json, node, beRepairParseList) -> {
                    int index = -1;
                    for (int i = beRepairParseList.size() - 1; i > 0; i--) {
                        ParseTree parseNode = beRepairParseList.get(i);
                        if (parseNode instanceof ErrorNode) {
                            index = ((ErrorNodeImpl) (beRepairParseList.get(i))).getSymbol().getCharPositionInLine();
                        }
                    }
                    return json.substring(0, index) + KeySymbol.R_BRACE.val();
                }),
        VALUE(
                (node, beRepairParseList) -> node.isEOF() && node.expectingValue(),
                (json, node, beRepairParseList) -> {
                    int index = -1;
                    for (int i = beRepairParseList.size() - 1; i > 0; i--) {
                        ParseTree parseNode = beRepairParseList.get(i);
                        if (parseNode instanceof ErrorNode) {
                            index = ((ErrorNodeImpl) (beRepairParseList.get(i))).getSymbol().getCharPositionInLine();
                        }
                    }
                    if (json.substring(index).contains(KeySymbol.COMMA.val())) {
                        return json.substring(0, index) + KeySymbol.R_BRACKET.val();
                    }
                    if (json.substring(index).contains(KeySymbol.COLON.val())) {
                        return json + KeySymbol.NULL.val() + KeySymbol.R_BRACE.val();
                    }
                    throw new UnableHandleException();
                }),
        EOF(
                (node, beRepairParseList) -> node.expectingEOF(),
                (json, node, beRepairParseList) -> KeySymbol.L_BRACE.val() + json),
        CLOSE_QUOTATION_MARK(
                (node, beRepairParseList) -> node.expectingToken() && node.key().startsWith("\""),
                (json, node, beRepairParseList) -> {
                    if (json.lastIndexOf(KeySymbol.COMMA.val()) != -1) {
                        ParseTree errorNode = beRepairParseList.stream().filter((parse) -> parse instanceof ErrorNode).findFirst().orElse(null);
                        if (errorNode != null && KeySymbol.COLON.val().equalsIgnoreCase(errorNode.getText())) {
                            return json.replaceFirst(node.key(), node.key().substring(0, node.key().length() - 1) + "\",");
                        }
                    }
                    return json.replaceFirst(node.key(), node.key() + "\"");
                }),
        OPEN_QUOTATION_MARK(
                (node, beRepairParseList) -> node.expectingToken() && node.key().endsWith("\""),
                (json, node, beRepairParseList) -> json.replaceFirst(node.key(), "\"" + node.key())),
        STRING_QUOTATION_MARK(
                (node, beRepairParseList) -> node.expectingToken() && node.key().endsWith(KeySymbol.COLON.val()),
                (json, node, beRepairParseList) -> json.replaceFirst(node.key(), "\"" + node.key().substring(0, node.key().length() - 1) + "\":")),
        VALUE_QUOTATION_MARK(
                (node, beRepairParseList) -> node.expectingToken(),
                (json, node, beRepairParseList) -> json.replaceFirst(node.key(), "\"" + node.key() + "\"")),
        CLOSE_BRACE(
                (node, beRepairParseList) -> beRepairParseList.stream().anyMatch(
                        (parse) -> parse instanceof ErrorNode
                                && node.key().equalsIgnoreCase(parse.getText()) && node.expectingObj()),
                (json, node, beRepairParseList) -> {
                    final int[] index = {-1};
                    beRepairParseList.stream().filter(
                            (parse) -> parse instanceof ErrorNode && node.key().equalsIgnoreCase(parse.getText())).findFirst()
                            .ifPresent(parseTree -> index[0] = ((ErrorNodeImpl) parseTree).getSymbol().getCharPositionInLine());
                    if (index[0] == -1) {
                        throw new UnableHandleException();
                    }
                    if (index[0] == json.length() - 1) {
                        return json + KeySymbol.R_BRACE.val();
                    }
                    String prefix = json.substring(0, index[0] + 1);
                    String suffix = json.substring(index[0] + 1);
                    return prefix + KeySymbol.R_BRACE.val() + suffix;
                }),
        CLOSE_BRACKET(
                (node, beRepairParseList) -> beRepairParseList.stream().anyMatch(
                        (parse) -> parse instanceof ErrorNode
                                && node.key().equalsIgnoreCase(parse.getText()) && node.expectingArr()),
                (json, node, beRepairParseList) -> {
                    final int[] index = {-1};
                    beRepairParseList.stream().filter(
                            (parse) -> parse instanceof ErrorNode && node.key().equalsIgnoreCase(parse.getText())).findFirst()
                            .ifPresent(parseTree -> index[0] = ((ErrorNodeImpl) parseTree).getSymbol().getCharPositionInLine());
                    if (index[0] == -1) {
                        throw new UnableHandleException();
                    }
                    if (index[0] == json.length() - 1) {
                        return json + KeySymbol.R_BRACKET.val();
                    }
                    String prefix = json.substring(0, index[0] + 1);
                    String suffix = json.substring(index[0] + 1);
                    return prefix + KeySymbol.R_BRACKET.val() + suffix;
                }),
                ;
        
        final BiPredicate<SimpleNodeWrapper, List<ParseTree>> expectingFixStrategy;
        final BaseFixStrategy fixStrategy;
        
        FixStrategy(BiPredicate<SimpleNodeWrapper, List<ParseTree>> expectingFixStrategy, BaseFixStrategy fixStrategy) {
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
        
        String fix(String json, SimpleNodeWrapper node, List<ParseTree> beRepairParseList);
    }
}
