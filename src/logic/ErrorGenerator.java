package logic;

import model.CanFrame;

import java.util.*;

public class ErrorGenerator {
    java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();

    public BitSet generateErrorInCanFrame(CanFrame canFrame, int errors, int burstLength){
        BitSet frame = canFrame.getFullFrame();
        int dlc = canFrame.getDataLengthCode();
        int SoFToCRCBitAmount = 19 + (dlc * 8) + 15;

        Set<Integer> errorIndexInCanFrame  = new HashSet<>();
        int counter = 0;

        int burstStart = random.nextInt(0,SoFToCRCBitAmount-burstLength + 1);
        int burstEnd = burstStart + burstLength;

        while(counter < errors) {
            int indexForErrorBit = random.nextInt(burstStart,burstEnd);
            if(!errorIndexInCanFrame.contains(indexForErrorBit)){
                errorIndexInCanFrame.add(indexForErrorBit);
                frame.flip(indexForErrorBit);

                counter++;
            }
        }

        return frame;
    }
}

//        counterForErrorInCurrentFrame = 0;
//        // jeśli wylosowana pseudolosowa liczba jest <= 0.35 w zakresie [0.0;1.0) to zmieniamy ramkę
//        // symulując powstawanie błędów, przy wartości 0.35 szansa na uzyskanie błędnej ramki CAN to 35% (przyjęte na sztywno)
//        if(random.nextDouble()<= 0.4) {
//            counterForErrorFrames++;
//            int amountOfErrorInNewCanFrame = random.nextInt(15) + 1; // losujemy ile błędów wstawimy do ramki CAN (+1, bo zakres musi być od 1)
//
//            for (int i = 0; i < amountOfErrorInNewCanFrame; i++) {
//                counterForTotalAmountOfErrors++;
//                counterForErrorInCurrentFrame++;
//                int bitToChange = random.nextInt(frameLength); // który bit zmieniamy z zakresu [0;frameLength]
//                frame.flip(bitToChange); // odwracamy bit o wylosowanym indeksie
//            }
//        }
