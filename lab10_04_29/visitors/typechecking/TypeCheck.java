package lab10_04_29.visitors.typechecking;

import static java.lang.System.err;
import static lab10_04_29.visitors.typechecking.PrimtType.*;

import java.io.InputStreamReader;

import lab10_04_29.environments.EnvironmentException;
import lab10_04_29.environments.GenEnvironment;
import lab09_04_08.parser.MyParser;
import lab09_04_08.parser.Parser;
import lab09_04_08.parser.ParserException;
import lab09_04_08.parser.StreamTokenizer;
import lab09_04_08.parser.Tokenizer;
import lab09_04_08.parser.TokenizerException;
import lab09_04_08.parser.ast.*;
import lab10_04_29.visitors.Visitor;

public class TypeCheck implements Visitor<Type> {

	private final GenEnvironment<Type> env = new GenEnvironment<>();

	private void checkBinOp(Exp left, Exp right, Type type) {
		type.checkEqual(left.accept(this));
		type.checkEqual(right.accept(this));
	}

	// static semantics for programs; no value returned by the visitor

	@Override
	public Type visitProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
		} catch (EnvironmentException e) { // undefined variable
			throw new TypecheckerException(e);
		}
		return null;
	}

	// static semantics for statements; no value returned by the visitor

	@Override
	public Type visitAssignStmt(Ident ident, Exp exp) 
	{
		Type t = env.lookup(ident);
		t.checkEqual(exp.accept(this));
		return null;
	}

	@Override
	public Type visitPrintStmt(Exp exp) {
	 exp.accept(this);
	  return null;
	}


	@Override
	public Type visitDecStmt(Ident ident, Exp exp) 
	{
		env.dec(ident, exp.accept(this));
		return null;
	}

	@Override
	public Type visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		BOOL.checkEqual(exp.accept(this));
		thenBlock.accept(this);
		if (elseBlock == null)
			return null;
		elseBlock.accept(this);
		return null;

	}

	@Override
	public Type visitBlock(StmtSeq stmtSeq) {
		env.enterScope();
		stmtSeq.accept(this);
		env.exitScope();
		return null;

	}
	
	@Override
	public Type visitWhileStmt(Exp exp, Block block) {
		BOOL.checkEqual(exp.accept(this));
		block.accept(this);
		return null;
	}

	// static semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Type visitSingleStmt(Stmt stmt) {
		stmt.accept(this);		
		return null;
	}

	@Override
	public Type visitMoreStmt(Stmt first, StmtSeq rest) 
	{
	    first.accept(this);
	    rest.accept(this);
		return null;
	}

	// static semantics of expressions; a type is returned by the visitor

	@Override
	public Type visitAdd(Exp left, Exp right) {
	    checkBinOp(left, right, INT);
	    return INT;
	}

	@Override
	public Type visitIntLiteral(int value) {
		return INT;
	}

	@Override
	public Type visitMul(Exp left, Exp right) {
	    checkBinOp(left,right,INT);
	    return INT;
	}

	@Override
	public Type visitSign(Exp exp) {
		return INT.checkEqual(exp.accept(this));
	}

	@Override
	public Type visitIdent(Ident id) {
	   return env.lookup(id);
	}

	@Override
	public Type visitNot(Exp exp) {
	    return BOOL.checkEqual(exp.accept(this));
	}

	@Override
	public Type visitAnd(Exp left, Exp right) {
		 checkBinOp(left,right,BOOL);
		  return BOOL;
	}

	@Override
	public Type visitBoolLiteral(boolean value) {
	   return BOOL;
	}

	@Override
	public Type visitEq(Exp left, Exp right) {
		left.accept(this).checkEqual(right.accept(this));
		return BOOL;

	}

	@Override
	public Type visitPairLit(Exp left, Exp right) {
		 return new PairType(left.accept(this),right.accept(this));
	}

	@Override
	public Type visitFst(Exp exp) {
	    return exp.accept(this).getFstPairType();
	}

	@Override
	public Type visitSnd(Exp exp) {
	    return exp.accept(this).getSndPairType();
	}
	
	@Override
	public Type visitDim(Exp exp) {
		Type tp = exp.accept(this);
		if(!tp.equals(STRING))
			tp.checkIsSetorError();
		return INT;
	}

	@Override
	public Type visitConcat(Exp left, Exp right) {
		checkBinOp(left, right, STRING);
		return STRING;
	}

	@Override
	public Type visitIn(Exp left, Exp right) {
		Type l = left.accept(this);
		Type r = new SetType(l);
		r.checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public Type visitIntersect(Exp left, Exp right) {
		left.accept(this).checkIsSetType();
		right.accept(this).checkIsSetType();
		checkBinOp(left, right, left.accept(this));
		return left.accept(this);
	}

	@Override
	public Type visitSetLit(ExpSeq exps) {
		return new SetType(exps.accept(this));
	}

	@Override
	public Type visitMoreExp(Exp left, ExpSeq right) {
		Type found = left.accept(this);
		return found.checkEqual(right.accept(this));
	}

	@Override
	public Type visitSingleExp(Exp exp) {
		return exp.accept(this);
	}

	@Override
	public Type visitStringLiteral(String value) {
		return STRING;
	}

	@Override
	public Type visitUnion(Exp left, Exp right) {
		left.accept(this).checkIsSetType();
		right.accept(this).checkIsSetType();
		checkBinOp(left, right, left.accept(this));
		return left.accept(this);
	}

	public static void main(String[] args) {
		try (Tokenizer tokenizer = new StreamTokenizer(new InputStreamReader(System.in))) {
			Parser parser = new MyParser(tokenizer);
			Prog prog = parser.parseProg();
			prog.accept(new TypeCheck());
		} catch (TokenizerException e) {
			err.println("Tokenizer error: " + e.getMessage());
		} catch (ParserException e) {
			err.println("Syntax error: " + e.getMessage());
		} catch (TypecheckerException e) {
			err.println("Static error: " + e.getMessage());
		} catch (Throwable e) {
			err.println("Unexpected error.");
			e.printStackTrace();
		}
	}

}
