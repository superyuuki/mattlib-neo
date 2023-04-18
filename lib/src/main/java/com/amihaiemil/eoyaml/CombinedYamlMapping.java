package com.amihaiemil.eoyaml;

import com.amihaiemil.eoyaml.BaseYamlMapping;
import com.amihaiemil.eoyaml.Comment;
import com.amihaiemil.eoyaml.YamlNode;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CombinedYamlMapping extends BaseYamlMapping {


    /**
     * Comments referring to this mapping.
     */
    private Comment comment;

    /**
     * Key:value linked map (maintains the order of insertion).
     */
    private final Map<YamlNode, YamlNode> mappings =
            new LinkedHashMap<>();

    /**
     * Ctor.
     * @param entries Entries contained in this mapping.
     */
    CombinedYamlMapping(final Map<YamlNode, YamlNode> entries) {
        this(entries, "");
    }

    /**
     * Ctor.
     * @param entries Entries contained in this mapping.
     * @param comment Comment on top of this YamlMapping.
     */
    CombinedYamlMapping(
            final Map<YamlNode, YamlNode> entries,
            final String comment
    ) {

        YamlNode oofa = this;
        this.mappings.putAll(entries);
        this.comment = new Comment() {
            @Override
            public YamlNode yamlNode() {
                return oofa;
            }

            @Override
            public String value() {
                return "";
            }
        };
    }

    @Override
    public Set<YamlNode> keys() {
        final Set<YamlNode> keys = new LinkedHashSet<>();
        keys.addAll(this.mappings.keySet());
        return keys;
    }

    @Override
    public YamlNode value(final YamlNode key) {
        return this.mappings.get(key);
    }

    @Override
    public Comment comment() {
        return this.comment;
    }
}
