package mattlib.util;


import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.superyuuki.yuukonfig.BadValueException;
import com.superyuuki.yuukonfig.manipulation.Contextual;
import com.superyuuki.yuukonfig.manipulation.Manipulation;
import com.superyuuki.yuukonfig.manipulation.Manipulator;
import com.superyuuki.yuukonfig.manipulation.Priority;
import mattlib.ProcessPath;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This should return Map < ProcessPath , T > where T is a typed config of ProcessPath P
 */
public class NeoComponentManipulator implements Manipulator {



    final Manipulation manipulation;
    final Class<?> toCheck;
    final Contextual<Type> typeContextual;
    final Map<ProcessPath, Class<?>> loadAs; //shitty hacl

    public NeoComponentManipulator(Manipulation manipulation, Class<?> toCheck, Contextual<Type> typeContextual, Map<ProcessPath, Class<?>> loadAs) {
        this.manipulation = manipulation;
        this.toCheck = toCheck;
        this.typeContextual = typeContextual;
        this.loadAs = loadAs;
    }

    @Override
    public int handles() {
        if (toCheck.isAssignableFrom(TypeMap.class)) return Priority.HANDLE;

        return Priority.DONT_HANDLE;
    }

    @Override
    public Object deserialize(YamlNode node, String exceptionalKey) throws BadValueException {

        Map<ProcessPath, Object> toReturnMap = new HashMap<>();
        YamlMapping root = node.asMapping();

        for (Map.Entry<ProcessPath, Class<?>> subConfig : loadAs.entrySet()) {

            YamlNode drillNode = drillToNode(root, subConfig.getKey());
            Object configObject = manipulation.deserialize(drillNode, subConfig.getKey().getTail(), subConfig.getValue());
            toReturnMap.put(subConfig.getKey(), configObject);

        }

        return new TypeMap(toReturnMap);
    }

    YamlNode drillToNode(YamlMapping root, ProcessPath path) throws BadValueException {

        if (path.length() == 0) {
            return root;
        }

        String[] internalArray = path.asArray();
        int useIndex = 0;

        YamlNode closestToTheTruth = null;

        while (useIndex < internalArray.length) {
            if (closestToTheTruth == null) {
                closestToTheTruth = root.yamlMapping(internalArray[useIndex]);
            } else {
                closestToTheTruth = closestToTheTruth.asMapping().yamlMapping(internalArray[useIndex]);
            }

            if (closestToTheTruth == null) throw new BadValueException(
                    manipulation.configName(),
                    internalArray[useIndex],
                    "No YAML found, please write some in!"
            );

            useIndex++;
        }

        return closestToTheTruth;

    }


    @Override
    public YamlNode serializeObject(Object object, String[] comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public YamlNode serializeDefault(String[] comment) {
        YamlMappingBuilder root = new CombinedYamlMappingBuilder();
        for (Map.Entry<ProcessPath, Class<?>> entry : loadAs.entrySet()) {

            YamlNode serializedNode = manipulation.serializeDefault(entry.getValue(), new String[0] );

            System.out.println("e" + serializedNode.toString());

            String[] internalArray = entry.getKey().asArray();
            YamlNode toAddToRoot = serializedNode;


            for (int i = internalArray.length - 1; i >= 0; i--) {
                if (i == 0) {
                    System.out.println("i" + internalArray.length);

                    root = root.add(
                            internalArray[i],
                            toAddToRoot
                    );

                    System.out.println(root.build());
                } else {
                    System.out.println("OOO");
                    toAddToRoot = new CombinedYamlMappingBuilder()
                            .add(
                                    internalArray[i],
                                    toAddToRoot
                            )
                            .build();
                }
            }
        }


        return root.build();
    }
}
