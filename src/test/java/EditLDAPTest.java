import org.junit.Assert;
import org.junit.Ignore;
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
    public void connect() throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");
        DirContext context = new InitialDirContext(env);
        Assert.assertNotNull(context);
    }

    @Test(expected = Exception.class)
    public void connectCommunicationException() throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndiz.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:00000");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "qwerty");
        DirContext context = new InitialDirContext(env);
    }

    @Test
    @Ignore
    public void test() throws NamingException {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system"); // admin DN
        env.put(Context.SECURITY_CREDENTIALS, "qwerty");// passwd

        // new DN
        String DNrecord = "uid=testUser,ou=system";
        Attribute cn = new BasicAttribute("cn", "testUser");
        Attribute sn = new BasicAttribute("sn", "testUser");
        Attribute email = new BasicAttribute("mail", "test_user@example.com");
        Attribute phone = new BasicAttribute("phone number", "123 123 123");

        Attribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("top");
        objectClass.add("person");
        objectClass.add("organizationalPerson");
        objectClass.add("inetOrgPerson");

        DirContext ctx = new InitialDirContext(env);
        Assert.assertNotNull(ctx);

        BasicAttributes record = new BasicAttributes();
        record.put(cn);
        record.put(sn);
        record.put(email);
        record.put(phone);
        record.put(objectClass);

        //add record
        ctx.createSubcontext(DNrecord, record);

    }

}
