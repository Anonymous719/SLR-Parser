package com.compiler.slr_parser.controller;


import com.compiler.slr_parser.model.FirstFollow;
import com.compiler.slr_parser.model.ParseCheckRequest;
import com.compiler.slr_parser.model.Grammar;
import com.compiler.slr_parser.model.ParsingTable;
import com.compiler.slr_parser.model.ProductionRule;
import com.compiler.slr_parser.service.ParserService;
import com.compiler.slr_parser.util.FirstFollowGenerator;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ParserController {

    private ParserService parserService;
    private FirstFollowGenerator firstFollowGenerator;

    public ParserController(ParserService parserService) {
        this.parserService = parserService;
        this.firstFollowGenerator = new FirstFollowGenerator();
    }

    @GetMapping("/parse-table/{startSymbol}")
    public ParsingTable parse(@PathVariable Character startSymbol, @RequestBody ArrayList<ProductionRule> productionRules) {
        Grammar grammar = new Grammar(productionRules, startSymbol);
        return parserService.generateParsingTable(grammar);
    }

    @GetMapping("/first-follow/{startSymbol}")
    public FirstFollow firstFollow(@PathVariable Character startSymbol, @RequestBody ArrayList<ProductionRule> productionRules) {
        Grammar grammar = new Grammar(productionRules, startSymbol);
        return firstFollowGenerator.computeFirstFollow(grammar);
    }

    @GetMapping("/check-parsable")
    public boolean checkParsable(@RequestBody ParseCheckRequest request) {
        return parserService.checkParsable(request);
    }

}

