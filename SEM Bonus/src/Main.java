
// Author: Sven Köpke

import Jama.Matrix;

public class Main {

	public static void main(String[] args) {

		// displayPValsPositiveOnly();

		solveLGS(createMatrix(), createBVector()); // Matrix und b-Vektor
													// werden erstellt und LGS
													// gelöst

		simulate(4, 7, 2, 1000000);
		System.out.println();
		simulate(0, 2, 1, 1000000);
		System.out.println();
		Sub.Main_2();

		// displayPValsPositiveOnly();

		int[] feld = { 10, 10, 8 };
		System.out.println(g(compose(feld), 2)[0]);
		System.out.println(g(compose(feld), 2)[1]);
		System.out.println(g(compose(feld), 2)[2]);

		System.out.println(drain_1(feld[0]));

	}

	/**
	 * Methode simuliert den Prozess n mal ausgehend vom übergebenen
	 * Anfangszustand
	 * 
	 * @param i1
	 *            Anfangsfüllstand in Kraftwerk 1
	 * @param i2
	 *            Anfangsfüllstand in Kraftwerk 2
	 * @param i3
	 *            Anfangsfüllstand in Kraftwerk 3
	 * @param n
	 *            Anzahl der simulierten Perioden
	 */
	public static void simulate(int i1, int i2, int i3, int n) {
		int[] state = { i1, i2, i3 };
		int[] count = new int[1089];
		for (int i = 0; i < 1089; i++) {
			count[i] = 0;
		}
		double rand = 0;
		int flow = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 1089; j++) {
				if (compose(state) == j) {
					count[j] += 1;
				}
			}
			rand = (int) (Math.random() * 100) + 1;

			if (rand <= 20) {
				flow = 0;
			} else if ((rand <= 55) && (rand > 20)) {
				flow = 1;
			} else if ((rand <= 75) && (rand > 55)) {
				flow = 2;
			} else if (rand > 75) {
				flow = 3;
			}
			state = g(compose(state), flow);

		}
		System.out.println("\nRelative Häufigkeiten der Simluation von " + n
				+ " Perioden ausgehend vom Anfangszustand (" + i1 + ", " + i2 + ", " + i3 + "): ");
		for (int i = 0; i < 1089; i++) {
			if (count[i] > 100) {
				System.out.println("Count(" + decompose(i)[0] + ", " + decompose(i)[1] + ", " + decompose(i)[2] + ") = "
						+ count[i] / ((double) n));
			}
		}
	}

	/**
	 * Funktion erstellt linke seite des linearen Gleichungssystems als Matrix
	 * (zweidimensionales Feld), die das LGS in der Gausschen Normalform
	 * darstellt
	 * 
	 * @return Linke Seite des LGS als 2-dimensionales Array
	 */
	public static double[][] createMatrix() {
		double[][] matrix = new double[1089][1089];
		for (int i = 0; i < 1089; i++) {
			for (int j = 0; j < 1089; j++) {
				if (i == j) {
					matrix[i][j] = 1 - 0.95 * pVal(i, j);
				} else {
					matrix[i][j] = -(0.95 * pVal(i, j));
				}
			}
		}
		return matrix;
	}

	/**
	 * Funktion erstellt rechte Seite des linearen Gleichungssystems
	 * 
	 * @return b-Vektor des LGS
	 */
	public static double[][] createBVector() {
		double[][] b_vector = new double[1089][1];
		for (int i = 0; i < 1089; i++) {
			b_vector[i][0] = revenue(decompose(i));
		}

		return b_vector;
	}

	/**
	 * Funktion löst das Gleichungssystem nach den einzelnen erwarteten
	 * diskontierten Gesamtgewinnen in Abhängigkeit vom Anfangszustand
	 * 
	 * @param matrix
	 *            LGS wird in Matrixform (Gausssche Normalform) übergeben
	 * @param b_vector
	 *            b-Vektor des LGS wird als Vektor übergeben
	 */
	public static void solveLGS(double[][] matrix, double[][] b_vector) {
		Matrix m, b_vec, x_vec;
		m = new Matrix(matrix);
		b_vec = new Matrix(b_vector);
		x_vec = m.solve(b_vec);

		for (int i = 0; i < 1089; i++) {
			System.out.println("V(" + decompose(i)[0] + ", " + decompose(i)[1] + ", " + decompose(i)[2] + ") = "
					+ x_vec.getArray()[i][0]);
		}

	}

	/**
	 * Gibt die berechneten p-Werte auf der Konsole aus (nur positive Werte
	 * werden ausgegeben)
	 */
	public static void displayPValsPositiveOnly() {
		for (int i = 0; i < 1089; i++) {
			for (int j = 0; j < 1089; j++) {
				if (pVal(i, j) != 0) {
					System.out.println("pVal(" + decompose(i)[0] + ", " + decompose(i)[1] + ", " + decompose(i)[2]
							+ "; " + decompose(j)[0] + ", " + decompose(j)[1] + ", " + decompose(j)[2] + ") = "
							+ pVal(i, j));
				}
			}
		}
	}

	/**
	 * Zustandsübergangsfunktion g
	 * 
	 * @param i1
	 *            Füllstand von Kraftwerk 1 zum Zeitpunkt n
	 * @param i2
	 *            Füllstand von Kraftwerk 2 zum Zeitpunkt n
	 * @param i3
	 *            Füllstand von Kraftwerk 3 zum Zeitpunkt n
	 * @param wn
	 *            Zufluss in Periode n
	 * @return Gibt Zustand zum Zeitpunkt n + 1 in Vektorform als Feld zurück
	 */
	public static int[] g(int state, int wn) {
		int i1 = decompose(state)[0];
		int i2 = decompose(state)[1];
		int i3 = decompose(state)[2];
		int[] retVal = new int[3];
		int drain1 = drain_1(i1);

		retVal[0] = i1 - drain1 + wn;
		retVal[1] = (i2 - drain_2(i2)) + (int) (drain1 * (2.0 / 3.0));
		retVal[2] = (i3 - drain_3(i3)) + (int) (drain1 * (1.0 / 3.0));

		return retVal;
	}

	/**
	 * Funktion gibt Abfluss aus Kraftwerk 1 bei gegebenem Wasserstand zurück
	 * 
	 * @param in
	 *            Füllstand in Kraftwerk 1 zum Zeitpunkt n
	 * @return Gibt Abfluss aus Kraftwerk 1 nach Zeitpunkt n zurück
	 */
	public static int drain_1(int in) {
		int val = 0;
		if (in >= 8) {
			val = 6;
		} else if (in >= 3) {
			val = 3;
		} else {
			val = 0;
		}

		return val;
	}

	/**
	 * Funktion gibt Abfluss aus Kraftwerk 2 bei gegebenem Wasserstand zurück
	 * 
	 * @param in
	 *            Füllstand in Kraftwerk 2 zum Zeitpunkt n
	 * @return Gibt Abfluss aus Kraftwerk 2 nach Zeitpunkt n zurück
	 */
	public static int drain_2(int in) {
		int val = 0;
		if (in >= 8) {
			val = 4;
		} else if (in >= 3) {
			val = 2;
		} else {
			val = 0;
		}
		return val;
	}

	/**
	 * Funktion gibt Abfluss aus Kraftwerk 3 bei gegebenem Wasserstand zurück
	 * 
	 * @param in
	 *            Füllstand in Kraftwerk 3 zum Zeitpunkt n
	 * @return Gibt Abfluss aus Kraftwerk 3 nach Zeitpunkt n zurück
	 */
	public static int drain_3(int in) {
		int val = 0;
		if (in >= 6) {
			val = 2;
		} else if (in >= 2) {
			val = 1;
		} else {
			val = 0;
		}
		return val;
	}

	/**
	 * Funktion zerlegt Indexwert in die Werte für i1, i2 und i3
	 * 
	 * @param i
	 *            Codierung eines Zustands-Vektors als Integer
	 * @return Gibt einen Zustand in Vektorform als Feld zurück
	 */
	public static int[] decompose(int i) {
		int[] vals = new int[3];
		vals[0] = i / 99;
		vals[1] = (i % 99) / 9;
		vals[2] = (i % 99) % 9;
		return vals;
	}

	/**
	 * Funktion erstellt Indexwert aus gegebenen i1, i2 und i3
	 * 
	 * @param i1
	 *            Füllstand von Kraftwerk 1
	 * @param i2
	 *            Füllstand von Kraftwerk 2
	 * @param i3
	 *            Füllstand von Kraftwerk 3
	 * @return Gibt eine Codierung des Zustands-Vektors als Ganzzahl zurück
	 */
	public static int compose(int i1, int i2, int i3) {
		return (99 * i1 + 9 * i2 + i3);
	}

	/**
	 * Funktion erstellt Indexwert aus als Array übergebenen i1, i2 und i3
	 * 
	 * @param array
	 *            Füllstände der drei Kraftwerke als Array
	 * @return Gibt die eindeutige Codierung des Zustands-Vektors als Ganzzahl
	 *         zurück
	 */
	public static int compose(int[] array) {
		return compose(array[0], array[1], array[2]);
	}

	// Funktion gibt den Wahrscheinlichkeitswert des Übergangs von Zustand ind1
	// in Zustand ind 2 zurück
	/**
	 * @param ind1
	 *            Zustand i(n) als Integer
	 * @param ind2
	 *            Zustand i(n+1) als Integer
	 * @return Wahrscheinlichkeitswert des Zustandsübergangs von i(n) nach
	 *         i(n+1)
	 */
	public static double pVal(int ind1, int ind2) {
		double retVal = 0;

		if (equalsState(compose(g(ind1, 0)), ind2)) {
			retVal += 0.2;
		}
		if (equalsState(compose(g(ind1, 1)), ind2)) {
			retVal += 0.25;
		}
		if (equalsState(compose(g(ind1, 2)), ind2)) {
			retVal += 0.3;
		}
		if (equalsState(compose(g(ind1, 3)), ind2)) {
			retVal += 0.25;
		}
		return retVal;
	}

	/**
	 * @param ind1
	 *            Zustand in Periode n
	 * @param ind2
	 *            Zustand in Periode n + 1
	 * @return Wahrheitsweit, ob Zustände ind1 und ind2 übereinstimmen
	 */
	public static boolean equalsState(int ind1, int ind2) {
		int[] vals1 = decompose(ind1);
		int[] vals2 = decompose(ind2);
		return ((vals2[0] == vals1[0]) && (vals2[1] == vals1[1]) && (vals2[2] == vals1[2]));
	}

	// Gibt den Revenue in der auf den i(n) folgenden Periode wieder

	/**
	 * @param state
	 *            Zustand als Integer-Codierung der Füllstände
	 * @return Mit 5 GE gewichtete Summe der Abflüsse einer auf einen Zustand
	 *         folgenden Periode
	 */
	public static double revenue(int[] state) {

		double outflow1 = drain_1(state[0]);
		double outflow2 = drain_2(state[1]);
		double outflow3 = drain_3(state[2]);
		return 5 * (outflow1 + outflow2 + outflow3);
	}
}
