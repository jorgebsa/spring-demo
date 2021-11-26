package com.github.jorgebsa.spring.demo.util;

import org.testcontainers.containers.MongoDBContainer;

public class ExtendedMongoDBContainer extends MongoDBContainer {
    public static final String DEFAULT_DOCKER_IMAGE = "mongo:5.0.3";

    private static ExtendedMongoDBContainer instance;
    private static final Object MUTEX = new Object();

    public static ExtendedMongoDBContainer getInstance() {
        synchronized(MUTEX) {
            if (instance == null) {
                instance = new ExtendedMongoDBContainer(DEFAULT_DOCKER_IMAGE);
            }
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
