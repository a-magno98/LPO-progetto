package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class Dim extends UnaryOp{
	public Dim(Exp exp) {
		super(exp);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitDim(exp);
	}
}