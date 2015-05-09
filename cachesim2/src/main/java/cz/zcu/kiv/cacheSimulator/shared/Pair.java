package cz.zcu.kiv.cacheSimulator.shared;

/**
 * sablona pro praci s dvojici soudrznych hodnot
 * @author Pavel Bzoch
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Pair<A, B> {

	private A first;

	private B second;

	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	public A getFirst() {
		return first;
	}

	public void setFirst(A first) {
		this.first = first;
	}

	public B getSecond() {
		return second;
	}

	public void setSecond(B second) {
		this.second = second;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(!(obj instanceof Pair)){
			return false;
		}
		Pair other = (Pair) obj;
		if(second == null){
			if(other.second != null){
				return false;
			}
		}
		else if(!second.equals(other.second)){
			return false;
		}
		return true;
	}
}