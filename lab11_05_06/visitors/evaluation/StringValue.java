package lab11_05_06.visitors.evaluation;

public class StringValue extends PrimValue<String> {

	public StringValue(String value) {
		super(value);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof StringValue))
			return false;
		return value.equals(((StringValue) obj).value);
	}

	@Override
	public String asString() {
		return value;
	}
	
	@Override
	public String checkisStringOrError() {
		return asString();
	}

}
