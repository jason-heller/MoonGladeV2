package engine.utils.math;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean endOfWord;

    Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return endOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        this.endOfWord = endOfWord;
    }

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public void print(StringBuilder buffer, String prefix, String childPrefix, char lastChar) {
		buffer.append(prefix);
		buffer.append(lastChar);
        buffer.append('\n');
        for (Iterator<Character> it = children.keySet().iterator(); it.hasNext();) {
            Character c = it.next();
        	
            TrieNode next = children.get(c);
            if (it.hasNext()) {
            	next.print(buffer, childPrefix, "|-- " + childPrefix, c);
            } else {
            	next.print(buffer, childPrefix, "|__ " , c);
            }
        }
	}
}