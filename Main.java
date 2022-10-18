import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.io.IOException;

import lab09_04_08.parser.Parser;
import lab09_04_08.parser.ParserException;
import lab09_04_08.parser.MyParser;
import lab09_04_08.parser.StreamTokenizer;
import lab09_04_08.parser.Tokenizer;
import lab09_04_08.parser.ast.Prog;
import lab11_05_06.visitors.evaluation.Eval;
import lab11_05_06.visitors.evaluation.EvaluatorException;
import lab10_04_29.visitors.typechecking.TypeCheck;
import lab10_04_29.visitors.typechecking.TypecheckerException;

public class Main {
	
	public static final String INPUT_OPT = "-i";
	public static final String OUTPUT_OPT = "-o";
	public static final String NOT_TYPE_CHECKING = "-ntc";
	public static Boolean opt_ntc = true;
	

	public static final Map<String, String> options = new HashMap<>();
	static {
		options.put(INPUT_OPT, null);
		options.put(OUTPUT_OPT, null);
	}


	private static void optionError() {
		System.err.println("Option error.\nValid options:\n\t-i <input>\n\t-o <output>\n\t-ntc");
		System.exit(1);
	}


	private static String readArgOpt(Iterator<String> argIt) {
		if (!argIt.hasNext())
			optionError();
		return argIt.next();
	}


	private static void processArgs(String[] args) {
		Iterator<String> it = Arrays.asList(args).iterator();
		while (it.hasNext()) {
			String curr = it.next();
			if (!options.containsKey(curr) && !curr.equals(NOT_TYPE_CHECKING))
				optionError();
			if (curr.equals(NOT_TYPE_CHECKING))
				opt_ntc = false;
			else
				options.put(curr, readArgOpt(it));
		}
	}


	private static Reader tryOpenInput(String inputPath) throws FileNotFoundException {
		Reader rd = inputPath == null ? new InputStreamReader(System.in) : new FileReader(inputPath);
		return rd;
	}


	private static PrintWriter tryOpenOutput(String outputPath) throws FileNotFoundException {
		return outputPath == null ? new PrintWriter(System.out) : new PrintWriter(outputPath);
	}
	
	public static void main(String[] args) {
		processArgs(args);
		try(Reader rd = tryOpenInput(options.get(INPUT_OPT))) {
			Tokenizer tokenizer = new StreamTokenizer(rd);
			Parser parser = new MyParser(tokenizer);
			Prog prog = parser.parseProg();
			if(opt_ntc) {
				prog.accept(new TypeCheck());
			}
			PrintWriter pw = tryOpenOutput(options.get(OUTPUT_OPT));
			prog.accept(new Eval(pw));
			pw.close();
		} catch (IOException e) {
			err.println("I/O error: " + e.getMessage());
		} catch (ParserException e) {
			err.println("Syntax error: " + e.getMessage());
		} catch (TypecheckerException e) {
			err.println("Static error: " + e.getMessage());
		} catch (EvaluatorException e) {
			err.println("Dynamic error: " + e.getMessage());
		} catch (Throwable e) {
			err.println("Unexpected error.");
			e.printStackTrace();
		}
	}
}
