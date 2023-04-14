package mattlib.internals.model;

import mattlib.ProcessPath;

import java.util.Optional;
import java.util.function.Supplier;

public interface ITuneFeature {

    <T> Optional<Supplier<T>> generateTuner(ProcessPath path, T defaultValue);

}
