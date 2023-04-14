package mattlib.internals.impl;

import edu.wpi.first.networktables.NetworkTableInstance;
import mattlib.ProcessPath;
import mattlib.internals.model.ILogFeature;

import java.util.Optional;
import java.util.function.Consumer;

public class NetworkLogFeature implements ILogFeature {
    @Override
    public <T> Optional<Consumer<T>> generateLogger(ProcessPath path, Class<T> type) {
        if (type == Integer.class || type == int.class) {
            return Optional.of((a) -> {
                NetworkTableInstance.getDefault().getEntry("e").setInteger(Integer.toUnsignedLong((Integer) a));
            });
        }



        return Optional.empty();
    }
}
