import java.util.Scanner;
import Jama.Matrix;

/**
 * @author Sven Koepke, Daniel Heinz, Marius Laemmlin
 * @version 1.0
 */
public class Main {

	// Definition von Klassenvariablen
	final static int NO_OF_SIMS = 1000000; // Anzahl Simulationen (Wenn aktiv)
	static Matrix m, b_vec, x_vec; // Matrizen
	static Plant[] plants; // Feld der Plant-Instanzen
	static int states; // Anzahl der möglichen Zustände
	final static int[] ALPHA = { 3, 3, 2 }; // Alpha wie in Aufgabenstellung
	final static int[] BETA = { 8, 8, 6 }; // Beta wie in Aufgabenstellung
	final static int[][] A = { { 0, 3, 6 }, { 0, 2, 4 }, { 0, 1, 2 } }; // Abflussmengen
	final static int[] C = { 10, 10, 8 }; // Kapazität der Kraftwerke
	final static double GAMMA = 0.95; // Diskontierungsfaktor
	final static int REVENUE_PER_UNIT = 5; // Gewinn pro abgelassener Einheit Wasser
	final static int[] plants_Empty = { 0, 0, 0 }; // Leerer Anfangszustand der Kraftwerke

	/**
	 * Main-Methode zum Aufruf der Methoden und Interaktion mit dem User.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		loadConfig(); // Kraftwerke werden mit Konfigurationsparametern
						// instantiiert

		solveLGS(createMatrix(), createBVector()); // Matrix und b-Vektor
													// werden erstellt und LGS
													// geloest

		System.out.println("Erwarteter diskontierter Gesamtgewinn ausgehend von Zustand \n" + "V(0, 0, 0) = "
				+ x_vec.getArray()[compose(plants_Empty)][0] + "\n");

		System.out.println("Erwarteter Gewinn pro Zeitstufe ausgehend von Zustand \n" + "V(0, 0, 0) = "
				+ Sub.createExpectedRevenue(plants_Empty));

		// Scanner für User-Eingabe
		Scanner in = new Scanner(System.in);

		System.out.print(
				"\nGeben Sie die Anfangsfüllstände der Kraftwerke 1, 2 und 3 in folgender Form an: x y z. Verwenden Sie Leerzeichen zum Trennen der Werte.\nEingabe: ");
		int[] levels = new int[3];
		do {
			try {
				levels[0] = Integer.parseInt(in.next());
			} catch (NumberFormatException nfe) {
				break;
			}
			levels[1] = Integer.parseInt(in.next());
			levels[2] = Integer.parseInt(in.next());
			
			if(levels[0] > C[0] || levels[1] > C[1] || levels[2] > C[2]) {
				System.out.println("\nKein gültiger Startwert!\nEingabe: ");
				continue;
			}

			System.out.println("\nErwarteter diskontierter Gesamtgewinn ausgehend von Zustand \n" + "V(" + levels[0]
					+ ", " + levels[1] + ", " + levels[2] + ") = " + x_vec.getArray()[compose(levels)][0] + "\n");

			System.out.println("Erwarteter Gewinn pro Zeitstufe ausgehend von Zustand \n" + "V(" + levels[0] + ", "
					+ levels[1] + ", " + levels[2] + ") = " + Sub.createExpectedRevenue(levels));

			System.out.print(
					"_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n\nWeitere Startzustände (x y z). Abbruch mit beliebigem Buchstaben.\nEingabe: ");
		} while (true);
		
		// Simulationen zur Identifikation der rekurrenten Zustände. Zum Anzeigen, '//' entfernen.
		// simulate(4, 7, 2, NO_OF_SIMS);
		// System.out.println();
		// simulate(0, 2, 1, NO_OF_SIMS);
		// System.out.println();
		
		System.out.println("\nProgramm beendet.");
		in.close();

	}

	/**
	 * Methode simuliert den Prozess n mal ausgehend vom Uebergebenen
	 * Anfangszustand
	 * 
	 * @param i1
	 *            Anfangsfuellstand in Kraftwerk 1
	 * @param i2
	 *            Anfangsfuellstand in Kraftwerk 2
	 * @param i3
	 *            Anfangsfuellstand in Kraftwerk 3
	 * @param n
	 *            Anzahl der simulierten Perioden
	 */
	public static void simulate(int i1, int i2, int i3, int n) {
		int[] state = { i1, i2, i3 };
		int[] count = new int[states];
		for (int i = 0; i < states; i++) {
			count[i] = 0;
		}
		double rand = 0;
		int flow = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < states; j++) {
				if (compose(state) == j) {
					count[j] += 1;
				}
			}
			rand = (int) (Math.random() * 100) + 1;

			if (rand <= 20) {
				flow = 0;
			} else if ((rand <= 45) && (rand > 20)) {
				flow = 1;
			} else if ((rand <= 75) && (rand > 45)) {
				flow = 2;
			} else if (rand > 75) {
				flow = 3;
			}
			state = g(state, flow);

		}
		System.out.println("\nRelative Haeufigkeiten der Simluation von " + n
				+ " Perioden ausgehend vom Anfangszustand (" + i1 + ", " + i2 + ", " + i3 + "): ");
		for (int i = 0; i < states; i++) {
			if (count[i] > NO_OF_SIMS / 1000) {
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

		double[][] matrix = new double[states][states];
		for (int i = 0; i < states; i++) {
			for (int j = 0; j < states; j++) {
				if (i == j) {
					matrix[i][j] = 1 - GAMMA * p(i, j);
				} else {
					matrix[i][j] = -(GAMMA * p(i, j));
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
		double[][] b_vector = new double[states][1];
		for (int i = 0; i < states; i++) {
			b_vector[i][0] = revenue(i);
		}

		return b_vector;
	}

	/**
	 * Funktion loest das Gleichungssystem nach den einzelnen erwarteten
	 * diskontierten Gesamtgewinnen in Abhaengigkeit vom Anfangszustand
	 * 
	 * @param matrix
	 *            LGS wird in Matrixform (Gausssche Normalform) uebergeben
	 * @param b_vector
	 *            b-Vektor des LGS wird als Vektor uebergeben
	 */
	public static void solveLGS(double[][] matrix, double[][] b_vector) {
		m = new Matrix(matrix);
		b_vec = new Matrix(b_vector);
		x_vec = m.solve(b_vec);
	}

	/**
	 * Zustandsuebergangsfunktion g
	 * 
	 * @param i1
	 *            Fuellstand von Kraftwerk 1 zum Zeitpunkt n
	 * @param i2
	 *            Fuellstand von Kraftwerk 2 zum Zeitpunkt n
	 * @param i3
	 *            Fuellstand von Kraftwerk 3 zum Zeitpunkt n
	 * @param wn
	 *            Zufluss in Periode n
	 * @return Gibt Zustand zum Zeitpunkt n + 1 in Vektorform als Feld zurueck
	 */
	public static int[] g(int[] state, int wn) {
		int[] retVal = new int[3];
		int a1 = plants[0].a(state[0]);

		retVal[0] = state[0] - a1 + wn;
		retVal[1] = (state[1] - plants[1].a(state[1])) + (int) (a1 * (2.0 / 3.0));
		retVal[2] = (state[2] - plants[2].a(state[2])) + (int) (a1 * (1.0 / 3.0));

		return retVal;
	}

	/**
	 * Funktion zerlegt Indexwert in die Werte fuer i1, i2 und i3
	 * 
	 * @param i
	 *            Codierung eines Zustands-Vektors als Integer
	 * @return Gibt einen Zustand in Vektorform als Feld zurueck
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
	 *            Fuellstand von Kraftwerk 1
	 * @param i2
	 *            Fuellstand von Kraftwerk 2
	 * @param i3
	 *            Fuellstand von Kraftwerk 3
	 * @return Gibt eine Codierung des Zustands-Vektors als Ganzzahl zurueck
	 */
	public static int compose(int i1, int i2, int i3) {
		return (99 * i1 + 9 * i2 + i3);
	}

	/**
	 * Funktion erstellt Indexwert aus als Array uebergebenen i1, i2 und i3
	 * 
	 * @param array
	 *            Fuellstaende der drei Kraftwerke als Array
	 * @return Gibt die eindeutige Codierung des Zustands-Vektors als Ganzzahl
	 *         zurueck
	 */
	public static int compose(int[] array) {
		return compose(array[0], array[1], array[2]);
	}

	/**
	 * Funktion gibt den Wahrscheinlichkeitswert des Uebergangs von Zustand ind1
	 * in Zustand ind 2 zurueck
	 * 
	 * @param state1
	 *            Zustand i(n) als Integer-Array
	 * @param state2
	 *            Zustand i(n+1) als Integer-Array
	 * @return Wahrscheinlichkeitswert des Zustandsuebergangs von i(n) nach
	 *         i(n+1)
	 */
	public static double p(int[] state1, int[] state2) {
		double retVal = 0;

		if (equalsState(g(state1, 0), state2)) {
			retVal += 0.2;
		}
		if (equalsState(g(state1, 1), state2)) {
			retVal += 0.25;
		}
		if (equalsState(g(state1, 2), state2)) {
			retVal += 0.3;
		}
		if (equalsState(g(state1, 3), state2)) {
			retVal += 0.25;
		}
		return retVal;
	}

	/**
	 * Funktion gibt den Wahrscheinlichkeitswert des Uebergangs von Zustand
	 * state1 in Zustand state2 zurueck
	 * 
	 * @param state1
	 *            Zustand i(n) als kodierte Integer
	 * @param state2
	 *            Zustand i(n+1) als kodierte Integer
	 * @return Wahrscheinlichkeitswert des Zustandsuebergangs von i(n) nach
	 *         i(n+1)
	 */
	public static double p(int state1, int state2) {
		return p(decompose(state1), decompose(state2));
	}

	/**
	 * Vergleicht zwei Zustaende auf Uebereinstimmung
	 * 
	 * @param state1
	 *            Zustand in Periode n
	 * @param state2
	 *            Zustand in Periode n + 1
	 * @return Wahrheitsweit, ob Zustaende ind1 und ind2 uebereinstimmen
	 */
	public static boolean equalsState(int[] state1, int[] state2) {
		return ((state2[0] == state1[0]) && (state2[1] == state1[1]) && (state2[2] == state1[2]));
	}

	/**
	 * Gibt den Revenue in der auf den i(n) folgenden Periode wieder
	 * 
	 * @param state
	 *            Zustand als Integer-Array der Fuellstaende
	 * @return Mit 5 GE gewichtete Summe der Abfluesse einer auf einen Zustand
	 *         folgenden Periode
	 */
	public static double revenue(int[] state) {
		return REVENUE_PER_UNIT * (plants[0].a(state[0]) + plants[1].a(state[1]) + plants[2].a(state[2]));
	}

	/**
	 * Gibt den Revenue in der auf den i(n) folgenden Periode wieder
	 * 
	 * @param state
	 *            Zustand als Integer-Codierung der Fuellstaende
	 * @return Mit 5 GE gewichtete Summe der Abfluesse einer auf einen Zustand
	 *         folgenden Periode
	 */
	public static double revenue(int state) {
		return revenue(decompose(state));
	}

	/**
	 * Laedt alle Werte aus der Konfiguration und erstellt Plant-Instanzen
	 */
	public static void loadConfig() {
		// Kraftwerke erstellen
		plants = new Plant[3];
		for (int i = 0; i < plants.length; i++) {
			plants[i] = new Plant(ALPHA[i], BETA[i], A[i], C[i]);
		}

		// Anzahl Zustaende bestimmen
		states = 1;
		for (Plant x : plants)
			states = states * (x.C + 1);
	}
}
