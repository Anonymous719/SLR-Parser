package com.compiler.slr_parser.model;

import java.util.ArrayList;

public class ParseCheckRequest {
    private ArrayList<ProductionRule> productionRules;
    private Character startSymbol;
    private String inputString;

    // Getters and setters

    public ArrayList<ProductionRule> getProductionRules() {
        return productionRules;
    }

    public void setProductionRules(ArrayList<ProductionRule> productionRules) {
        this.productionRules = productionRules;
    }

    public Character getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(Character startSymbol) {
        this.startSymbol = startSymbol;
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }
}

