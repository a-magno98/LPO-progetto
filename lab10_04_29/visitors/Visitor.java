package lab10_04_29.visitors;

import lab09_04_08.parser.ast.Block;
import lab09_04_08.parser.ast.Exp;
import lab09_04_08.parser.ast.ExpSeq;
import lab09_04_08.parser.ast.Ident;
import lab09_04_08.parser.ast.Stmt;
import lab09_04_08.parser.ast.StmtSeq;

public interface Visitor<T> {
	T visitAdd(Exp left, Exp right);

	T visitAssignStmt(Ident ident, Exp exp);

	T visitIntLiteral(int value);
	
	T visitEq(Exp left, Exp right);

	T visitMoreStmt(Stmt first, StmtSeq rest);

	T visitMul(Exp left, Exp right);

	T visitPrintStmt(Exp exp);

	T visitProg(StmtSeq stmtSeq);

	T visitSign(Exp exp);

	T visitIdent(Ident id); // the only corner case ...

	T visitSingleStmt(Stmt stmt);

	T visitDecStmt(Ident ident, Exp exp);

	T visitNot(Exp exp);

	T visitAnd(Exp left, Exp right);

	T visitBoolLiteral(boolean value);

	T visitIfStmt(Exp exp, Block thenBlock, Block elseBlock);

	T visitBlock(StmtSeq stmtSeq);

	T visitPairLit(Exp left, Exp right);

	T visitFst(Exp exp);

	T visitSnd(Exp exp);
	
	T visitDim(Exp exp);
	
	T visitConcat(Exp left, Exp right);
	
	T visitIn(Exp left, Exp right);
	
	T visitIntersect(Exp left, Exp right);
	
	T visitSetLit(ExpSeq exps);
		
	T visitMoreExp(Exp left, ExpSeq right);
	
	T visitSingleExp(Exp exp);
	
	T visitStringLiteral(String value);
	
	T visitUnion(Exp left, Exp right);
	
	T visitWhileStmt(Exp exp, Block block);
	
}
