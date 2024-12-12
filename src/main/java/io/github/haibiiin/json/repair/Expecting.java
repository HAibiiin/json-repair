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

import java.util.ArrayList;
import java.util.List;

public class Expecting {
    
    private List<Node> list;
    
    public Expecting() {
        this.list = new ArrayList<>();
    }
    
    public void add(int index, String key, List<String> expectinList) {
        this.list.add(new Node(index, key, expectinList));
    }
    
    public boolean none() {
        return list.isEmpty();
    }
    
    public Node node() {
        return this.list.get(0);
    }
    
    public class Node {
        
        private int index;
        
        private String key;
        
        private List<String> expectingList;
        
        public Node(int index, String key, List<String> expectingList) {
            this.index = index;
            this.key = key;
            this.expectingList = expectingList;
        }
        
        public int index() {
            return index;
        }
        
        public String key() {
            return key;
        }
        
        public List<String> expectingList() {
            return expectingList;
        }
    }
}