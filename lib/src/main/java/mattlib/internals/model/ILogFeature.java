package mattlib.internals.model;

import mattlib.ProcessPath;

import java.util.Optional;
import java.util.function.Consumer;

public interface ILogFeature {

    <T> Optional<Consumer<T>> generateLogger(ProcessPath path, Class<T> type);

}
