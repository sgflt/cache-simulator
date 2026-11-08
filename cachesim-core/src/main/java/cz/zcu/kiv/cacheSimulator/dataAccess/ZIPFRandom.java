package cz.zcu.kiv.cacheSimulator.dataAccess;
import java.util.Random;

/**
 * trida pro generovani nahodnych cisel s ZIPF distribuci 
 * @author Pavel Bzoch
 *
 */
public class ZIPFRandom {

	/**
	 * cumulative probability of each value, built on first use
	 */
	private double[] cumulative;

	/**
	 * Normalization constant
	 */
	private double c = 0;

	/**
	 * random generator
	 */
	private final Random rd;

	/**
	 * horni mez pro generovani cisel
	 */
	private final int n;

	/**
	 * nastaveni yipf generatoru
	 */
	private final double alpha;

	/**
	 * konstruktor - inicializace promennych
	 * @param n
	 * @param alpha
	 */
	public ZIPFRandom(final int n, final double alpha) {
		this.n = n;
		this.alpha = alpha;
		this.rd = new Random(0);
	}

	/**
	 * metoda, ktera vraci dalsi nahodnou hodnotu
	 * @return nahodna hodnota
	 */
	public int zipfNext() {
		// Cumulative probabilities, built on first call only. They are summed in the same
		// order as the original linear scan, so the results are bit-for-bit the same.
		if (this.cumulative == null) {
			for (int i = 1; i <= this.n; i++) {
				this.c = this.c + (1.0 / Math.pow((double) i, this.alpha));
			}
			this.c = 1.0 / this.c;
			this.cumulative = new double[this.n];
			double sum = 0;
			for (int i = 1; i <= this.n; i++) {
				sum = sum + this.c / Math.pow((double) i, this.alpha);
				this.cumulative[i - 1] = sum;
			}
		}

		// Pull a uniform random number (0 < z < 1)
		double z;
		do {
			z = this.rd.nextDouble();
		} while ((z == 0) || (z == 1));

		// first value whose cumulative probability reaches z
		int low = 0;
		int high = this.n - 1;
		while (low < high) {
			final int mid = (low + high) >>> 1;
			if (this.cumulative[mid] >= z) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		// rounding can leave the last sum just under z; the linear scan then found nothing
		return this.n > 0 && this.cumulative[low] >= z ? low : -1;
	}
}
