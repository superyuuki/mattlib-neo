package mattlib.util;

import com.amihaiemil.eoyaml.*;
import com.amihaiemil.eoyaml.extensions.MergedYamlMapping;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CombinedYamlMappingBuilder implements YamlMappingBuilder {

    private final Map<YamlNode, YamlNode> pairs;

    /**
     * Default ctor.
     */
    public CombinedYamlMappingBuilder() {
        this(new LinkedHashMap<>());
    }


    /**
     * Constructor.
     * @param pairs Pairs used in building the YamlMapping.
     */
    public CombinedYamlMappingBuilder(final Map<YamlNode, YamlNode> pairs) {
        this.pairs = pairs;
    }

    @Override
    public YamlMappingBuilder add(final String key, final String value) {
        return this.add(
                new PublicScalar(key),
                new PublicScalar(value)
        );
    }

    @Override
    public YamlMappingBuilder add(final YamlNode key, final String value) {
        return this.add(key, new PublicScalar(value));
    }

    @Override
    public YamlMappingBuilder add(final String key, final YamlNode value) {
        return this.add(new PublicScalar(key), value);
    }

    @Override
    public YamlMappingBuilder add(final YamlNode key, final YamlNode value) {
        if(key == null || key.isEmpty()) {
            throw new IllegalArgumentException(
                    "The key in YamlMapping cannot be null or empty!"
            );
        }
        final Map<YamlNode, YamlNode> withAdded = new LinkedHashMap<>(this.pairs);

        if (withAdded.containsKey(key)) {
            YamlNode existing = withAdded.get(key);

            if (existing instanceof YamlMapping && value instanceof YamlMapping) {
                YamlNode together = new MergedYamlMapping((YamlMapping) existing, (YamlMapping) value);
                withAdded.put(key, together);
            } else {
                //TODO this needs to be more descriptive
                throw new IllegalArgumentException("You have a scalar that is also a map. Do not do that.");
            }

        } else {
            withAdded.put(key, value);
        }


        return new CombinedYamlMappingBuilder(withAdded);
    }

    @Override
    public YamlMapping build(final String comment) {
        YamlMapping mapping = new CombinedYamlMapping(this.pairs, comment);
        if (pairs.isEmpty()) {
            mapping = new EmptyYamlMapping(mapping);
        }
        return mapping;
    }
}
