package logic;

import model.CanFrame;

import java.util.BitSet;

public class ErrorDetector {
    Crc15Calculator crc15Calculator = new Crc15Calculator();

    /**
     * Główne zadanie tej klasy, jeśli wykryje błąd to nie zmieni ACK Slot na 0 oraz zwrócić 1 do statystyk
     */
    public int detectErrorsInCanFrame(BitSet receivedFrame, CanFrame canFrame) {
        int dlc = canFrame.getDataLengthCode();
        int ackSlotIndex = 19 + (dlc * 8) + 15 + 1;

        if (hasCrcError(receivedFrame, canFrame)) {
            receivedFrame.set(ackSlotIndex, true);
            return 1;
        }
        return 0;
    }

    /**
     * 1. FORM ERROR - Obliczamy CRC oraz sprawdzamy poprawność z otrzymamym CRC
     * Funkcja zwraca FALSE jeśli wystąpił błąd podczas sprawdzania CRC
     */
    boolean hasCrcError(BitSet receivedFrame, CanFrame canFrame) {
        int dlc = canFrame.getDataLengthCode();
        int dataBitCount = 19 + dlc * 8;

        BitSet bitSetFromSoFToDF = receivedFrame.get(0, 19 + dlc * 8); // od najstarszego bitu do końca pola DATA
        BitSet crcFromCanFrame = receivedFrame.get(dataBitCount, dataBitCount + 15); // od pierwszego bitu CRC do ostatniego bitu CRC

        BitSet calculatedCRC = crc15Calculator.compute(bitSetFromSoFToDF, 19 + dlc * 8); // sztywna długość ramki

        for (int i = 0; i < 15; i++) {
            if (calculatedCRC.get(14 - i) != crcFromCanFrame.get(i))
                return true; // true = znaleziono błąd
        }
        return false;
    }

    /**
     * 2. FORM ERROR - Sprawdza czy pola o stałych wartościach są poprawne.
     * Funkcja zwraca FALSE jeśli nie ma błędów
     */
    public boolean hasFormError(BitSet receivedFrame, CanFrame canFrame) {
        int dlc = canFrame.getDataLengthCode();

        // Wyliczamy pozycje stałych pól
        int crcDelimiterIdx = 19 + (dlc * 8) + 15;
        int ackDelimiterIdx = crcDelimiterIdx + 2; // pomijamy slot ACK (index +1)
        int eofStartIndex = ackDelimiterIdx + 1;

        // CRC Delimiter musi być 1
        if (!receivedFrame.get(crcDelimiterIdx)) return true;

        // ACK Delimiter musi być 1
        if (!receivedFrame.get(ackDelimiterIdx)) return true;

        // EOF musi składać się z 7 bitów o wartości 1
        for (int i = 0; i < 7; i++) {
            if (!receivedFrame.get(eofStartIndex + i)) {
                return true; // true - znaleziono błąd
            }
        }

        return false;
    }
}