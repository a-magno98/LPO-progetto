package lab10_04_29.visitors.typechecking;

import static java.util.Objects.requireNonNull;

public class SetType implements Type {

		private final Type TypeElement;
		
		public static final String TYPE_NAME = "SET";

		
		public SetType(Type pt) {
			this.TypeElement=requireNonNull(pt);
		}
		
		@Override
		public final boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof SetType))
				return false;
			SetType st = (SetType) obj;
			return TypeElement.equals(st.TypeElement);
		}
		
		public Type getSetType() {
			return TypeElement;
		}

		@Override
		public int hashCode() {
			return 31 * TypeElement.hashCode();
		}

		@Override
		public String toString() {
			return TypeElement + " " + TYPE_NAME;
		}
}
