package io.cyg.trading.client.bots;

import io.cyg.trading.client.core.User;

public interface Bot extends Runnable
{
    public void stop();

    public User getUser();
}
