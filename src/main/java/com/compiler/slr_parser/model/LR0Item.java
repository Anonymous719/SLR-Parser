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
}

