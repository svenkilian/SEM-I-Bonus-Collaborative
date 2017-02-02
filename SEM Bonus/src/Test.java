public class Test {

	public static void main(String[] args) {
		simulate(0,0,0,100000);
	}

	public static void simulate(int i1, int i2, int i3, int n) {
		int[] count = new int[4];
		for (int i = 0; i < 4; i++) {
			count[i] = 0;
		}
		double rand = 0;
		
		for (int i = 0; i < n; i++) {
			rand = (int) (Math.random() * 100) + 1;

			if (rand <= 20) {
				count[0]++;
			} else if ((rand <= 55) && (rand > 20)) {
				count[1]++;
			} else if ((rand <= 75) && (rand > 55)) {
				count[2]++;
			} else if (rand > 75) {
				count[3]++;
			}


		}
		for (int i = 0; i < 4; i++) {
			System.out.print(i+": "+ count[i]/(double)n + " | ");
		}
	}
}
