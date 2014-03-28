package com.github.storytime;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.name.Dn;

import java.io.File;
import java.util.Date;

public class Main {

    public static void main(String[] args) {

        try {

            File tmpDir = new File(System.getProperty(Text.CONF_SYS_TMP) + Text.CONF_APP_TMP + new Date());
            tmpDir.mkdirs();

            LDAPEmbeddedServer ads = new LDAPEmbeddedServer(tmpDir);

            Entry result = ads.getService().getAdminSession().lookup(new Dn("cn=admin,ou=service,ou=home,dc=apache,dc=org"));
            System.out.println(Text.MSG_FOUND  + result);

            result = ads.getService().getAdminSession().lookup(new Dn("cn=user1@test.com,ou=home,dc=apache,dc=org"));
            System.out.println(Text.MSG_FOUND + result);
            ads.startServer();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
