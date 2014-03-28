package com.github.storytime;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schemaextractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaextractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaloader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LDAPEmbeddedServer {

    private DirectoryService service;
    private LdapServer server;
    protected final static int SERVER_PORT = 10389;

    public DirectoryService getService() {
        return service;
    }


    /**
     * Add a new partition to the server
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition(String partitionId, String partitionDn) throws Exception {
        JdbmPartition partition = new JdbmPartition(service.getSchemaManager());
        partition.setId(partitionId);
        partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
        partition.setSuffixDn(new Dn(partitionDn));
        service.addPartition(partition);

        return partition;
    }


    /**
     * Add a new set of index on the given attributes
     */
    private void addIndex(Partition partition, String... attrs) {
        // Index some attributes on the apache partition
        Set indexedAttributes = new HashSet();

        for (String attribute : attrs) {
            indexedAttributes.add(new JdbmIndex<String, Entry>(attribute, false));
        }

        ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
    }


    /**
     * initialize the schema manager and add the schema partition to diectory service
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception {

        InstanceLayout instanceLayout = service.getInstanceLayout();
        File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

        // Extract the schema on disk
        if (schemaPartitionDirectory.exists()) {
            System.out.println("schema partition already exists, skipping schema extraction");
        } else {
            SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
            extractor.extractOrCopy();
        }

        SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
        SchemaManager schemaManager = new DefaultSchemaManager(loader);

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix Dn
        schemaManager.loadAllEnabled();
        List<Throwable> errors = schemaManager.getErrors();

        if (errors.size() != 0) {
            throw new Exception(I18n.err(I18n.ERR_317, Exceptions.printErrors(errors)));
        }

        service.setSchemaManager(schemaManager);

        // Init the LdifPartition with schema
        LdifPartition schemaLdifPartition = new LdifPartition(schemaManager);
        schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

        // The schema partition
        SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
        schemaPartition.setWrappedPartition(schemaLdifPartition);
        service.setSchemaPartition(schemaPartition);
    }


    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     */
    private void initDirectoryService(File workDir) throws Exception {

        // Initialize the LDAP service
        service = new DefaultDirectoryService();
        service.setInstanceLayout(new InstanceLayout(workDir));

        CacheService cacheService = new CacheService();
        cacheService.initialize(service.getInstanceLayout());
        service.setCacheService(cacheService);

        // first load the schema
        initSchemaPartition();

        // then the system partition
        // this is a MANDATORY partition
        // DO NOT add this via addPartition() method, trunk code complains about duplicate partition
        // while initializing 
        JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager());
        systemPartition.setId(Text.CONF_SYS_PART);
        systemPartition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), systemPartition.getId()).toURI());
        systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
        systemPartition.setSchemaManager(service.getSchemaManager());

        // mandatory to call this method to set the system partition
        // Note: this system partition might be removed from trunk
        service.setSystemPartition(systemPartition);

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled(false);
        service.setDenormalizeOpAttrsEnabled(true);

        // Now we can create as many partitions as we need
        // Create some new partitions named 'foo', 'bar' and 'apache'.
        Partition mainPartition = addPartition(Text.CONF_HOME_PART, Text.HOME_APACHE);
        Partition servicePartition = addPartition(Text.CONF_SERVICE_PART, Text.SRV_HOME_APACHE);

        // Index some attributes on the apache partition
        addIndex(mainPartition, Text.CONF_OC, Text.CONF_OU, Text.CONF_UID);
        addIndex(servicePartition, Text.CONF_OC, Text.CONF_OU, Text.CONF_UID);

        // And start the service
        service.startup();

        if (!service.getAdminSession().exists(mainPartition.getSuffixDn())) {
            Entry homeApache = addSimpleEntry(Text.HOME_APACHE);
            service.getAdminSession().add(homeApache);
            Entry entryAdmin = addNewUser(Text.SRV_HOME_APACHE_USER, "user1@test.com", "user1@test.com",
                    "user1@test.com", "LDAP Typical user", "SN User", "qwerty");
            service.getAdminSession().add(entryAdmin);
        }

        if (!service.getAdminSession().exists(servicePartition.getSuffixDn())) {
            Entry entryApache = addSimpleEntry(Text.SRV_HOME_APACHE);
            service.getAdminSession().add(entryApache);
            Entry entryAdmin = addNewUser(Text.SRV_HOME_APACHE_ADMIN, "admin@test.com", "admin@test.com",
                    "admin", "LDAP Admin", "SN Admin", "qwerty");
            service.getAdminSession().add(entryAdmin);
        }
    }

    private Entry addNewUser(String dn, String email, String uid, String cn, String dsName, String sn, String password)
            throws LdapException {
        Dn admin = new Dn(dn);
        Entry entryAdmin = service.newEntry(admin);
        entryAdmin.add(Text.CONF_OC, Text.CONF_TOP, Text.CONF_ORG_PER, Text.CONF_PER, Text.CONF_INET_ORG_PER);
        entryAdmin.add(Text.CONF_MAIL, email);
        entryAdmin.add(Text.CONF_UID, uid);
        entryAdmin.add(Text.CONF_CN, cn);
        entryAdmin.add(Text.CONF_DS_NAME, dsName);
        entryAdmin.add(Text.CONF_SN, sn);
        if (password != null)
            entryAdmin.add(Text.CONF_USER_PSWD, password);
        return entryAdmin;
    }

    private Entry addSimpleEntry(String dn) throws LdapException {
        Dn dnApache = new Dn(dn);
        Entry entryApache = service.newEntry(dnApache);
        entryApache.add(Text.CONF_OC, Text.CONF_TOP, Text.CONF_DOMAIN, Text.CONF_EX_OBJ);
        entryApache.add(Text.CONF_DC, Text.TEXT_APACHE);
        return entryApache;
    }

    /**
     * Initializes the directory service.
     */
    public LDAPEmbeddedServer(File workDir) throws Exception {
        initDirectoryService(workDir);
    }

    /**
     * starts the LdapServer
     */
    public void startServer() throws Exception {
        server = new LdapServer();
        server.setTransports(new TcpTransport(SERVER_PORT));
        server.setDirectoryService(service);
        server.start();
    }

    /**
     * stop the LdapServer
     */
    public void stopServer() throws Exception {
        server.stop();
    }
}