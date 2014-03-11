import org.apache.directory.api.ldap.model.entry.Entry;
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
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition(String partitionId, String partitionDn) throws Exception {
        // Create a new partition with the given partition id 
        JdbmPartition partition = new JdbmPartition(service.getSchemaManager());
        partition.setId(partitionId);
        partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
        partition.setSuffixDn(new Dn(partitionDn));
        service.addPartition(partition);

        return partition;
    }


    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs     The list of attributes to index
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
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception {
        InstanceLayout instanceLayout = service.getInstanceLayout();

        File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

        // Extract the schema on disk (a brand new one) and load the registries
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
     *
     * @param workDir the directory to be used for storing the data
     * @throws Exception if there were some problems while initializing the system
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
        Partition infopulsePartition = addPartition(Text.CONF_HOME_PART, "ou=home,dc=apache,dc=org");
        Partition servicePartition = addPartition(Text.CONF_SERVICE_PART, "ou=service,ou=home,dc=apache,dc=org");

        // Index some attributes on the apache partition
        addIndex(infopulsePartition, Text.CONF_OC, Text.CONF_OU, Text.CONF_UID);
        addIndex(servicePartition, Text.CONF_OC, Text.CONF_OU, Text.CONF_UID);

        // And start the service
        service.startup();


        if (!service.getAdminSession().exists(infopulsePartition.getSuffixDn())) {
            Dn dnApache = new Dn("ou=home,dc=apache,dc=org");
            Entry entryApache = service.newEntry(dnApache);
            entryApache.add("objectClass", "top", "domain", "extensibleObject");
            entryApache.add("dc", "Apache");
            service.getAdminSession().add(entryApache);

            Dn user = new Dn("cn=user1@test.com,ou=home,dc=apache,dc=org");
            Entry entryUser = service.newEntry(user);
            entryUser.add("objectClass", "top", "organizationalPerson", "person", "inetOrgPerson");
            entryUser.add("mail", "user1@test.com");
            entryUser.add("uid", "user1@test.com");
            entryUser.add("cn", "user1@test.com");
            entryUser.add("displayName", "Directory Typical Super User");
            entryUser.add("sn", "Typical User");
            entryUser.add("userPassword", "qwerty");
            service.getAdminSession().add(entryUser);
        }

        if (!service.getAdminSession().exists(servicePartition.getSuffixDn())) {
            Dn dnApache = new Dn("ou=service,ou=home,dc=apache,dc=org");
            Entry entryApache = service.newEntry(dnApache);
            entryApache.add("objectClass", "top", "domain", "extensibleObject");
            entryApache.add("dc", "Apache");
            service.getAdminSession().add(entryApache);

            Dn admin = new Dn("cn=admin,ou=service,ou=home,dc=apache,dc=org");
            Entry entryAdmin = service.newEntry(admin);
            entryAdmin.add("objectClass", "top", "organizationalPerson", "person", "inetOrgPerson");
            entryAdmin.add("mail", "admin@test.com");
            entryAdmin.add("uid", "admin@test.com");
            entryAdmin.add("cn", "admin@test.com");
            entryAdmin.add("displayName", "Directory Server Super User");
            entryAdmin.add("sn", "Admin Muster Firma");
            entryAdmin.add("userPassword", "qwerty");

            service.getAdminSession().add(entryAdmin);
        }
    }


    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception If something went wrong
     */
    public LDAPEmbeddedServer(File workDir) throws Exception {
        initDirectoryService(workDir);
    }


    /**
     * starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer() throws Exception {
        server = new LdapServer();
        server.setTransports(new TcpTransport(SERVER_PORT));
        server.setDirectoryService(service);

        server.start();
    }
}