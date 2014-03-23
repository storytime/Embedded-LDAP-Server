import org.junit.Test;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

/**
 * User: Bogdan
 * Date: 3/23/14
 * Time: 1:18 AM
 */
public class EditLDAPTest extends ITest {

    @Test
    public void test(){
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL,"uid=admin,ou=system"); // specify the username
        env.put(Context.SECURITY_CREDENTIALS,"secret");// specify the password

        // entry's DN
        String entryDN = "uid=user222221,ou=system";

        // entry's attributes

        Attribute cn = new BasicAttribute("cn", "Test User211");
        Attribute sn = new BasicAttribute("sn", "Test2");
        Attribute mail = new BasicAttribute("mail", "newuser@foo.com");
        Attribute phone = new BasicAttribute("telephoneNumber", "+1 222 3334444");
        Attribute oc = new BasicAttribute("objectClass");
        oc.add("top");
        oc.add("person");
        oc.add("organizationalPerson");
        oc.add("inetOrgPerson");
        DirContext ctx = null;

        try {
            // get a handle to an Initial DirContext
            ctx = new InitialDirContext(env);

            // build the entry
            BasicAttributes entry = new BasicAttributes();
            entry.put(cn);
            entry.put(sn);
            entry.put(mail);
            entry.put(phone);

            entry.put(oc);

            // Add the entry

            ctx.createSubcontext(entryDN, entry);
            //          System.out.println( "AddUser: added entry " + entryDN + ".");

        } catch (NamingException e) {
            e.printStackTrace();
            System.err.println("AddUser: error adding entry." + e);
        }
    }

}
