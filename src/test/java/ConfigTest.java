import org.dorkmaster.reportbot.config.Config;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {
    @Test
    public void happy() {
        Config config = new Config();
        Config.Value v= config.get("servers.1.raw");
        Assert.assertFalse(v.isNull());
        Assert.assertEquals(v.asString(), "rawField");
    }
}
