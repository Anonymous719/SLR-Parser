package com.compiler.slr_parser.util;

import com.compiler.slr_parser.model.*;
import java.util.Stack;

public class ParseString {
    private ParsingTable parsingTable;
    private Grammar grammar;

    public ParseString(ParsingTable parsingTable, Grammar grammar) {
        this.parsingTable = parsingTable;
        this.grammar = grammar;
    }

    public boolean parse(String input) {
        Stack<Integer> stateStack = new Stack<>();
        Stack<String> symbolStack = new Stack<>();
        stateStack.push(0);
        symbolStack.push("$");

        String[] inputTokens = input.split("");
        int inputIndex = 0;

        while (true) {
            if (stateStack.isEmpty()) {
                return false; // Error: Empty state stack
            }

            int currentState = stateStack.peek();
            String currentSymbol = inputIndex < inputTokens.length ? inputTokens[inputIndex] : "$";

            int terminalIndex = parsingTable.getTerminal().indexOf(currentSymbol);
            if (terminalIndex == -1) {
                return false; // Invalid input symbol
            }

            if (currentState >= parsingTable.actionTable.size()) {
                return false; // Invalid state
            }

            String[] actionRow = parsingTable.actionTable.get(currentState);
            if (actionRow == null || terminalIndex >= actionRow.length) {
                return false; // Invalid state or terminal index
            }

            String action = actionRow[terminalIndex];

            if (action == null || action.isEmpty()) {
                return false; // No action defined
            }

            if (action.equals("accept")) {
                return true; // Input string accepted
            } else if (action.startsWith("shift")) {
                int nextState = Integer.parseInt(action.split(" ")[1]);
                stateStack.push(nextState);
                symbolStack.push(currentSymbol);
                inputIndex++;
            } else if (action.startsWith("reduce")) {
                int ruleIndex = Integer.parseInt(action.split(" ")[1]);
                ProductionRule rule = grammar.getProductionRules().get(ruleIndex - 1);

                // Pop states and symbols
                for (int i = 0; i < rule.getRHS().length(); i++) {
                    if (!stateStack.isEmpty()) {
                        stateStack.pop();
                    }
                    if (!symbolStack.isEmpty()) {
                        symbolStack.pop();
                    }
                }

                if (stateStack.isEmpty()) {
                    return false; // Error: Empty state stack after reduction
                }

                int gotoState = Integer.parseInt(parsingTable.gotoTable.get(stateStack.peek())[parsingTable.getNonTerminal().indexOf(rule.getLHS())]);
                stateStack.push(gotoState);
                symbolStack.push(rule.getLHS());
            } else {
                return false; // Invalid action
            }
        }
    }
}


