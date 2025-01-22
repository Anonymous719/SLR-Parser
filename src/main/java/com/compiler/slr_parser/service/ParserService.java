package com.compiler.slr_parser.service;

import com.compiler.slr_parser.model.Grammar;
import com.compiler.slr_parser.model.ParseCheckRequest;
import com.compiler.slr_parser.model.ParsingTable;
import com.compiler.slr_parser.util.ParseString;
import com.compiler.slr_parser.util.TableGenerator;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    public ParsingTable generateParsingTable(Grammar grammar) {
        TableGenerator tableGeneratorService = new TableGenerator();
        ParsingTable table = tableGeneratorService.tableGenerator(grammar);
        return table;
    }

    public boolean checkParsable(ParseCheckRequest request) {
        Grammar grammar = new Grammar(request.getProductionRules(), request.getStartSymbol());
        ParsingTable parsingTable = generateParsingTable(grammar);

        ParseString parseString = new ParseString(parsingTable, grammar);
        return parseString.parse(request.getInputString());
    }
}
