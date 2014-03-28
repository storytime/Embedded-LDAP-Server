package com.github.storytime;

/**
 * User: Bogdan
 * Date: 3/11/14
 * Time: 10:45 PM
 */
public interface Text {

    /**
     * Apache DS Strings
     */
    String CONF_SYS_TMP = "java.io.tmpdir";
    String CONF_APP_TMP = "/ldap-server-tmp-dir";
    String CONF_SYS_PART = "system";
    String CONF_HOME_PART = "home";
    String CONF_SERVICE_PART = "service";

    /**
     * LDAP Strings
     */
    String CONF_OC = "objectClass";
    String CONF_OU = "ou";
    String CONF_UID = "uid";
    String CONF_TOP = "top";
    String CONF_DOMAIN="domain";
    String CONF_DC = "dc";
    String CONF_EX_OBJ="extensibleObject";
    String CONF_ORG_PER = "organizationalPerson";
    String CONF_PER="person";
    String CONF_INET_ORG_PER="inetOrgPerson";
    String CONF_MAIL="mail";
    String CONF_CN="cn";
    String CONF_DS_NAME="displayName";
    String CONF_SN="sn";
    String CONF_USER_PSWD="userPassword";

    String SRV_HOME_APACHE = "ou=service,ou=home,dc=apache,dc=org";
    String HOME_APACHE = "ou=home,dc=apache,dc=org";
    String SRV_HOME_APACHE_ADMIN = "cn=admin,ou=service,ou=home,dc=apache,dc=org";
    String SRV_HOME_APACHE_USER="cn=user1@test.com,ou=home,dc=apache,dc=org";

    /**
     * Messages
     */
    String MSG_FOUND = "Found entry : ";

    /**
     * Texts
     */
    String TEXT_APACHE = "Apache";
    String TEXT_SEP = " : ----------------------------------------------";
    String TEXT_STRT = "Start Server";
    String TEXT_SHT = "Stop Server";

}
