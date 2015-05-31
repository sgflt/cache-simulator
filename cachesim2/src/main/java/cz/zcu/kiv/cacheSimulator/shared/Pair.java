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

  public Pair(final A first, final B second) {
    super();
    this.first = first;
    this.second = second;
  }

  @Override
  public String toString() {
    return "(" + this.first + ", " + this.second + ")";
  }

  public A getFirst() {
    return this.first;
  }

  public void setFirst(final A first) {
    this.first = first;
  }

  public B getSecond() {
    return this.second;
  }

  public void setSecond(final B second) {
    this.second = second;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if(this == obj){
      return true;
    }
    if(obj == null){
      return false;
    }
    if(!(obj instanceof Pair)){
      return false;
    }
    final Pair<?, ?> other = (Pair<?, ?>) obj;
    if(this.second == null){
      if(other.second != null){
        return false;
      }
    }
    else if(!this.second.equals(other.second)){
      return false;
    }
    return true;
  }
}
