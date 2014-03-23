import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.util.Date;

/**
 * User: Bogdan
 * Date: 3/23/14
 * Time: 1:21 AM
 */
public class ITest {

    protected static LDAPEmbeddedServer embeddedServer;

    @BeforeClass
    public static void before() throws Exception {
        File tmpDir = new File(System.getProperty(Text.CONF_SYS_TMP) + Text.CONF_APP_TMP + new Date());
        tmpDir.mkdirs();
        embeddedServer = new LDAPEmbeddedServer(tmpDir);
        embeddedServer.startServer();
        System.out.println(Text.TEXT_STRT + Text.TEXT_SEP);
    }

    @AfterClass
    public static void after() throws Exception {
        embeddedServer.stopServer();
        System.out.println(Text.TEXT_SHT + Text.TEXT_SEP);
    }

}
