import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

public class LdapProgram {

    public static void main(String[] args) {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL,"uid=admin,ou=system"); // specify the username
        env.put(Context.SECURITY_CREDENTIALS,"secret");// specify the password
        // TODO code application logic here

        // entry's DN
        String entryDN = "uid=user1,ou=system";

        // entry's attributes

        Attribute cn = new BasicAttribute("cn", "Test User2");
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
            System.err.println("AddUser: error adding entry." + e);
        }
    }
}