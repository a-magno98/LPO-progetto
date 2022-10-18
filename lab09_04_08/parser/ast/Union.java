package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class Union extends BinaryOp {
	public Union(Exp left, Exp right) {
		super(left, right);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitUnion(left,right);
	}
}