package com.compiler.slr_parser.model;


import lombok.Data;

import java.util.ArrayList;

@Data
public class ParsingTable {

    public ArrayList<String[]> actionTable;
    public ArrayList<String[]> gotoTable;
    private ArrayList<String> terminal;
    private ArrayList<String> nonTerminal;

    public ParsingTable(Grammar grammar){
        this.terminal = grammar.getTerminal();
        this.nonTerminal = grammar.getNonTerminal();
        // Initialize actionTable and gotoTable with null values
        actionTable = new ArrayList<String[]>(terminal.size());
        gotoTable = new ArrayList<String[]>(nonTerminal.size());

// Fill the lists with null arrays
        for (int i = 0; i < terminal.size(); i++) {
            actionTable.add(new String[terminal.size()]);
        }
        for (int i = 0; i < nonTerminal.size(); i++) {
            gotoTable.add(new String[nonTerminal.size()]);
        }

    }

    public ArrayList<String[]> getActionTable() {
        return actionTable;
    }

    public void setActionTable(ArrayList<String[]> actionTable) {
        this.actionTable = actionTable;
    }

    public ArrayList<String[]> getGotoTable() {
        return gotoTable;
    }

    public void setGotoTable(ArrayList<String[]> gotoTable) {
        this.gotoTable = gotoTable;
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
}
