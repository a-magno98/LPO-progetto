package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class AssignStmt extends AbstractAssignStmt {

	public AssignStmt(Ident ident, Exp exp) {
		super(ident, exp);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitAssignStmt(ident, exp);
	}

}
