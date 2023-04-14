package mattlib.internals.impl;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import mattlib.ProcessPath;
import mattlib.internals.model.ITuneFeature;

import java.util.Optional;
import java.util.function.Supplier;

public class NetworkTuneFeature implements ITuneFeature {


    //TODO this is hacky and dumb

    @Override
    public <T> Optional<Supplier<T>> generateTuner(ProcessPath path, T defaultValue) {
        return Optional.ofNullable(getCorrectSupplier(path, defaultValue));
    }

    <T> Supplier<T> getCorrectSupplier(ProcessPath path, T defaultValue) {
        Class<?> returnType = defaultValue.getClass();
        NetworkTableEntry entry = NetworkTableInstance.getDefault().getEntry(path.getAsTablePath());

        if (returnType == Double.class) { //handle doubles
            Supplier<Double> supplier = () -> entry.getDouble((Double) defaultValue);

            return (Supplier<T>) supplier;
        }

        if (returnType == Long.class) { //handle longs
            Supplier<Long> supplier = () -> entry.getInteger((Long) defaultValue);

            return (Supplier<T>) supplier;
        }

        if (returnType == String.class) {
            Supplier<String> supplier = () -> entry.getString((String) defaultValue);

            return (Supplier<T>) supplier;
        }


        //TODO handle double arrays

        return null;

    }
}
