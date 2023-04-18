package mattlib.util;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.superyuuki.yuukonfig.BadValueException;
import com.superyuuki.yuukonfig.manipulation.Contextual;
import com.superyuuki.yuukonfig.manipulation.Manipulation;
import com.superyuuki.yuukonfig.manipulation.Manipulator;
import com.superyuuki.yuukonfig.manipulation.Priority;
import com.superyuuki.yuukonfig.user.ConfComment;
import com.superyuuki.yuukonfig.user.ConfKey;
import mattlib.internals.model.ILogFeature;
import mattlib.internals.model.ITuneFeature;
import mattlib.model.Dirty;
import mattlib.model.annotation.core.Conf;
import mattlib.model.annotation.core.Log;
import mattlib.model.annotation.core.Tune;
import mattlib.model.hardware.IComponent;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Dirty
public class ComponentManipulator implements Manipulator {

    final Class<?> useClass;
    final Manipulation manipulation;



    final Supplier<Boolean> shouldUseTuning;
    final ITuneFeature tuneFeature;
    final ILogFeature logFeature;

    public ComponentManipulator(Class<?> useClass, Manipulation manipulation, Supplier<Boolean> shouldUseTuning, ITuneFeature tuneFeature, ILogFeature logFeature) {
        this.useClass = useClass;
        this.manipulation = manipulation;
        this.shouldUseTuning = shouldUseTuning;
        this.tuneFeature = tuneFeature;
        this.logFeature = logFeature;
    }

    @Override
    public int handles() {
        if (IComponent.class.isAssignableFrom(useClass)) return Priority.HANDLE;
        return Priority.DONT_HANDLE;
    }

    @Override
    public Object deserialize(YamlNode node, String exceptionalKey) throws BadValueException {

        Map<String, Supplier<Object>> configOrTuneMap = new HashMap<>();
        Map<String, Consumer<Object>> loggerMap = new HashMap<>();

        for (Method method : useClass.getMethods()) {
            if (method.getDeclaringClass() == Objects.class) continue;
            check(method);

            String key = getKey(method);

            Conf conf = method.getAnnotation(Conf.class);
            Tune tune = method.getAnnotation(Tune.class);

            if (conf != null || tune != null) { //Handle this as a config value
                YamlNode nullable = node.asMapping().value(key);

                //if (true) throw new IllegalStateException(node.toString());
                if (nullable == null) throw new BadValueException(
                        manipulation.configName(),
                        key,
                        "No YAML found, please write some in!"
                );

                Class<?> returnType = method.getReturnType();
                Object confObject = manipulation.deserialize(
                        nullable,
                        key,
                        returnType
                );

                Supplier<Object> objectSupplier;

                if (tune != null && shouldUseTuning.get()) { //it's a tune and not a conf AND we are in tuning mode (only set at restart)
                    objectSupplier = tuneFeature.generateTuner(null, confObject).orElseThrow(() -> {
                        throw new BadValueException(
                                manipulation.configName(),
                                exceptionalKey,
                                String.format(
                                        "Cannot set up a tuneable value that tunes a value with type: %s",
                                        confObject.getClass().getName()
                                )
                        );
                    });
                } else {
                    objectSupplier = () -> confObject;
                }

                configOrTuneMap.put(key, objectSupplier);
            } else { //It's a logger!

                Class<Object> type = (Class<Object>) method.getParameters()[0].getType();

                Consumer<Object> objectConsumer = logFeature.generateLogger(null, type).orElseThrow(() -> {
                    throw new BadValueException(
                            manipulation.configName(),
                            exceptionalKey,
                            String.format(
                                    "Cannot set up a loggable value that logs a value with type: %s",
                                    type.getName()
                            )
                    );
                });

                loggerMap.put(key, objectConsumer);


            }
        }


        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{ useClass },
                (invoked,method,parameter) -> {
                    //check loggers first

                    Consumer<Object> logger = loggerMap.get(getKey(method));

                    if (logger != null) {
                        //treat it as a logger TODO this isn't safe or something

                        logger.accept(parameter[0]); return null;
                    }

                    Supplier<Object> supplier = configOrTuneMap.get(getKey(method));

                    if (supplier != null) {
                        //treat it as a supplier TODO also not safe

                        return supplier.get();
                    }

                    throw new IllegalStateException("Config not initialized properly! Contact matt ASAP this is horrible and if you see this things are exploding");

                }

        );

    }


    @Override
    public YamlNode serializeObject(Object object, String[] comment) {
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();

        for (Method method : useClass.getMethods()) {
            if (method.getDeclaringClass() == Objects.class) continue;
            check(method);

            String key = getKey(method);
            Class<?> as = method.getReturnType();
            String[] comments = getComment(method);
            Object toSerialize = new CustomForwarder(method, object).invoke(); //get the return of the method


            YamlNode serialized = manipulation.serialize(
                    toSerialize,
                    as,
                    comments,
                    Contextual.present(
                            method.getGenericReturnType()
                    )
            );

            builder = builder.add(key, serialized);

        }


        return builder.build(Arrays.asList(comment));
    }

    void check(Method method) {

        int quantity = 0;
        Conf conf = method.getAnnotation(Conf.class);
        if (conf != null) quantity++;
        Log log = method.getAnnotation(Log.class);
        if (log != null) quantity++;
        Tune tune = method.getAnnotation(Tune.class);
        if (tune != null) quantity++;

        if (quantity > 1) {
            throw new BadValueException(
                    manipulation.configName(),
                    method.getName(),
                    "Too many annotations! \nPlease use only @log, @tune, or @conf on it"
            );
        }


        if (method.getAnnotations().length == 0) {
            throw new BadValueException(
                    manipulation.configName(),
                    method.getName(),
                    String.format("No annotation on method: %s for class: %s! \nPlease add either @log, @tune, or @conf on it", method.getName(), useClass.getName())
            );
        }

        if ((conf != null || tune != null) && method.getParameterCount() != 0) {
            throw new IllegalStateException(
                    String.format("The config interface method '%s' cannot have arguments, but it has %s arguments!", method.getName(), method.getParameterCount())
            );
        }

        if (log != null && method.getParameterCount() == 0) {
            throw new IllegalStateException(
                    String.format("The logging interface method '%s' must have arguments!", method.getName())
            );
        }


        if (log != null && method.getReturnType() != void.class) {
            throw new BadValueException(
                    manipulation.configName(),
                    method.getName(),
                    String.format("return type must be void, but return type is %s", method.getReturnType())
            );
        }
    }



    @Override
    public YamlNode serializeDefault(String[] comment) {


        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();

        for (Method method : useClass.getMethods()) {
            if (method.getDeclaringClass() == Objects.class) continue;
            check(method);
            if (method.getAnnotation(Log.class) != null) continue; //No need to serialize for log

            Class<?> returnType = method.getReturnType();
            String key = getKey(method);
            String[] comments = getComment(method);
            YamlNode serialized;

            //System.out.println("working on: " + key);

            serialized = manipulation.serializeDefault(
                    returnType,
                    comments,
                    Contextual.present(method.getGenericReturnType())
            );

            //System.out.println("finished: " + key + " : " + serialized);

            builder = builder.add(key, serialized);
        }

        return builder.build(Arrays.asList(comment));
    }

    String[] getComment(Method method) {
        if (method.isAnnotationPresent(ConfComment.class)) {
            return method.getAnnotation(ConfComment.class).value();
        }

        return new String[]{};
    }


    String getKey(Method method) {
        if (method.isAnnotationPresent(ConfKey.class)) {
            return method.getAnnotation(ConfKey.class).value();
        }

        return method.getName();
    }
}
