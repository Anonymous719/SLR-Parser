package com.compiler.slr_parser.model;

import lombok.Data;

@Data
public class ProductionRule {

    private String LHS;
    private String RHS;

    public String getLHS(){
        return LHS;
    }

    public String getRHS(){
        return RHS;
    }

    public void setLHS(String LHS) {
        this.LHS = LHS;
    }

    public void setRHS(String RHS) {
        this.RHS = RHS;
    }
}
