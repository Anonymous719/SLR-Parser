package com.compiler.slr_parser.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Grammar {
    public Character getStartSymbol() {
        return startSymbol;
    }

    public ArrayList<String> getTerminal() {
        return terminal;
    }

    public void setTerminal(ArrayList<String> terminal) {
        this.terminal = terminal;
    }

    public ArrayList<String> getNonTerminal() {
        return nonTerminal;
    }

    public void setNonTerminal(ArrayList<String> nonTerminal) {
        this.nonTerminal = nonTerminal;
    }

    public void setStartSymbol(Character startSymbol) {
        this.startSymbol = startSymbol;
    }

    public ArrayList<ProductionRule> getProductionRules() {
        return productionRules;
    }

    public void setProductionRules(ArrayList<ProductionRule> productionRules) {
        this.productionRules = productionRules;
    }

    private Character startSymbol;
    private ArrayList<ProductionRule> productionRules;
    private ArrayList<String> terminal; // List of terminals
    private ArrayList<String> nonTerminal; // List of non-terminals

    public Grammar(ArrayList<ProductionRule> productionRules, Character startSymbol) {
        this.startSymbol = startSymbol;
        this.productionRules = productionRules;

        // Initialize terminal and nonTerminal lists
        this.terminal = new ArrayList<>();
        this.nonTerminal = new ArrayList<>();

        // Populate nonTerminals
        for (int i = 0; i < productionRules.size(); i++) {
            String lhs = productionRules.get(i).getLHS();
            if (!this.nonTerminal.contains(lhs)) {
                this.nonTerminal.add(lhs);
            }
        }

        // Populate terminals
        for (int i = 0; i < productionRules.size(); i++) {
            for (int j = 0; j < productionRules.get(i).getRHS().length(); j++) {
                char symbol = productionRules.get(i).getRHS().charAt(j);
                String symbolStr = String.valueOf(symbol);
                if (!this.nonTerminal.contains(symbolStr) && !this.terminal.contains(symbolStr)) {
                    this.terminal.add(symbolStr);
                }
            }
        }
    }

    public ArrayList<ProductionRule> addRule(ProductionRule newRule) {
        productionRules.add(newRule);
        return productionRules;
    }

    // Getters for terminals and non-terminals (optional)
    public ArrayList<String> getTerminals() {
        return terminal;
    }

    public ArrayList<String> getNonTerminals() {
        return nonTerminal;
    }

}

