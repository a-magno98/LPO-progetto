package lab09_04_08.parser;

import static lab09_04_08.parser.TokenType.*;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class StreamTokenizer implements Tokenizer {
	private static final String regEx;
	private static final Map<String, TokenType> keywords = new HashMap<>();
	private static final Map<String, TokenType> symbols = new HashMap<>();

	private boolean hasNext = true; // any stream contains at least the EOF
									// token
	private TokenType tokenType;
	private String tokenString;
	private int intValue;
	private boolean boolValue;
	private String stringValue;
	private final Scanner scanner;

	static {
		// remark: groups must correspond to the ordinal of the corresponding
		// token type
		final String skipRegEx = "(\\s+|//.*)"; // group 1
		final String identRegEx = "([a-zA-Z][a-zA-Z0-9]*)"; // group 2
		final String numRegEx = "(0[xX][0-9a-fA-F]+|[1-9][0-9]*|0)"; // group 3
		final String stringRegEx = "(\\\"(\\\\.|[^\"\\\\])*\\\")"; // group 4
		final String symbolRegEx = "\\+|\\*|==|=|\\(|\\)|\\[|\\]|;|,|\\{|\\}|-|!|&&|#|/\\\\|\\\\/|\\^";
		regEx = skipRegEx + "|" + identRegEx + "|" + numRegEx + "|" + stringRegEx + "|" + symbolRegEx;
	}

	static {
		keywords.put("print", PRINT);
		keywords.put("let", LET);
		keywords.put("false", BOOL);
		keywords.put("true", BOOL);
		keywords.put("if", IF);
		keywords.put("else", ELSE);
		keywords.put("fst", FST);
		keywords.put("snd", SND);
		keywords.put("in", IN);
		keywords.put("while", WHILE);
	}

	static {
		symbols.put("+", PLUS);
		symbols.put("*", TIMES);
		symbols.put("=", ASSIGN);
		symbols.put("(", OPEN_PAR);
		symbols.put(")", CLOSE_PAR);
		symbols.put("[", OPEN_PAIR);
		symbols.put("]", CLOSE_PAIR);
		symbols.put(";", STMT_SEP);
		symbols.put(",", EXP_SEP);
		symbols.put("{", OPEN_BLOCK);
		symbols.put("}", CLOSE_BLOCK_OR_SET);
		symbols.put("-", MINUS);
		symbols.put("!", NOT);
		symbols.put("&&", AND);
		symbols.put("==", EQ);
		symbols.put("\\/", UNION);
		symbols.put("/\\", INTERSECT);
		symbols.put("^", CONCAT);
		symbols.put("#", LENGTH);
	}

	public StreamTokenizer(Reader reader) {
		scanner = new StreamScanner(regEx, reader);
	}

	private void checkType() {
		tokenString = scanner.group();
		if (scanner.group(IDENT.ordinal()) != null) { // IDENT or BOOL or a keyword
			tokenType = keywords.get(tokenString);
			if (tokenType == null)
				tokenType = IDENT;
			if (tokenType == BOOL)
				boolValue = Boolean.parseBoolean(tokenString);
			return;
		}
		if (scanner.group(NUM.ordinal()) != null) { // NUM
			tokenType = NUM;
			intValue = Integer.decode(tokenString);
			return;
		}

		if (scanner.group(STRING.ordinal()) != null) { // STRING
			tokenType = STRING;
			stringValue = tokenString.substring(1, tokenString.length() - 1).replace("\\\"", "\"").replace("\\\\","\\");
			return;
		}
		if (scanner.group(SKIP.ordinal()) != null) { // SKIP
			tokenType = SKIP;
			return;
		}
		tokenType = symbols.get(tokenString); // a symbol
		if (tokenType == null)
			throw new AssertionError("Fatal error");
	}

	@Override
	public TokenType next() throws TokenizerException {
		do {
			tokenType = null;
			tokenString = "";
			try {
				if (hasNext && !scanner.hasNext()) {
					hasNext = false;
					return tokenType = EOF;
				}
				scanner.next();
			} catch (ScannerException e) {
				throw new TokenizerException(e);
			}
			checkType();
		} while (tokenType == SKIP);
		return tokenType;
	}

	private void checkValidToken() {
		if (tokenType == null)
			throw new IllegalStateException();
	}

	private void checkValidToken(TokenType ttype) {
		if (tokenType != ttype)
			throw new IllegalStateException();
	}

	@Override
	public String tokenString() {
		checkValidToken();
		return tokenString;
	}

	@Override
	public boolean boolValue() {
		checkValidToken(BOOL);
		return boolValue;
	}
	
	@Override
	public String stringValue() {
		checkValidToken(STRING);
		return stringValue;
	}

	@Override
	public int intValue() {
		checkValidToken(NUM);
		return intValue;
	}

	@Override
	public TokenType tokenType() {
		checkValidToken();
		return tokenType;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public void close() throws TokenizerException {
		try {
			scanner.close();
		} catch (ScannerException e) {
			throw new TokenizerException(e);
		}
	}
}
