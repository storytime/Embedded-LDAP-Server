Embedded LDAP Server
=================

### Description


According to the forum thread:
[embedded LDAP Server NOT working with Apache DS 1.5 ](http://forum.spring.io/forum/spring-projects/security/81005-embedded-ldap-server-not-working-with-apache-ds1-5?p=501921#post501921)

There are several reasons, why spring not working with Apache DS 1.5.
One of them is that apacheds-all.jar is using certain version of the slf4j (pretty old version),
so if you've already used higher version of the slf4j there's going to be dependency conflicts.
So I try to solve this issue.


### Run:
    mvn clean test

### Maven result:
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running EditLDAPTest
    Start Server : ----------------------------------------------
    Stop Server : ----------------------------------------------
    Tests run: 3, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 16.122 sec

    Running SpringLDAPIntegrationTest
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 13.114 sec

    Running LDAPTest
    Start Server : ------1----------------------------------------
    Found entry : Entry
        dn[n]: cn=admin,ou=service,ou=home,dc=apache,dc=org
        objectClass: organizationalPerson
        objectClass: person
        objectClass: inetOrgPerson
        objectClass: top
        uid: admin@test.com
        userPassword: '0x71 0x77 0x65 0x72 0x74 0x79 '
        cn: admin
        sn: SN Admin
        mail: admin@test.com
        displayName: LDAP Admin

    Found entry : Entry
        dn[n]: cn=user1@test.com,ou=home,dc=apache,dc=org
        objectClass: organizationalPerson
        objectClass: person
        objectClass: inetOrgPerson
        objectClass: top
        uid: user1@test.com
        userPassword: '0x71 0x77 0x65 0x72 0x74 0x79 '
        cn: user1@test.com
        sn: SN User
        mail: user1@test.com
        displayName: LDAP Typical user

    Stop Server : ----------------------------------------------
    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.493 sec

    Results :

    Tests run: 7, Failures: 0, Errors: 0, Skipped: 1

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 1:01.332s
    [INFO] Finished at: Wed Apr 02 21:37:50 EEST 2014
    [INFO] Final Memory: 22M/213M
    [INFO] ------------------------------------------------------------------------


<a href="http://i.imgur.com/Dz2Xo1a.png"><img src="http://i.imgur.com/Dz2Xo1a.png" title="Hosted by imgur.com"/></a>







