package engine.utils.math;

import java.util.ArrayList;
import java.util.List;

public class Trie {
	private TrieNode root;

	public Trie() {
		root = new TrieNode();
	}

	public void insert(String word) {
		TrieNode current = root;

		for (char l : word.toCharArray()) {
			current = current.getChildren().computeIfAbsent(l, c -> new TrieNode());
		}
		current.setEndOfWord(true);
	}

	public boolean remove(String word) {
		return remove(root, word, 0);
	}

	private boolean remove(TrieNode current, String word, int index) {
		if (index == word.length()) {
			if (!current.isEndOfWord())
				return false;

			current.setEndOfWord(false);
			return current.getChildren().isEmpty();
		}

		char ch = word.charAt(index);
		TrieNode node = current.getChildren().get(ch);

		if (node == null)
			return false;

		boolean shouldDelete = remove(node, word, index + 1) && !node.isEndOfWord();

		if (shouldDelete) {
			current.getChildren().remove(ch);
			return current.getChildren().isEmpty();
		}

		return false;
	}

	public boolean find(String word) {
		TrieNode current = root;
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			TrieNode node = current.getChildren().get(ch);
			if (node == null) {
				return false;
			}
			current = node;
		}
		return current.isEndOfWord();
	}

	public List<String> getSuggestions(String query) {
		List<String> suggestions = new ArrayList<>();
		TrieNode pCrawl = root;
		StringBuffer currPrefix = new StringBuffer();

		for (char c : query.toCharArray()) {
			pCrawl = pCrawl.getChildren().get(c);

			// no string in the Trie has this prefix
			if (pCrawl == null)
				return suggestions;

			currPrefix.append(c);
		}

		suggestionsRec(suggestions, pCrawl, currPrefix);

		return suggestions;
	}

	private void suggestionsRec(List<String> suggestions, TrieNode root, StringBuffer currPrefix) {
		if (root.isEndOfWord())
			suggestions.add(currPrefix.toString());

		if (root.getChildren().isEmpty())
			return;

		for (char c : root.getChildren().keySet()) {
			TrieNode node = root.getChildren().get(c);
			suggestionsRec(suggestions, node, currPrefix.append(c));
			currPrefix.setLength(currPrefix.length() - 1);
		}
	}

	public boolean contains(String word) {
		TrieNode current = root;

		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			TrieNode node = current.getChildren().get(ch);

			if (node == null)
				return false;

			current = node;
		}
		return current.isEndOfWord();
	}

	public boolean isEmpty() {
		return (root == null);
	}

	@Override
	public String toString() {
		if (root == null) {
			return "empty trie";
		}

		StringBuilder buffer = new StringBuilder(50);
		root.print(buffer, "", "", ' ');
		return buffer.toString();
	}
}
