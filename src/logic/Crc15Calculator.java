package logic;

import java.util.BitSet;

public class Crc15Calculator {

    // Wielomian CAN: 0x4599 (bez bitu x^15)
    public static final int CAN_POLYNOMIAL = 0x4599;
    private static final int CRC_MASK = 0x7FFF; // Maska dla 15 bitów

    // Oblicza CRC-15, bitStream to bitSet zawierające dane do obliczenia CRC, bitCOunt to ilość bitów, która ma ramka
    // do obliczenia CRC
    public BitSet compute(BitSet bitStream, int bitCount) {
        int crc = 0;

        for(int i = 0; i < bitCount; i++){
            boolean msb = (crc & 0x4000) != 0;
            boolean currentBit = bitStream.get(i);

            crc <<= 1;
            if(currentBit) crc |= 1;

            if(msb) crc ^= CAN_POLYNOMIAL;

            crc &= CRC_MASK;
        }

        for (int i = 0; i < 15; i++){
            boolean msb = (crc & 0x4000) != 0;
            crc <<= 1;
            if(msb) crc ^= CAN_POLYNOMIAL;

            crc &= CRC_MASK;
        }

        return convertIntToBitSet(crc, 15); // stale ustalowa wartość 15, bo działamy na CRC-15
    }

    // Funkcja pomocnicza - zamieniająca liczbę int na BitSet, drugi argument to ilość liczb, która ta liczba ma w systemie binarnym
    private BitSet convertIntToBitSet(int value, int length) {
        BitSet result = new BitSet(length);
        for (int i = 0; i < length; i++) {
            if (((value >> i) & 1) == 1) {
                result.set(i);
            }
        }
        return result;
    }
}