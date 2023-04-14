import com.superyuuki.yuukonfig.user.Section;
import mattlib.MattLog;
import mattlib.ProcessPath;
import mattlib.model.annotation.core.Conf;
import mattlib.model.hardware.IComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class NeoComponentManipulatorTest {

    interface TestComponent extends Section {

        @Conf
        int someValue();
    }

    @Test
    public void makeThatShitWork() {
        TestComponent comp = MattLog.instance().loadTheTypeMap(
                Map.of(
                        ProcessPath.of("a","b"), TestComponent.class,
                        ProcessPath.of("a"), TestComponent.class
                )
        ).requestForPath(ProcessPath.of("a"));



        Assertions.assertEquals(0, comp.someValue());



    }


}
