package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class IntLiteral extends PrimLiteral<Integer> {

	public IntLiteral(int n) {
		super(n);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitIntLiteral(value);
	}
}
