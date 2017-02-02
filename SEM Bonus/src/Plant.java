
public class Plant {
	final int ALPHA;
	final int BETA;
	final int C;
	final int[] A;
	
	public Plant(int alpha, int beta, int[] a, int c) {
		this.ALPHA = alpha;
		this.BETA = beta;
		this.A = a;
		this.C = c;
	}

	public int a(int i) {
		int result = 0;
		if(i < ALPHA)
			result = A[0];
		if(i >= ALPHA && i < BETA)
			result = A[1];
		if(i >= BETA)
			result = A[2];
		return result;
	}
}
