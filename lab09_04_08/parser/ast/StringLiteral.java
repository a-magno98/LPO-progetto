package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class StringLiteral extends PrimLiteral<String>
{
	public StringLiteral(String n) {
		super(n);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitStringLiteral(value);
	}
}