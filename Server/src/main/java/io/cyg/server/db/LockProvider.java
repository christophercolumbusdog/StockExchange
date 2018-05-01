package io.cyg.server.db;

import java.util.concurrent.Semaphore;

public class LockProvider
{
    private static Semaphore lock = new Semaphore(1);

    public static Semaphore getLock() {
        return lock;
    }
}
