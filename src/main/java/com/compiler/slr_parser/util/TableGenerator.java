package com.compiler.slr_parser.util;

import com.compiler.slr_parser.model.*;
import java.util.ArrayList;

public class TableGenerator {

    private ParsingTable parsingTable = null;

    public ParsingTable tableGenerator(Grammar grammar) {
        parsingTable = new ParsingTable(grammar);

        // Augmenting grammar
        String startSymbol = grammar.getStartSymbol().toString();
        ProductionRule auxiliaryRule = new ProductionRule();
        auxiliaryRule.setLHS(startSymbol + "'");
        auxiliaryRule.setRHS(startSymbol);

        ArrayList<ProductionRule> augmentedRules = grammar.getProductionRules();
        augmentedRules.add(0, auxiliaryRule); // Use add(0, auxiliaryRule) instead of addFirst
        ArrayList<Integer> dotPosition = new ArrayList<>();

        for (int i = 0; i < augmentedRules.size(); i++) {
            dotPosition.add(0);
        }

        ArrayList<LR0Item> I = new ArrayList<>();
        I.add(new LR0Item(augmentedRules, dotPosition, grammar));

        // Compute FIRST and FOLLOW sets
        FirstFollowGenerator firstFollowGenerator = new FirstFollowGenerator();
        FirstFollow firstFollow = firstFollowGenerator.computeFirstFollow(grammar);

        int i = 0;
        generateItem(I.get(i), grammar, i++, firstFollow);

        return parsingTable;
    }

    private void generateItem(LR0Item prevItem, Grammar grammar, int itemNo, FirstFollow firstFollow) {
        ArrayList<LR0Item> newItems = new ArrayList<>();
        ArrayList<ProductionRule> prevRules = prevItem.getProductionRules();
        ArrayList<Integer> prevPosition = prevItem.getDotPosition();

        // Iterate through previous production rules
        for (int i = 0; i < prevRules.size(); i++) {
            ArrayList<ProductionRule> prodRules = new ArrayList<>();
            ArrayList<Integer> dotPosition = new ArrayList<>();

            ProductionRule prodRule = prevRules.get(i);
            Integer dotPos = prevPosition.get(i);

            if (dotPos < prodRule.getRHS().length()) {
                char token = prodRule.getRHS().charAt(dotPos);
                dotPos++;
                prodRules.add(prodRule);
                dotPosition.add(dotPos);

                // Remove current rule and position
                prevRules.remove(i);
                prevPosition.remove(i);
                i--;

                // Check remaining rules for the same token
                for (int j = 0; j < prevRules.size(); j++) {
                    if (prevRules.get(j).getRHS().charAt(prevPosition.get(j)) == token) {
                        prodRules.add(prevRules.get(j));
                        dotPosition.add(prevPosition.get(j) + 1);
                    }
                }

                // Update parsing table for shift/reduce conflict detection
                if (Character.isUpperCase(token)) {
                    int index = parsingTable.getNonTerminal().indexOf(String.valueOf(token));
                    parsingTable.gotoTable.get(itemNo)[index] = String.valueOf(i + index);
                } else {
                    int index = parsingTable.getTerminal().indexOf(String.valueOf(token));
                    if (parsingTable.actionTable.get(itemNo) == null) {
                        parsingTable.actionTable.set(itemNo, new String[parsingTable.getTerminal().size()]);
                    }

                    // Check for existing action to detect conflicts
                    String existingAction = parsingTable.actionTable.get(itemNo)[index];

                    if (existingAction == null) {
                        parsingTable.actionTable.get(itemNo)[index] = "shift " + (i + index);
                    } else {
                        // If there's already an action, it's a conflict
                        if (existingAction.startsWith("reduce")) {
                            parsingTable.actionTable.get(itemNo)[index] = "shift-reduce conflict";
                        } else {
                            parsingTable.actionTable.get(itemNo)[index] = "shift-shift conflict";
                        }
                    }
                }

                // Create a new LR0Item and check for duplicates
                LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);

                // Check if the new item already exists in the list
                boolean exists = false;
                for (LR0Item item : newItems) {
                    if (item.equals(newItem)) { // Assuming equals is properly overridden in LR0Item
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    newItems.add(newItem);
                }

            } else {
                // Handle reduce state: The dot is at the end of the production rule
                String lhs = prodRule.getLHS(); // Left-hand side of the production rule
                int index = parsingTable.getNonTerminal().indexOf(lhs);

                // Ensure action table is initialized
                if (parsingTable.actionTable.get(itemNo) == null) {
                    parsingTable.actionTable.set(itemNo, new String[parsingTable.getTerminal().size()]);
                }

                // Set reduce action in the action table for each terminal in FOLLOW(lhs)
                String followSetString = firstFollow.getFollow().get(firstFollow.getNonTerminals().indexOf(lhs)); // Get FOLLOW set as a merged string

                if (followSetString != null && !followSetString.isEmpty()) { // Check if followSetString is not null or empty
                    for (char terminal : followSetString.toCharArray()) {
                        int terminalIndex = parsingTable.getTerminal().indexOf(String.valueOf(terminal));
                        if (terminalIndex != -1) { // Check if terminal exists in the table
                            if (parsingTable.actionTable.get(itemNo)[terminalIndex] == null) {
                                parsingTable.actionTable.get(itemNo)[terminalIndex] = "reduce " + lhs;
                            } else {
                                // If there's already an action, it's a conflict
                                if (!parsingTable.actionTable.get(itemNo)[terminalIndex].startsWith("reduce")) {
                                    parsingTable.actionTable.get(itemNo)[terminalIndex] = "shift-reduce conflict";
                                }
                            }
                        }
                    }
                }

                // Create a new LR0Item for this reduce state and add it to the list
                LR0Item newItem = new LR0Item(prodRules, dotPosition, grammar);

                boolean exists = false;
                for (LR0Item item : newItems) {
                    if (item.equals(newItem)) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    newItems.add(newItem);
                }
            }
        }
    }
}




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
