package lab09_04_08.parser.ast;

import static java.util.Objects.requireNonNull;

import lab10_04_29.visitors.Visitor;

public class WhileStmt implements Stmt {
	private final Exp exp;
	private final Block instructions;

	public WhileStmt(Exp exp, Block instructions) {
		this.exp = requireNonNull(exp);
		this.instructions = requireNonNull(instructions);

	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + exp + "," + instructions + ")";
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitWhileStmt(exp,instructions);
	}

}
