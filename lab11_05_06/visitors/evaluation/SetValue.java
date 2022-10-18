package lab11_05_06.visitors.evaluation;

import static java.util.Objects.requireNonNull;
import java.util.HashSet;
import java.util.Iterator;

public class SetValue implements Value, Iterable<Value> {

	private final HashSet<Value> set;

	public SetValue() {
		set = new HashSet<>();	
	}
	
	public SetValue(SetValue sett) {
		this();
		for(Value el : sett)
			set.add(el);
	}
	
	public SetValue(Value val, SetValue sett) {
		this(sett);
		set.add(requireNonNull(val));
	}
	
	public SetValue(Value val) {
		this();
		this.add(val);
	}

	@Override
	public Iterator<Value> iterator() {
		return set.iterator();
	}
	
	@Override
	public SetValue asSet() {
		return this;
	}
	
	@Override
	public String toString() {
		return "{" +set.toString().substring(1).substring(0, (set.toString().length()-2))+"}";
	}	
	
	@Override
	public int hashCode() {
		return set.hashCode();
	}
	
	public Value add(Value e){
		set.add(e);
		return this;
	}
	
	public HashSet<Value> getSet() {
		return this.set;
	}

	public Value union(SetValue s) {
		SetValue set1 = new SetValue(this);
		SetValue set2 = new SetValue(s);
		SetValue SV = new SetValue();
		SV.getSet().addAll(set1.getSet());
		SV.getSet().addAll(set2.getSet());
		return SV;
	}

	public Value intersect(SetValue s) {
		SetValue set1 = new SetValue(this);
		SetValue set2 = new SetValue(s);
		SetValue SV = new SetValue();
		SV.getSet().addAll(set1.getSet());
		SV.getSet().retainAll(set2.getSet());
		return SV;
	}

	public Boolean in(Value val) {
		return set.contains(val);
	}
	
	public int size() {
		return set.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SetValue))
			return false;
		return set.equals(((SetValue) obj).set);
	}
}
