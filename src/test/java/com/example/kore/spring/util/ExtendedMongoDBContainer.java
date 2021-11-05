package com.example.kore.spring.util;

import org.testcontainers.containers.MongoDBContainer;

public class ExtendedMongoDBContainer extends MongoDBContainer {
    public static final String DEFAULT_DOCKER_IMAGE = "mongo:4.0.10";

    private static ExtendedMongoDBContainer instance;

    public static ExtendedMongoDBContainer getInstance() {
        if (instance == null) {
            instance = new ExtendedMongoDBContainer(DEFAULT_DOCKER_IMAGE);
        }
        return instance;
    }

    public ExtendedMongoDBContainer(final String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("MONGODB_URL", instance.getReplicaSetUrl());
    }

    @Override
    public void stop() {

    }

}
