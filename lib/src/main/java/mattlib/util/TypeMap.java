package mattlib.util;

import mattlib.ProcessPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeMap {

    final Map<ProcessPath, Object> backingMap;

    public TypeMap(Map<ProcessPath, Object> backingMap) {
        this.backingMap = backingMap; //todo copy
    }

    @SuppressWarnings("unchecked")
    public <T> T requestForPath(ProcessPath path) {

        Object internal = backingMap.get(path);
        if (internal == null) throw new IllegalStateException("HOW");

        return (T) internal;

    }
}
