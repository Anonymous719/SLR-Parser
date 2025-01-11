package com.compiler.slr_parser.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class FirstFollow {

    private ArrayList<String> nonTerminals;
    private ArrayList<String> first;
    private ArrayList<String> follow;

    public void setNonTerminals(ArrayList<String> nonTerminals){
        this.nonTerminals = nonTerminals;
    }

    public void setFirst(ArrayList<String> first){
        this.first = first;
    }

    public void setFollow(ArrayList<String> follow){
        this.follow = follow;
    }

    public ArrayList<String> getNonTerminals() {
        return nonTerminals;
    }

    public ArrayList<String> getFirst() {
        return first;
    }

    public ArrayList<String> getFollow() {
        return follow;
    }
}
