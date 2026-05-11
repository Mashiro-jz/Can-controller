import logic.ErrorDetector;
import logic.ErrorGenerator;
import logic.FrameGenerator;
import model.CanFrame;

public static void main() throws InterruptedException {
    int errorDataFramesToTest = 10_000_000;
    // Tablica 3D: [DLC 0-8][Błędy 1-16][Burst 2-30]
    int[][][] errorStatistic = new int[9][16][29];

    int cores = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(cores);

    // 1. Najpierw liczymy ile dokładnie będzie zadań (kombinacji)
    int totalTasks = 0;
    for (int d = 0; d <= 8; d++) {
        for (int e = 1; e <= 16; e++) {
            for (int b = 2; b <= 30; b++) {
                if (e <= b) totalTasks++;
            }
        }
    }

    AtomicInteger completedTasks = new AtomicInteger(0);
    final int finalTotalTasks = totalTasks;

    System.out.println("Uruchomiono symulację na " + cores + " wątkach.");
    System.out.println("Łączna liczba kombinacji do przeliczenia: " + finalTotalTasks);
    long startTime = System.nanoTime();

    for (int dlc = 0; dlc <= 8; dlc++) {
        final int currentDlc = dlc;
        for (int errors = 1; errors <= 16; errors++) {
            final int currentErrors = errors;
            for (int burst = 2; burst <= 30; burst++) {
                final int currentBurst = burst;

                if (currentErrors > currentBurst) continue;

                executor.submit(() -> {
                    // Lokalne instancje dla wątku
                    FrameGenerator generator = new FrameGenerator();
                    ErrorGenerator errorGenerator = new ErrorGenerator();
                    ErrorDetector errorDetector = new ErrorDetector();

                    CanFrame frame = generator.generateCanFrame(currentDlc);
                    int localFound = 0;

                    for (int i = 0; i < errorDataFramesToTest; i++) {
                        BitSet received = errorGenerator.generateErrorInCanFrame(frame, currentErrors, currentBurst);
                        localFound += errorDetector.detectErrorsInCanFrame(received, frame);
                    }

                    // Zapis wyniku do tablicy
                    errorStatistic[currentDlc][currentErrors - 1][currentBurst - 2] = errorDataFramesToTest - localFound;

                    // Aktualizacja paska postępu
                    int finished = completedTasks.incrementAndGet();
                    if (finished % 50 == 0 || finished == finalTotalTasks) {
                        double percent = (finished * 100.0) / finalTotalTasks;
                        System.out.printf("\rPostęp: %.2f%% (%d/%d kombinacji)", percent, finished, finalTotalTasks);
                    }
                });
            }
        }
    }

    executor.shutdown();
    executor.awaitTermination(24, TimeUnit.HOURS); // Czekamy na koniec wszystkich wątków

    long stopTime = System.nanoTime();
    long durationInSeconds = TimeUnit.NANOSECONDS.toSeconds(stopTime - startTime);

    System.out.println("\n\nKONIEC SYMULACJI");
    System.out.println("Czas operacji: " + durationInSeconds + " sekund.");
    System.out.println(Arrays.deepToString(errorStatistic));

    try (java.io.PrintWriter writer = new java.io.PrintWriter("wyniki_arq_can.txt")) {
        writer.println("DLC;Errors;Burst;Undetected_Errors"); // Nagłówek dla Excela

        for (int d = 0; d <= 8; d++) {
            for (int e = 1; e <= 16; e++) {
                for (int b = 2; b <= 30; b++) {
                    // Zapisujemy tylko te dane, które faktycznie liczyliśmy
                    if (e <= b) {
                        int value = errorStatistic[d][e - 1][b - 2];
                        writer.printf("%d;%d;%d;%d%n", d, e, b, value);
                    }
                }
            }
        }
        System.out.println("Wyniki zostały zapisane do pliku: wyniki_arq_can.txt");
    } catch (FileNotFoundException e) {
        System.err.println("Błąd zapisu pliku: " + e.getMessage());
    }
}