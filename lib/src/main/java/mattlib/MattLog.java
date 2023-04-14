package mattlib;

import com.superyuuki.yuukonfig.YuuKonfig;
import com.superyuuki.yuukonfig.YuuKonfigAPI;
import edu.wpi.first.wpilibj.Filesystem;
import mattlib.model.hardware.IComponent;
import mattlib.util.ComponentManipulator;
import mattlib.internals.impl.NetworkLogFeature;
import mattlib.internals.impl.NetworkTuneFeature;
import mattlib.util.NeoComponentManipulator;
import mattlib.util.TypeMap;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MattLog {

    static MattLog INSTANCE = null;

    Path getFilePathFromProcessPath(ProcessPath path) {
        Path deployPath = Filesystem.getDeployDirectory().toPath();
        String[] subPath = path.asArray();

        for (int i = 0; i < subPath.length; i++) {

            if (i == subPath.length - 1) { //if we're on the last one
                deployPath = deployPath.resolve(subPath[i] + ".yml"); //TODO make this less garbage
            } else { //just add normally
                deployPath = deployPath.resolve(subPath[i]);

            }
        }

        //Check if it exists..

        return deployPath;
    }



    @Deprecated //Use the big one instead
    public <T> T makeComponent(ProcessPath absoluteProcessPath, Class<T> typeToRead) {

        YuuKonfigAPI api = YuuKonfig.instance().register(
                (manipulation,clazz,c) -> new ComponentManipulator(
                        clazz,
                        manipulation,
                        () -> false,
                        new NetworkTuneFeature(),
                        new NetworkLogFeature()
                )
        );

        return api.loader(
                typeToRead,
                getFilePathFromProcessPath(absoluteProcessPath)
        ).load();

    }

    public TypeMap loadTheTypeMap(Map<ProcessPath, Class<?>> whatIsWhat) {
        return YuuKonfig.instance()
                .register(
                        (manipulation,clazz,c) -> new ComponentManipulator(
                                clazz,
                                manipulation,
                                () -> false,
                                new NetworkTuneFeature(),
                                new NetworkLogFeature()
                        )
                )
                .register(
                        (manipulation, useClass, useType) -> new NeoComponentManipulator(manipulation, useClass, useType, whatIsWhat)
                )
                .loader(
                        TypeMap.class,
                        Paths.get("").toAbsolutePath().resolve("penis.yml")
                        //Filesystem.getDeployDirectory().toPath().resolve("config.yml")
                )
                .load();
    }

    public static MattLog instance() {
        if (INSTANCE == null) {
            INSTANCE = new MattLog();
        }

        return INSTANCE;
    }

}
