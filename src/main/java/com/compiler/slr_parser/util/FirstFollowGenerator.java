package com.compiler.slr_parser.util;


import com.compiler.slr_parser.model.FirstFollow;
import com.compiler.slr_parser.model.Grammar;
import com.compiler.slr_parser.model.ProductionRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class FirstFollowGenerator {

    public FirstFollow computeFirstFollow(Grammar grammar) {
        FirstFollow firstFollow = new FirstFollow();
        ArrayList<String> nonTerminals = new ArrayList<>();

        // Collect all non-terminals from the grammar
        for (int i = 0; i < grammar.getProductionRules().size(); i++) {
            String lhs = grammar.getProductionRules().get(i).getLHS();
            if (!nonTerminals.contains(lhs)) {
                nonTerminals.add(lhs);
            }
        }
        firstFollow.setNonTerminals(nonTerminals);

        // Compute FIRST and FOLLOW sets
        HashMap<String, HashSet<String>> first = computeFirst(grammar, nonTerminals);
        HashMap<String, HashSet<String>> follow = computeFollow(grammar, nonTerminals, first);

        // Create ArrayLists to hold merged strings for FIRST and FOLLOW sets
        ArrayList<String> mergedFirst = new ArrayList<>();
        ArrayList<String> mergedFollow = new ArrayList<>();

        // Iterate over non-terminals to populate the merged lists
        for (String nonTerminal : nonTerminals) {
            // Merge FIRST set into a single string
            String firstMerged = mergeSetToString(first.get(nonTerminal));
            mergedFirst.add(firstMerged);

            // Merge FOLLOW set into a single string
            String followMerged = mergeSetToString(follow.get(nonTerminal));
            mergedFollow.add(followMerged);
        }

        // Assuming firstFollow has methods to set these ArrayLists
        firstFollow.setFirst(mergedFirst);
        firstFollow.setFollow(mergedFollow);

        return firstFollow;
    }

    private String mergeSetToString(HashSet<String> set) {
        StringBuilder sb = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                sb.append(s); // Concatenate each element
            }
        }
        return sb.toString(); // Return the concatenated string
    }

    private HashMap<String, HashSet<String>> computeFirst(Grammar grammar, ArrayList<String> nonTerminals) {
        HashMap<String, HashSet<String>> firstSets = new HashMap<>();

        // Initialize FIRST sets
        for (String nonTerminal : nonTerminals) {
            firstSets.put(nonTerminal, new HashSet<>());
        }

        boolean changed;

        do {
            changed = false;

            for (ProductionRule rule : grammar.getProductionRules()) {
                String lhs = rule.getLHS();
                String rhs = rule.getRHS();

                // Process each symbol in the RHS
                for (char symbol : rhs.toCharArray()) {
                    if (Character.isUpperCase(symbol)) { // Non-terminal
                        HashSet<String> firstOfNonTerminal = firstSets.get(String.valueOf(symbol));
                        if (firstOfNonTerminal != null) {
                            for (String f : firstOfNonTerminal) {
                                if (!firstSets.get(lhs).contains(f)) {
                                    firstSets.get(lhs).add(f);
                                    changed = true;
                                }
                            }
                        }
                        // If the non-terminal can derive ε (epsilon), continue to next symbol
                        if (!firstOfNonTerminal.contains("ε")) {
                            break;
                        }
                    } else { // Terminal
                        if (!firstSets.get(lhs).contains(String.valueOf(symbol))) {
                            firstSets.get(lhs).add(String.valueOf(symbol));
                            changed = true;
                        }
                        break; // Stop processing after finding a terminal
                    }
                }
            }
        } while (changed); // Repeat until no changes occur

        return firstSets;
    }

    private HashMap<String, HashSet<String>> computeFollow(Grammar grammar, ArrayList<String> nonTerminals,
                                                           HashMap<String, HashSet<String>> first) {
        HashMap<String, HashSet<String>> followSets = new HashMap<>();

        // Initialize FOLLOW sets
        for (String nonTerminal : nonTerminals) {
            followSets.put(nonTerminal, new HashSet<>());
        }

        // Add $ to the FOLLOW set of the start symbol
        String startSymbol = grammar.getProductionRules().get(0).getLHS();
        followSets.get(startSymbol).add("$");

        boolean changed;

        do {
            changed = false;

            for (ProductionRule rule : grammar.getProductionRules()) {
                String lhs = rule.getLHS();
                String rhs = rule.getRHS();

                for (int i = 0; i < rhs.length(); i++) {
                    char symbol = rhs.charAt(i);

                    if (Character.isUpperCase(symbol)) { // Non-terminal
                        // Check what follows this non-terminal
                        if (i + 1 < rhs.length()) { // If there's a symbol following it
                            char nextSymbol = rhs.charAt(i + 1);
                            if (Character.isUpperCase(nextSymbol)) { // Next is a non-terminal
                                for (String f : first.get(String.valueOf(nextSymbol))) {
                                    if (!followSets.get(String.valueOf(symbol)).contains(f)) {
                                        followSets.get(String.valueOf(symbol)).add(f);
                                        changed = true;
                                    }
                                }
                                // If next can derive ε, add FOLLOW of LHS to this non-terminal's FOLLOW set
                                if (first.get(String.valueOf(nextSymbol)).contains("ε")) {
                                    for (String f : followSets.get(lhs)) {
                                        if (!followSets.get(String.valueOf(symbol)).contains(f)) {
                                            followSets.get(String.valueOf(symbol)).add(f);
                                            changed = true;
                                        }
                                    }
                                }
                            } else { // Next is a terminal
                                if (!followSets.get(String.valueOf(symbol)).contains(String.valueOf(nextSymbol))) {
                                    followSets.get(String.valueOf(symbol)).add(String.valueOf(nextSymbol));
                                    changed = true;
                                }
                            }
                        } else { // No symbol follows this non-terminal
                            for (String f : followSets.get(lhs)) {
                                if (!followSets.get(String.valueOf(symbol)).contains(f)) {
                                    followSets.get(String.valueOf(symbol)).add(f);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        } while (changed); // Repeat until no changes occur

        return followSets;
    }

}



//public class FirstFollowGenerator {
//
//    public FirstFollow computeFirstFollow(Grammar grammar){
//        FirstFollow firstFollow = new FirstFollow();
//        ArrayList<String> nonTerminals = new ArrayList<>();
//        for(int i = 0; i < grammar.getProductionRules().size(); i++) {
//            if(!nonTerminals.contains(grammar.getProductionRules().get(i).getLHS())) {
//                nonTerminals.add(grammar.getProductionRules().get(i).getLHS());
//            }
//        }
//        firstFollow.setNonTerminals(nonTerminals);
//
//        ArrayList<String> first = computeFirst(grammar, nonTerminals);
//        ArrayList<String> follow = computeFollow(grammar, nonTerminals);
//
//        firstFollow.setFirst(first);
//        firstFollow.setFollow(follow);
//
//        return firstFollow;
//    }
//
//    private ArrayList<String> computeFirst(Grammar grammar, ArrayList<String> nonTerminals){
//        ArrayList<String> first = new ArrayList<>();
//        return first;
//    }
//
//    private ArrayList<String> computeFollow(Grammar grammar, ArrayList<String> nonTerminals){
//        ArrayList<String> follow = new ArrayList<>();
//        return follow;
//    }
//
//}
