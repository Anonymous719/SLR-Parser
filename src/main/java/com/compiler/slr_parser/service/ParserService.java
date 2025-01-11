package com.compiler.slr_parser.service;

import com.compiler.slr_parser.model.Grammar;
import com.compiler.slr_parser.model.ParsingTable;
import com.compiler.slr_parser.util.TableGenerator;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    public ParsingTable generateParsingTable(Grammar grammar){
        TableGenerator tableGeneratorService = new TableGenerator();

        ParsingTable table = tableGeneratorService.tableGenerator(grammar);

        return table;
    }
}
