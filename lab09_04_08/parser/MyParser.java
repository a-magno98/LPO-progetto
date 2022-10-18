package lab09_04_08.parser;

import static java.util.Objects.requireNonNull;
import static java.lang.System.err;
import static lab09_04_08.parser.TokenType.*;

import java.io.InputStreamReader;

import lab09_04_08.parser.ast.*;

/*
Prog ::= StmtSeq 'EOF'
 StmtSeq ::= Stmt (';' StmtSeq)?
 Stmt ::= 'let'? ID '=' Exp | 'print' Exp |  'if' '(' Exp ')' '{' StmtSeq '}' ('else' '{' StmtSeq '}')? | while (Exp) {StmtSeq}
 ExpSeq ::= Exp (, ExpSeq)?
 Exp ::= Eq ('&&' Eq)*
 Eq ::= In ('==' In)*
 In ::= Union ('in' Union)*
 Union ::= Intesect ('\\\/' Intersect)*
 Intersect ::= Concat ('\/\\' Concat)*
 Concat ::= Add ('^' Add)*
 Add ::= Mul ('+' Mul)*
 Mul::= Atom ('*' Atom)*
 Atom ::= '[' Exp ',' Exp ']' | '{' ExpSeq '}' | '#' Atom | 'fst' Atom | 'snd' Atom | '-' Atom | '!' Atom | BOOL | NUM | ID | STRING | '(' Exp ')'
*/

public class MyParser implements Parser {

	private final Tokenizer tokenizer;

	private void tryNext() throws ParserException {
		try {
			tokenizer.next();
		} catch (TokenizerException e) {
			throw new ParserException(e);
		}
	}

	private void match(TokenType expected) throws ParserException {
		final TokenType found = tokenizer.tokenType();
		if (found != expected)
			throw new ParserException(
					"Expecting " + expected + ", found " + found + "('" + tokenizer.tokenString() + "')");
	}

	private void consume(TokenType expected) throws ParserException {
		match(expected);
		tryNext();
	}

	private void unexpectedTokenError() throws ParserException {
		throw new ParserException("Unexpected token " + tokenizer.tokenType() + "('" + tokenizer.tokenString() + "')");
	}

	public MyParser(Tokenizer tokenizer) {
		this.tokenizer = requireNonNull(tokenizer);
	}

	@Override
	public Prog parseProg() throws ParserException {
		tryNext(); // one look-ahead symbol
		Prog prog = new ProgClass(parseStmtSeq());
		match(EOF);
		return prog;
	}

	private StmtSeq parseStmtSeq() throws ParserException {
	    Stmt stmt = parseStmt();
	    if(tokenizer.tokenType() == STMT_SEP) {
	    		tryNext();
	    		return new MoreStmt(stmt, parseStmtSeq());
	    }
	    return new SingleStmt(stmt);
	}

	private Stmt parseStmt() throws ParserException {
		switch (tokenizer.tokenType()) {
		default:
			unexpectedTokenError();
		case PRINT:
			return parsePrintStmt();
		case LET:
			return parseVarStmt();
		case IDENT:
			return parseAssignStmt();
		case IF:
			return parseIfStmt();
		case WHILE:
			return parseWhileStmt();
		}
	}
	
	

	private PrintStmt parsePrintStmt() throws ParserException {
	    consume(PRINT);
	    return new PrintStmt(parseExp());
	}

	private DecStmt parseVarStmt() throws ParserException {
	    consume(LET);
	    Ident ident = parseIdent();
	    consume(ASSIGN);
		return new DecStmt(ident, parseExp());
	}

	private AssignStmt parseAssignStmt() throws ParserException {
	    Ident ident = parseIdent();
	    consume(ASSIGN);
	    return new AssignStmt(ident, parseExp());
	}

	private IfStmt parseIfStmt() throws ParserException {
	    consume(IF);
	    Exp exp = parseRoundPar();
	    Block block = parseBlock();
	    if(tokenizer.tokenType() == ELSE) {
	    		consume(ELSE);
	    		Block opt = parseBlock();
	    		return new IfStmt(exp, block, opt);
	    }
	    return new IfStmt(exp, block);
	}
	
	private Block parseBlock() throws ParserException {
		consume(OPEN_BLOCK);
		StmtSeq stmt = parseStmtSeq();
		consume(CLOSE_BLOCK_OR_SET);
		return new Block(stmt);
	}
	
	private WhileStmt parseWhileStmt() throws ParserException {
		consume(WHILE);
		Exp exp = parseRoundPar();
		Block block = parseBlock();
		return new WhileStmt(exp, block);
	}
	
	private ExpSeq parseExpSeq() throws ParserException {
		Exp exp = parseExp();
		if (tokenizer.tokenType() == EXP_SEP) {
			tryNext();
			return new MoreExp(exp, parseExpSeq());
		}
		return new SingleExp(exp);
	}

	private Exp parseExp() throws ParserException {
		Exp exp = parseEq();
		while(tokenizer.tokenType() == AND)
		{
			tryNext();
			exp = new And(exp, parseEq());
		}
		return exp;
	}
	
	private Exp parseEq() throws ParserException {
		Exp exp = parseIn();
		while(tokenizer.tokenType() == EQ)
		{
			tryNext();
			exp = new Eq(exp,parseIn());
		}
		return exp;
	}
	
	private Exp parseIn() throws ParserException {
		Exp exp = parseUnion();
		while(tokenizer.tokenType() == IN)
		{
			tryNext();
			exp = new In(exp, parseUnion());
		}
		return exp;
	}
	
	private Exp parseUnion() throws ParserException {
		Exp exp = parseIntersect();
		while(tokenizer.tokenType() == UNION)
		{
			tryNext();
			exp = new Union(exp, parseIntersect());
		}
		return exp;
		
	}
	
	private Exp parseIntersect() throws ParserException {
		Exp exp = parseConcat();
		while(tokenizer.tokenType() == INTERSECT)
		{
			tryNext();
			exp = new Intersect(exp, parseConcat());
		}
		return exp;
		
	}
	
	private Exp parseConcat() throws ParserException {
		Exp exp = parseAdd();
		while(tokenizer.tokenType() == CONCAT)
		{
			tryNext();
			exp = new Concat(exp, parseAdd());
		}
		return exp;
		
	}

	private Exp parseAdd() throws ParserException {
		Exp exp = parseMul();
		while(tokenizer.tokenType() == PLUS)
		{
			tryNext();
			exp = new Add(exp, parseMul());
		}
		return exp;
	}

	private Exp parseMul() throws ParserException {
		Exp exp = parseAtom();
		while(tokenizer.tokenType() == TIMES)
		{
			tryNext();
			exp = new Mul(exp, parseAtom());
		}
		return exp;
	}

	private Exp parseAtom() throws ParserException {
		switch (tokenizer.tokenType()) {
		default:
			unexpectedTokenError();
		case NUM:
			return parseNum();
		case IDENT:
			return parseIdent();
		case STRING:
			return parseString();
		case MINUS:
			return parseMinus();
		case OPEN_PAR:
			return parseRoundPar();
		case BOOL:
			return parseBoolean();
		case NOT:
			return parseNot();
		case OPEN_PAIR:
			return parsePairLit();
		case FST:
			return parseFst();
		case SND:
			return parseSnd();
		case OPEN_BLOCK:
			return parseSetLit();
		case LENGTH:
			return parseDim();
		
		}
	}

	private IntLiteral parseNum() throws ParserException {
		int val = tokenizer.intValue();
		consume(NUM);
		return new IntLiteral(val);
	}

	private BoolLiteral parseBoolean() throws ParserException {
		boolean val = tokenizer.boolValue();
		consume(BOOL);
		return new BoolLiteral(val);
	}
	
	private StringLiteral parseString() throws ParserException
	{
		String name = tokenizer.stringValue();
		consume(STRING);
		return new StringLiteral(name);
	}

	private Ident parseIdent() throws ParserException {
		String name = tokenizer.tokenString();
		consume(IDENT);
		return new SimpleIdent(name);
	}

	private Sign parseMinus() throws ParserException {
		consume(MINUS);
		return new Sign(parseAtom());
	}

	private Fst parseFst() throws ParserException {
		consume(FST);
		return new Fst(parseAtom());
	}

	private Snd parseSnd() throws ParserException {
		consume(SND);
		return new Snd(parseAtom());
	}

	private Not parseNot() throws ParserException {
		consume(NOT);
		return new Not(parseAtom());
	}
	
	private Dim parseDim() throws ParserException {
		consume(LENGTH);
		return new Dim(parseAtom());
	}
	
	private PairLit parsePairLit() throws ParserException {
		consume(OPEN_PAIR);
		Exp exp_left = parseExp();
		consume(EXP_SEP);
		Exp exp_right = parseExp();
		consume(CLOSE_PAIR);
		return new PairLit(exp_left,exp_right);
	}
	
	private SetLit parseSetLit() throws ParserException {
		consume(OPEN_BLOCK);
		ExpSeq exps = parseExpSeq();
		consume(CLOSE_BLOCK_OR_SET);
		return new SetLit(exps);
	}

	private Exp parseRoundPar() throws ParserException {
		consume(OPEN_PAR);
		Exp exp = parseExp();
		consume(CLOSE_PAR);
		return exp;
	}

	public static void main(String[] args) {
		try (Tokenizer tokenizer = new StreamTokenizer(new InputStreamReader(System.in))) {
			Parser parser = new MyParser(tokenizer);
			Prog prog = parser.parseProg();
			System.out.println(prog);
		} catch (TokenizerException e) {
			err.println("Tokenizer error: " + e.getMessage());
		} catch (ParserException e) {
			err.println("Syntax error: " + e.getMessage());
		} catch (Throwable e) {
			err.println("Unexpected error.");
			e.printStackTrace();
		}
	}
}

