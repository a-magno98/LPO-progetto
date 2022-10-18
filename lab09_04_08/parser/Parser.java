package lab09_04_08.parser;

import lab09_04_08.parser.ast.Prog;

public interface Parser {

	Prog parseProg() throws ParserException;

}