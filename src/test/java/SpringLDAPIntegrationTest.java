import com.github.storytime.LDAPEmbeddedServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: Bogdan
 * Date: 3/23/14
 * Time: 2:29 AM
 */

@Component
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
public class SpringLDAPIntegrationTest {

    @Autowired
    private LDAPEmbeddedServer ldapEmbeddedServer;

    @Before
    public void before() throws Exception {
        ldapEmbeddedServer.startServer();
    }

    @Test
    public void checkServer() {
        Assert.assertNotNull(ldapEmbeddedServer);
    }


    @After
    public void after() throws Exception {
        ldapEmbeddedServer.stopServer();
    }
}
