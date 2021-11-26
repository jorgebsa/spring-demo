package com.github.jorgebsa.spring.demo.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.github.jorgebsa.spring.demo.util.UserData.ADMIN;
import static com.github.jorgebsa.spring.demo.util.UserData.SOME_USER;

public class ExtendedKeycloakContainer extends KeycloakContainer {

    private static final Logger log = LoggerFactory.getLogger(ExtendedKeycloakContainer.class);
    public static final String DEFAULT_DOCKER_IMAGE = "quay.io/keycloak/keycloak:15.0.2";
    public static final String REALM = "tests";
    public static final String CLIENT_ID = "spring-demo";
    public static final String CLIENT_SECRET = "the-secret";
    private static final Object MUTEX = new Object();

    private static ExtendedKeycloakContainer container;

    public static ExtendedKeycloakContainer getInstance() {
        synchronized(MUTEX) {
            if (container == null) {
                container = new ExtendedKeycloakContainer();
            }
        }
        return container;
    }

    public ExtendedKeycloakContainer() {
        super(DEFAULT_DOCKER_IMAGE);
    }

    @Override
    public void start() {
        super.start();
        var authServerUrl = getInstance().getAuthServerUrl();
        System.setProperty("KEYCLOAK_URL", authServerUrl);
        configureTestsRealm();
    }

    private static boolean configured;

    public void configureTestsRealm() {
        synchronized(MUTEX) {
            if (!configured) {
                log.info("Connecting to Keycloak with admin credentials");
                try (var keycloak = getKeycloakAdminClient()) {
                    var client = buildClient();
                    var adminUser = buildUser(ADMIN);
                    var someUser = buildUser(SOME_USER);
                    var realm = buildRealm(List.of(client), List.of(adminUser, someUser));
                    keycloak.realms().create(realm);
                    log.info("Saved realm [{}]", realm.getRealm());
                    configured = true;
                }
            }
        }
    }

    private Keycloak getKeycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(getInstance().getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(getInstance().getAdminUsername())
                .password(getInstance().getAdminPassword())
                .build();
    }

    private RealmRepresentation buildRealm(List<ClientRepresentation> clients, List<UserRepresentation> users) {
        var realm = new RealmRepresentation();
        realm.setRealm(REALM);
        realm.setEnabled(true);
        realm.setRegistrationAllowed(true);
        realm.setAccessTokenLifespan(600000);
        realm.setClients(clients);
        realm.setUsers(users);
        log.info("Configured realm [{}] with [{}] clients and [{}] users", realm.getRealm(), realm.getClients().size(), realm.getUsers().size());
        return realm;
    }

    private ClientRepresentation buildClient() {
        var client = new ClientRepresentation();
        client.setClientId(CLIENT_ID);
        client.setSecret(CLIENT_SECRET);
        client.setDirectAccessGrantsEnabled(true);
        log.info("Configured client [{}] with secret [{}]", client.getClientId(), client.getSecret());
        return client;
    }

    private UserRepresentation buildUser(UserData userData) {
        var credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(userData.password());

        var user = new UserRepresentation();
        user.setUsername(userData.username());
        user.setEnabled(true);
        user.setCredentials(List.of(credentials));
        user.setClientRoles(Map.of(CLIENT_ID, userData.roles()));
        log.info("Configured user [{}] with roles {}", user.getUsername(), user.getClientRoles().get(CLIENT_ID));
        return user;
    }

    @Override
    public void stop() {

    }

}
