import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: Bogdan
 * Date: 3/22/14
 * Time: 8:51 PM
 */
public class LDAPTest extends ITest {

    @Test
    public void searchDnInTree() throws Exception {
        Entry result = embeddedServer.getService().getAdminSession().lookup(new Dn("cn=admin,ou=service,ou=home,dc=apache,dc=org"));
        Assert.assertEquals("cn: admin\n", result.get(Text.CONF_CN).toString());

        result = embeddedServer.getService().getAdminSession().lookup(new Dn("cn=user1@test.com,ou=home,dc=apache,dc=org"));
        Assert.assertEquals("cn: user1@test.com\n", result.get(Text.CONF_CN).toString());
    }

    @Test
    public void checkServer() {
        Assert.assertNotNull(embeddedServer);
    }

    @Test
    public void checkRecord() throws LdapException {
        Entry result = embeddedServer.getService().getAdminSession().lookup(new Dn("cn=admin,ou=service,ou=home,dc=apache,dc=org"));
        System.out.println(Text.MSG_FOUND + result);
        Assert.assertNotNull(result);

        result = embeddedServer.getService().getAdminSession().lookup(new Dn("cn=user1@test.com,ou=home,dc=apache,dc=org"));
        System.out.println(Text.MSG_FOUND + result);
        Assert.assertNotNull(result);
    }

}
