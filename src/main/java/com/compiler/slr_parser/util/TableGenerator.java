package com.compiler.slr_parser.util;

import com.compiler.slr_parser.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TableGenerator {
    private ParsingTable parsingTable = null;
    private ArrayList<ProductionRule> allRules;
    private static final int MAX_STATES = 1000;

    public ParsingTable tableGenerator(Grammar grammar) {
        parsingTable = new ParsingTable(grammar);
        allRules = new ArrayList<>();

        String startSymbol = grammar.getStartSymbol().toString();
        ProductionRule auxiliaryRule = new ProductionRule();
        auxiliaryRule.setLHS(startSymbol + "'");
        auxiliaryRule.setRHS(startSymbol);

        allRules.add(auxiliaryRule);
        allRules.addAll(grammar.getProductionRules());

        ArrayList<LR0Item> states = new ArrayList<>();
        LR0Item initialItem = createInitialItem(grammar);
        states.add(initialItem);

        FirstFollowGenerator firstFollowGenerator = new FirstFollowGenerator();
        FirstFollow firstFollow = firstFollowGenerator.computeFirstFollow(grammar);

        int i = 0;
        while (i < states.size()) {
            if (states.size() >= MAX_STATES) {
                throw new RuntimeException("Grammar generates too many states. Possible left recursion or ambiguity.");
            }
            processState(states.get(i), grammar, i, firstFollow, states);
            i++;
        }

        addAcceptState(states, startSymbol);

        return parsingTable;
    }

    private LR0Item createInitialItem(Grammar grammar) {
        ArrayList<ProductionRule> initialRules = new ArrayList<>();
        ArrayList<Integer> initialDots = new ArrayList<>();

        initialRules.add(allRules.get(0));
        initialDots.add(0);

        LR0Item item = new LR0Item(initialRules, initialDots, grammar);
        computeClosure(item, grammar);
        return item;
    }

    private void processState(LR0Item state, Grammar grammar, int stateNo,
                              FirstFollow firstFollow, ArrayList<LR0Item> states) {
        Map<Character, ArrayList<Integer>> symbolGroups = new HashMap<>();

        for (int i = 0; i < state.getProductionRules().size(); i++) {
            ProductionRule rule = state.getProductionRules().get(i);
            int dotPos = state.getDotPosition().get(i);

            if (dotPos < rule.getRHS().length()) {
                char nextSymbol = rule.getRHS().charAt(dotPos);
                symbolGroups.computeIfAbsent(nextSymbol, k -> new ArrayList<>()).add(i);
            } else {
                handleReduce(rule, stateNo, firstFollow, findOriginalRuleIndex(rule));
            }
        }

        for (Map.Entry<Character, ArrayList<Integer>> entry : symbolGroups.entrySet()) {
            char symbol = entry.getKey();
            ArrayList<Integer> ruleIndices = entry.getValue();

            LR0Item newState = createNewState(state, ruleIndices, grammar);
            computeClosure(newState, grammar);

            int nextStateNo = addState(states, newState);
            updateTable(stateNo, symbol, nextStateNo);
        }
    }

    private void handleReduce(ProductionRule rule, int stateNo,
                              FirstFollow firstFollow, int ruleIndex) {
        int nonTerminalIndex = firstFollow.getNonTerminals().indexOf(rule.getLHS());
        if (nonTerminalIndex != -1) {
            String followSet = firstFollow.getFollow().get(nonTerminalIndex);
            if (followSet != null) {
                for (char terminal : followSet.toCharArray()) {
                    addReduceAction(stateNo, terminal, ruleIndex);
                }
            }
        }
    }

    private void addReduceAction(int stateNo, char terminal, int ruleIndex) {
        int terminalIndex = parsingTable.getTerminal().indexOf(String.valueOf(terminal));
        if (terminalIndex != -1) {
            ensureActionTableInitialized(stateNo);
            String reduceAction = "reduce " + ruleIndex;

            String currentAction = parsingTable.actionTable.get(stateNo)[terminalIndex];
            if (currentAction == null) {
                parsingTable.actionTable.get(stateNo)[terminalIndex] = reduceAction;
            } else if (!currentAction.equals(reduceAction)) {
                if (currentAction.startsWith("shift")) {
                    parsingTable.actionTable.get(stateNo)[terminalIndex] = "shift-reduce conflict";
                } else if (currentAction.startsWith("reduce")) {
                    parsingTable.actionTable.get(stateNo)[terminalIndex] = "reduce-reduce conflict";
                }
            }
        }
    }

    private LR0Item createNewState(LR0Item currentState,
                                   ArrayList<Integer> ruleIndices,
                                   Grammar grammar) {
        ArrayList<ProductionRule> newRules = new ArrayList<>();
        ArrayList<Integer> newDots = new ArrayList<>();

        for (int index : ruleIndices) {
            ProductionRule rule = currentState.getProductionRules().get(index);
            int dotPos = currentState.getDotPosition().get(index);

            newRules.add(rule);
            newDots.add(dotPos + 1);
        }

        return new LR0Item(newRules, newDots, grammar);
    }

    private void computeClosure(LR0Item item, Grammar grammar) {
        boolean changed;
        do {
            changed = false;
            ArrayList<ProductionRule> rules = new ArrayList<>(item.getProductionRules());
            ArrayList<Integer> dots = new ArrayList<>(item.getDotPosition());

            for (int i = 0; i < rules.size(); i++) {
                ProductionRule rule = rules.get(i);
                int dotPos = dots.get(i);

                if (dotPos < rule.getRHS().length()) {
                    char nextSymbol = rule.getRHS().charAt(dotPos);
                    if (Character.isUpperCase(nextSymbol)) {
                        for (ProductionRule prod : allRules) {
                            if (prod.getLHS().equals(String.valueOf(nextSymbol))) {
                                if (addToItem(item, prod, 0)) {
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        } while (changed);
    }

    private boolean addToItem(LR0Item item, ProductionRule rule, int dotPos) {
        ArrayList<ProductionRule> rules = item.getProductionRules();
        ArrayList<Integer> dots = item.getDotPosition();

        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).equals(rule) && dots.get(i) == dotPos) {
                return false;
            }
        }

        rules.add(rule);
        dots.add(dotPos);
        return true;
    }

    private int addState(ArrayList<LR0Item> states, LR0Item newState) {
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).equals(newState)) {
                return i;
            }
        }
        states.add(newState);
        return states.size() - 1;
    }

    private void updateTable(int fromState, char symbol, int toState) {
        if (Character.isUpperCase(symbol)) {
            int nonTerminalIndex = parsingTable.getNonTerminal().indexOf(String.valueOf(symbol));
            if (nonTerminalIndex != -1) {
                ensureGotoTableInitialized(fromState);
                parsingTable.gotoTable.get(fromState)[nonTerminalIndex] = String.valueOf(toState);
            }
        } else {
            int terminalIndex = parsingTable.getTerminal().indexOf(String.valueOf(symbol));
            if (terminalIndex != -1) {
                ensureActionTableInitialized(fromState);
                String shiftAction = "shift " + toState;

                String currentAction = parsingTable.actionTable.get(fromState)[terminalIndex];
                if (currentAction == null) {
                    parsingTable.actionTable.get(fromState)[terminalIndex] = shiftAction;
                } else if (!currentAction.equals(shiftAction)) {
                    if (currentAction.startsWith("reduce")) {
                        parsingTable.actionTable.get(fromState)[terminalIndex] = "shift-reduce conflict";
                    } else {
                        parsingTable.actionTable.get(fromState)[terminalIndex] = "shift-shift conflict";
                    }
                }
            }
        }
    }

    private int findOriginalRuleIndex(ProductionRule rule) {
        for (int i = 0; i < allRules.size(); i++) {
            if (allRules.get(i).equals(rule)) {
                return i;
            }
        }
        return -1;
    }

    private void ensureActionTableInitialized(int stateNo) {
        while (parsingTable.actionTable.size() <= stateNo) {
            parsingTable.actionTable.add(new String[parsingTable.getTerminal().size()]);
        }
    }

    private void ensureGotoTableInitialized(int stateNo) {
        while (parsingTable.gotoTable.size() <= stateNo) {
            parsingTable.gotoTable.add(new String[parsingTable.getNonTerminal().size()]);
        }
    }

    private void addAcceptState(ArrayList<LR0Item> states, String startSymbol) {
        for (int i = 0; i < states.size(); i++) {
            LR0Item state = states.get(i);
            for (int j = 0; j < state.getProductionRules().size(); j++) {
                ProductionRule rule = state.getProductionRules().get(j);
                int dotPos = state.getDotPosition().get(j);

                if (rule.getLHS().equals(startSymbol + "'") && dotPos == rule.getRHS().length()) {
                    int endMarkerIndex = parsingTable.getTerminal().indexOf("$");
                    if (endMarkerIndex != -1) {
                        ensureActionTableInitialized(i);
                        parsingTable.actionTable.get(i)[endMarkerIndex] = "accept";
                    }
                    return;
                }
            }
        }
    }
}




//public class TableGenerator {
//
//    private ParsingTable parsingTable = null;
//
//    public ParsingTable tableGenerator(Grammar grammar) {
//        parsingTable = new ParsingTable(grammar);
//
//        // Augmenting grammar
//        String startSymbol = grammar.getStartSymbol().toString();
//        ProductionRule auxiliaryRule = new ProductionRule();
//        auxiliaryRule.setLHS(startSymbol + "'");
//        auxiliaryRule.setRHS(startSymbol);
//
//        ArrayList<ProductionRule> augmentedRules = grammar.getProductionRules();
//        augmentedRules.add(0, auxiliaryRule); // Use add(0, auxiliaryRule) instead of addFirst
//        ArrayList<Integer> dotPosition = new ArrayList<>();
//
//        for (int i = 0; i < augmentedRules.size(); i++) {
//            dotPosition.add(0);
//        }
//
//        ArrayList<LR0Item> I = new ArrayList<>();
//        I.add(new LR0Item(augmentedRules, dotPosition, grammar));
//
//        // Compute FIRST and FOLLOW sets
//        FirstFollowGenerator firstFollowGenerator = new FirstFollowGenerator();
//        FirstFollow firstFollow = firstFollowGenerator.computeFirstFollow(grammar);
//
//        int i = 0;
//        generateItem(I.get(i), grammar, i++, firstFollow);
//
//        return parsingTable;
//    }
//
//    private void generateItem(LR0Item prevItem, Grammar grammar, int itemNo, FirstFollow firstFollow) {
//        ArrayList<LR0Item> newItems = new ArrayList<>();
//        ArrayList<ProductionRule> prevRules = prevItem.getProductionRules();
//        ArrayList<Integer> prevPosition = prevItem.getDotPosition();
//
//        // Iterate through previous production rules
//        for (int i = 0; i < prevRules.size(); i++) {
//            ArrayList<ProductionRule> prodRules = new ArrayList<>();
//            ArrayList<Integer> dotPosition = new ArrayList<>();
//
//            ProductionRule prodRule = prevRules.get(i);
//            Integer dotPos = prevPosition.get(i);
//
//            if (dotPos < prodRule.getRHS().length()) {
//                char token = prodRule.getRHS().charAt(dotPos);
//                dotPos++;
//                prodRules.add(prodRule);
//                dotPosition.add(dotPos);
//
//                // Remove current rule and position
//                prevRules.remove(i);
//                prevPosition.remove(i);
//                i--;
//
//                // Check remaining rules for the same token
//                for (int j = 0; j < prevRules.size(); j++) {
//                    if (prevRules.get(j).getRHS().charAt(prevPosition.get(j)) == token) {
//                        prodRules.add(prevRules.get(j));
//                        dotPosition.add(prevPosition.get(j) + 1);
//                    }
//                }
//
//                // Update parsing table for shift/reduce conflict detection
//                if (Character.isUpperCase(token)) {
//                    int index = parsingTable.getNonTerminal().indexOf(String.valueOf(token));
//                    parsingTable.gotoTable.get(itemNo)[index] = String.valueOf(i + index);
//                } else {
//                    int index = parsingTable.getTerminal().indexOf(String.valueOf(token));
//                    if (parsingTable.actionTable.get(itemNo) == null) {
//                        parsingTable.actionTable.set(itemNo, new String[parsingTable.getTerminal().size()]);
//                    }
//
//                    // Check for existing action to detect conflicts
//                    String existingAction = parsingTable.actionTable.get(itemNo)[index];
//
//                    if (existingAction == null) {
//                        parsingTable.actionTable.get(itemNo)[index] = "shift " + (i + index);
//                    } else {
//                        // If there's already an action, it's a conflict
//                        if (existingAction.startsWith("reduce")) {
//                            parsingTable.actionTable.get(itemNo)[index] = "shift-reduce conflict";
//                        } else {
//                            parsingTable.actionTable.get(itemNo)[index] = "shift-shift conflict";
//                        }
//                    }
//                }
//
//                // Create a new LR0Item and check for duplicates
//                LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);
//
//                // Check if the new item already exists in the list
//                boolean exists = false;
//                for (LR0Item item : newItems) {
//                    if (item.equals(newItem)) { // Assuming equals is properly overridden in LR0Item
//                        exists = true;
//                        break;
//                    }
//                }
//
//                if (!exists) {
//                    newItems.add(newItem);
//                }
//
//            } else {
//                // Handle reduce state: The dot is at the end of the production rule
//                String lhs = prodRule.getLHS(); // Left-hand side of the production rule
//                int index = parsingTable.getNonTerminal().indexOf(lhs);
//
//                // Ensure action table is initialized
//                if (parsingTable.actionTable.get(itemNo) == null) {
//                    parsingTable.actionTable.set(itemNo, new String[parsingTable.getTerminal().size()]);
//                }
//
//                // Set reduce action in the action table for each terminal in FOLLOW(lhs)
//                String followSetString = firstFollow.getFollow().get(firstFollow.getNonTerminals().indexOf(lhs)); // Get FOLLOW set as a merged string
//
//                if (followSetString != null && !followSetString.isEmpty()) { // Check if followSetString is not null or empty
//                    for (char terminal : followSetString.toCharArray()) {
//                        int terminalIndex = parsingTable.getTerminal().indexOf(String.valueOf(terminal));
//                        if (terminalIndex != -1) { // Check if terminal exists in the table
//                            if (parsingTable.actionTable.get(itemNo)[terminalIndex] == null) {
//                                parsingTable.actionTable.get(itemNo)[terminalIndex] = "reduce " + lhs;
//                            } else {
//                                // If there's already an action, it's a conflict
//                                if (!parsingTable.actionTable.get(itemNo)[terminalIndex].startsWith("reduce")) {
//                                    parsingTable.actionTable.get(itemNo)[terminalIndex] = "shift-reduce conflict";
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Create a new LR0Item for this reduce state and add it to the list
//                LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);
//
//                boolean exists = false;
//                for (LR0Item item : newItems) {
//                    if (item.equals(newItem)) {
//                        exists = true;
//                        break;
//                    }
//                }
//
//                if (!exists) {
//                    newItems.add(newItem);
//                }
//            }
//        }
//    }
//}




//public class TableGenerator {
//
//    private ParsingTable parsingTable = null;
//
//    public ParsingTable tableGenerator(Grammar grammar){
//
//        parsingTable = new ParsingTable(grammar);
//
//        //Augmenting grammar
//        String startSymbol = grammar.getStartSymbol().toString();
//        ProductionRule axuilaryRule = new ProductionRule();
//        axuilaryRule.setLHS(startSymbol+"'");
//        axuilaryRule.setRHS(startSymbol);
//
//        ArrayList<ProductionRule> augmentedRules = grammar.getProductionRules();
//        augmentedRules.addFirst(axuilaryRule);
//        ArrayList<Integer> dotPosition = new ArrayList<Integer>();
//
//        for(int i = 0; i<augmentedRules.size();i++){
//            dotPosition.add(0);
//        }
//
//        ArrayList<LR0Item> I = new ArrayList<>();
//
//        I.add(new LR0Item(augmentedRules, dotPosition, grammar));
//
//        int i = 0;
//        generateItem(I.get(i), grammar, i++);
//
//
//        return  parsingTable;
//    }
//
//
//    private void generateItem(LR0Item prevItem, Grammar grammar, int itemNo) {
//        ArrayList<LR0Item> newItems = new ArrayList<>();
//        ArrayList<ProductionRule> prevRules = prevItem.getProductionRules();
//        ArrayList<Integer> prevPosition = prevItem.getDotPosition();
//
//        // Iterate through previous production rules
//        for (int i = 0; i < prevRules.size(); i++) {
//            ArrayList<ProductionRule> prodRules = new ArrayList<>();
//            ArrayList<Integer> dotPosition = new ArrayList<>();
//
//            ProductionRule prodRule = prevRules.get(i);
//            Integer dotPos = prevPosition.get(i);
//
//            if (dotPos < prodRule.getRHS().length()) {
//                char token = prodRule.getRHS().charAt(dotPos);
//                dotPos++;
//                prodRules.add(prodRule);
//                dotPosition.add(dotPos);
//
//                // Remove current rule and position
//                prevRules.remove(i);
//                prevPosition.remove(i);
//                i--;
//
//                // Check remaining rules for the same token
//                for (int j = 0; j < prevRules.size(); j++) {
//                    if (prevRules.get(j).getRHS().charAt(prevPosition.get(j)) == token) {
//                        prodRules.add(prevRules.get(j));
//                        dotPosition.add(prevPosition.get(j) + 1);
//                    }
//                }
//
//                // Update parsing table for shift/reduce conflict detection
//                if (Character.isUpperCase(token)) {
//                    int index = parsingTable.getNonTerminal().indexOf(String.valueOf(token));
//                    parsingTable.gotoTable.get(itemNo)[index] = String.valueOf(i + index);
//                } else {
//                    int index = parsingTable.getTerminal().indexOf(String.valueOf(token));
//                    if (parsingTable.actionTable.get(itemNo) == null) {
//                        parsingTable.actionTable.set(itemNo, new String[parsingTable.getTerminal().size()]);
//                    }
//
//                    // Check for existing action to detect conflicts
//                    String existingAction = parsingTable.actionTable.get(itemNo)[index];
//
//                    if (existingAction == null) {
//                        parsingTable.actionTable.get(itemNo)[index] = "shift " + (i + index);
//                    } else {
//                        // If there's already an action, it's a conflict
//                        if (existingAction.startsWith("reduce")) {
//                            parsingTable.actionTable.get(itemNo)[index] = "shift-reduce conflict";
//                        } else {
//                            parsingTable.actionTable.get(itemNo)[index] = "shift-shift conflict";
//                        }
//                    }
//                }
//
//                // Create a new LR0Item and check for duplicates
//                LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);
//
//                // Check if the new item already exists in the list
//                boolean exists = false;
//                for (LR0Item item : newItems) {
//                    if (item.equals(newItem)) { // Assuming equals is properly overridden in LR0Item
//                        exists = true;
//                        break;
//                    }
//                }
//
//                if (!exists) {
//                    newItems.add(newItem);
//                } else {
//                    // Update the existing item's entry in the parsing table if needed
//                    // You may want to define how you want to update the existing item here
//                    // For example, updating action or goto tables based on your logic.
//                }
//
//            } else {
//                // Handle reduce state: The dot is at the end of the production rule
//                String lhs = prodRule.getLHS(); // Left-hand side of the production rule
//                int index = parsingTable.getNonTerminal().indexOf(lhs);
//
//                // Ensure action table is initialized
//                if (parsingTable.actionTable.get(itemNo) == null) {
//                    parsingTable.actionTable.set(itemNo, new String[parsingTable.getTerminal().size()]);
//                }
//
//                // Set reduce action in the action table for each terminal
//                for (int k = 0; k < parsingTable.getTerminal().size(); k++) {
//                    if (parsingTable.actionTable.get(itemNo)[k] == null) {
//                        parsingTable.actionTable.get(itemNo)[k] = "reduce " + lhs;
//                    } else {
//                        // If there's already an action, it's a conflict
//                        if (!parsingTable.actionTable.get(itemNo)[k].startsWith("reduce")) {
//                            parsingTable.actionTable.get(itemNo)[k] = "shift-reduce conflict";
//                        }
//                    }
//                }
//
//                // Create a new LR0Item for this reduce state and add it to the list
//                LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);
//
//                boolean exists = false;
//                for (LR0Item item : newItems) {
//                    if (item.equals(newItem)) {
//                        exists = true;
//                        break;
//                    }
//                }
//
//                if (!exists) {
//                    newItems.add(newItem);
//                }
//            }
//        }
//    }

//    private ArrayList<LR0Item> generateItem(LR0Item prevItem, Grammar grammar, int itemNo){
//
//        ArrayList<LR0Item> newItems = new ArrayList<LR0Item>();
//        ArrayList<ProductionRule> prevRules = prevItem.getProductionRules();
//        ArrayList<Integer> prevPosition = prevItem.getDotPosition();
//
//        for(int i = 0; i < prevItem.getProductionRules().size(); i++) {
//            ArrayList<ProductionRule> prodRules = new ArrayList<ProductionRule>();
//            ArrayList<Integer> dotPosition = new ArrayList<Integer>();
//
//            ProductionRule prodRule = prevRules.get(i);
//            Integer dotPos = prevPosition.get(i);
//            char token = prodRule.getRHS().charAt(dotPos);
//            dotPos++;
//            prodRules.add(prodRule);
//            dotPosition.add(dotPos);
//
//            prevRules.remove(i);
//            prevPosition.remove(i);
//
//            for (int j = 0; j < prevRules.size(); j++){
//                if(prevRules.get(j).getRHS().charAt(prevPosition.get(j)) == token){
//                    prodRules.add(prevRules.get(j));
//                    dotPosition.add(prevPosition.get(j)+1);
//                    j--;
//                }
//            }
//
//            if(Character.isUpperCase(token)){
//                int index = parsingTable.getNonTerminal().indexOf(String.valueOf(token));
//                parsingTable.gotoTable.get(itemNo)[index] = String.valueOf(i + index);
//            }else {
//                int index = parsingTable.getTerminal().indexOf(String.valueOf(token));
//                if(parsingTable.actionTable.get(itemNo)[index] != null){
//
//                }
//                parsingTable.actionTable.get(itemNo)[index] = "shift" + (i + index);
//            }
//
//            LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);
//            newItems.add(newItem);
//
//            if (prodRules.isEmpty()){
//                break;
//            }
//        }
//
//        return newItems;
//    }

//}
