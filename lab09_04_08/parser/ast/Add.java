package lab09_04_08.parser.ast;
import lab10_04_29.visitors.Visitor;


public class Add extends BinaryOp {
	public Add(Exp left, Exp right) {
		super(left, right);
	}
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitAdd(left, right);
	}

	
}
