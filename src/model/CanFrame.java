package model;

import logic.Crc15Calculator;

import java.util.BitSet;
import java.util.List;

public class CanFrame {
    // 11 bitów ID
    private final BitSet arbitrationId;

    // 4 bity Data Length Code (od 0000 do 1000)
    private final int dataLengthCode;
    // przesyłana zawartość
    private final BitSet payLoad;

    Crc15Calculator crc15Calculator = new Crc15Calculator();

    public CanFrame(int arbitrationId, int dataLengthCode, List<Integer> payLoad) {
        this.arbitrationId = createBitSetFromLong(arbitrationId);
        this.dataLengthCode = dataLengthCode;
        this.payLoad = new BitSet();

        for(int i = 0; i < payLoad.size(); i++){
            appendByteToBitSet(this.payLoad, payLoad.get(i), i * 8);
        }
    }

    // potrzebne, aby mieć długość ramki
    public int getFrameLength() {
        return 1 + 11 + 1 + 1 + 1 + 4 + (dataLengthCode * 8) + 15 + 1 + 1 + 1 + 7;
    }

    // potrzebne w ErrorDetector.java
    public int getDataLengthCode() {
        return dataLengthCode;
    }

    public BitSet getFullFrame(){
        // Tutaj zwracamy ciąg bitów potrzebnych do obliczenia CRC-15, czyli:
        // SOF, pole arbitażu, pole controli, pole danych
        BitSet result = new BitSet();
        int currentPosition = 0;

        // SOF
        result.set(currentPosition++, false);

        // Arbitraż 11 bitów ID,
        for(int i = 10; i >= 0; i--){
            result.set(currentPosition++, arbitrationId.get(i));
        }
        // 1 bit RTR (tutaj ustawiłem stałe 0)
        result.set(currentPosition++, false);

        // Control Field, 1 bit - IDE, 1 bit - r0 (zarezerwowany), 4 bity - DLC
        result.set(currentPosition++, false); // IDE = 0
        result.set(currentPosition++, false); // r0 = 0

        // DLC
        for(int i = 3; i >= 0; i--){
            boolean bit = ((dataLengthCode >> i) & 1) == 1; // tutaj zamieniam liczbę dataLengthCode na system binarny (5 -> 0101)
            result.set(currentPosition++, bit);
        }

        // Data Field od 0 do 64 bitów
        for (int i = 0; i < dataLengthCode * 8; i++){
            result.set(currentPosition++, payLoad.get(i));
        }

        // Obliczanie CRC-15
        BitSet crcValue = crc15Calculator.compute(result, currentPosition);


        // CRC-15
        for(int i = 14; i >= 0; i--)
            result.set(currentPosition++, crcValue.get(i));

        //CRC Delimiter
        result.set(currentPosition++, true); // zawsze recesywny

        // ACK slot - 1 bit (odbiorca zmienia go jeśli jest dobrze na 0)
        result.set(currentPosition++, true);

        // ACK Delimiter - 1 bit
        result.set(currentPosition++, true); // zawsze recesywny

        // EOF - 7 bitów, wszystkie recesywne
        for(int i = 0; i < 7; i++)
            result.set(currentPosition++, true);

        return result;
    }


    // FUNKCJE POMOCNICZE

    // Funkcja zamieniająca z wartości HEX na BitSet (np. z 0x245 na BitSet)
    public BitSet createBitSetFromLong(long value){
        return BitSet.valueOf(new long[]{value});
    }

    // Dodawanie bajtów do bitsetu jako bity
    private void appendByteToBitSet(BitSet bitSet, int value, int startOffset) {
        for (int i = 7; i >= 0; i--) {
            if (((value >> i) & 1) == 1) {
                bitSet.set(startOffset + (7 - i));
            }
        }
    }


    // PONIŻEJ: SKOPIOWANE Z CHATU, ABY ŁADNIE SIĘ WYŚWIETLAŁO / NIE CZYTAĆ
    @Override
    public String toString() {
        BitSet frame = getFullFrame();
        // Obliczamy całkowitą długość ramki:
        // SOF(1) + ID(11) + RTR(1) + IDE(1) + r0(1) + DLC(4) + Data(DLC*8) + CRC(15) + DEL(1) + ACK(1) + DEL(1) + EOF(7)
        int totalLength = 1 + 11 + 1 + 1 + 1 + 4 + (dataLengthCode * 8) + 15 + 1 + 1 + 1 + 7;

        StringBuilder sb = new StringBuilder();
        sb.append("CAN Frame (Standard 2.0A):\n");
        sb.append("--------------------------------------------------\n");

        int p = 0;
        sb.append("SOF: ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("ID : ").append(getBits(frame, p, 11)).append(" (Hex: 0x").append(Long.toHexString(idToLong())).append(")\n"); p += 11;
        sb.append("RTR: ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("IDE: ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("r0 : ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("DLC: ").append(getBits(frame, p, 4)).append(" (Value: ").append(dataLengthCode).append(")\n"); p += 4;

        if (dataLengthCode > 0) {
            sb.append("DATA: ").append(getBits(frame, p, dataLengthCode * 8)).append("\n");
            p += dataLengthCode * 8;
        }

        sb.append("CRC: ").append(getBits(frame, p, 15)).append("\n"); p += 15;
        sb.append("DEL: ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("ACK: ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("DEL: ").append(getBits(frame, p, 1)).append("\n"); p += 1;
        sb.append("EOF: ").append(getBits(frame, p, 7)).append("\n");

        sb.append("--------------------------------------------------\n");
        sb.append("OSTATECZNIE: ").append(getBits(frame, 0, totalLength));

        return sb.toString();
    }

    // Pomocnicza metoda do wyciągania fragmentu bitów jako String
    private String getBits(BitSet bits, int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            sb.append(bits.get(i) ? "1" : "0");
        }
        return sb.toString();
    }

    // Pomocnicza metoda do wyświetlenia ID w Hex w toString
    private long idToLong() {
        long val = 0;
        for (int i = 0; i < 11; i++) {
            if (arbitrationId.get(i)) val |= (1L << i);
        }
        return val;
    }
}
