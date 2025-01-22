package com.compiler.slr_parser.model;

import lombok.Data;
import java.util.ArrayList;


@Data
public class LR0Item {
    public ArrayList<ProductionRule> getProductionRules() {
        return productionRules;
    }

    public void setProductionRules(ArrayList<ProductionRule> productionRules) {
        this.productionRules = productionRules;
    }

    public ArrayList<Integer> getDotPosition() {
        return dotPosition;
    }

    public void setDotPosition(ArrayList<Integer> dotPosition) {
        this.dotPosition = dotPosition;
    }

    public ArrayList<ProductionRule> productionRules;
    public ArrayList<Integer> dotPosition;

    public LR0Item(ArrayList<ProductionRule> productionRules, ArrayList<Integer> dotPosition, Grammar grammar) {
        this.productionRules = new ArrayList<>(productionRules);
        this.dotPosition = new ArrayList<>(dotPosition);

        // Perform closure for each production rule
        for (int i = 0; i < productionRules.size(); i++) {
            String word = productionRules.get(i).getRHS();
            int dotPos = dotPosition.get(i);

            // Check if the dot position is within bounds
            if (dotPos < word.length() && Character.isUpperCase(word.charAt(dotPos))) {
                char nonTerminal = word.charAt(dotPos);
                ArrayList<ProductionRule> closuredRules = closouer(nonTerminal, grammar);

                // Add the closure rules to the current item
                for (ProductionRule rule : closuredRules) {
                    int index = productionRules.indexOf(rule); // Check if the rule already exists
                    if (index == -1 || dotPosition.get(index) != 0) { // Add if not present or dotPosition is not 0
                        productionRules.add(rule);
                        dotPosition.add(0); // Dot position for new rules starts at 0
                    }
                }
            }
        }
    }

    private ArrayList<ProductionRule> closouer(char nonTerminal, Grammar grammar) {
        ArrayList<ProductionRule> addRules = new ArrayList<>();

        for (ProductionRule rule : grammar.getProductionRules()) {
            if (rule.getLHS().toCharArray()[0] == nonTerminal) {
                addRules.add(rule);
            }
        }

        return addRules;
    }

    public void updateRules(ArrayList<ProductionRule> newRules, ArrayList<Integer> newPositions) {
        // Validate input parameters
        if (newRules == null || newPositions == null) {
            throw new IllegalArgumentException("New rules and positions cannot be null");
        }

        if (newRules.size() != newPositions.size()) {
            throw new IllegalArgumentException("Number of rules must match number of positions");
        }

        // Validate dot positions
        for (int i = 0; i < newRules.size(); i++) {
            ProductionRule rule = newRules.get(i);
            int position = newPositions.get(i);

            if (position < 0 || position > rule.getRHS().length()) {
                throw new IllegalArgumentException(
                        "Invalid dot position " + position +
                                " for rule " + rule.getLHS() + " → " + rule.getRHS()
                );
            }
        }

        // Create deep copies to prevent external modification
        this.productionRules = new ArrayList<>();
        this.dotPosition = new ArrayList<>();

        for (int i = 0; i < newRules.size(); i++) {
            // Deep copy of production rule
            ProductionRule originalRule = newRules.get(i);
            ProductionRule copiedRule = new ProductionRule();
            copiedRule.setLHS(originalRule.getLHS());
            copiedRule.setRHS(originalRule.getRHS());

            this.productionRules.add(copiedRule);
            this.dotPosition.add(newPositions.get(i));
        }

        // Sort rules for consistent comparison
        sortRules();
    }

    private void sortRules() {
        // Create list of indices
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < productionRules.size(); i++) {
            indices.add(i);
        }

        // Sort indices based on rules
        indices.sort((i1, i2) -> {
            ProductionRule r1 = productionRules.get(i1);
            ProductionRule r2 = productionRules.get(i2);

            // First compare LHS
            int lhsCompare = r1.getLHS().compareTo(r2.getLHS());
            if (lhsCompare != 0) {
                return lhsCompare;
            }

            // Then compare RHS
            int rhsCompare = r1.getRHS().compareTo(r2.getRHS());
            if (rhsCompare != 0) {
                return rhsCompare;
            }

            // If rules are the same, compare dot positions
            return dotPosition.get(i1).compareTo(dotPosition.get(i2));
        });

        // Create new sorted lists
        ArrayList<ProductionRule> sortedRules = new ArrayList<>();
        ArrayList<Integer> sortedPositions = new ArrayList<>();

        for (int index : indices) {
            sortedRules.add(productionRules.get(index));
            sortedPositions.add(dotPosition.get(index));
        }

        // Update the class fields
        productionRules = sortedRules;
        dotPosition = sortedPositions;
    }

}

