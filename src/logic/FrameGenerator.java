package logic;

import model.CanFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public class FrameGenerator {
    RandomGenerator random = RandomGenerator.getDefault();

    public CanFrame generateCanFrame(int dlc){
        final int arbitrationId = random.nextInt(0, 1 << 11); // przedział [0;2^11)

        final List<Integer> payLoad = new ArrayList<>(dlc);

        for (int i = 0; i < dlc; i++)
            payLoad.add(random.nextInt(0, 256));


        return new CanFrame(arbitrationId, dlc, payLoad);
    }
}
