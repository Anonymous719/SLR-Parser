package com.compiler.slr_parser.controller;


import com.compiler.slr_parser.model.FirstFollow;
import com.compiler.slr_parser.model.Grammar;
import com.compiler.slr_parser.model.ParsingTable;
import com.compiler.slr_parser.model.ProductionRule;
import com.compiler.slr_parser.service.ParserService;
import com.compiler.slr_parser.util.FirstFollowGenerator;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class ParserController {

    private ParserService parserService;

    private FirstFollowGenerator firstFollowGenerator;

    public ParserController(ParserService parserService){
        this.parserService = parserService;
    }

    @GetMapping("/parse-table/{startSymbol}")
    public ParsingTable parse(@PathVariable Character startSymbol, @RequestBody ArrayList<ProductionRule> productionRules) {
        // Create a Grammar object from the production rules and start symbol
        Grammar grammar = new Grammar(productionRules, startSymbol);
        ParsingTable table = parserService.generateParsingTable(grammar);
        return table;
    }

    @GetMapping("/first-follow/{startSymbol}")
    public FirstFollow firstFollow(@PathVariable Character startSymbol, @RequestBody ArrayList<ProductionRule> productionRules) {

        firstFollowGenerator = new FirstFollowGenerator();
        // Create a Grammar object from the production rules and start symbol
        Grammar grammar = new Grammar(productionRules, startSymbol);
        FirstFollow firstFollowTable = firstFollowGenerator.computeFirstFollow(grammar);
        return firstFollowTable;
    }

}

