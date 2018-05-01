package io.cyg.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Any needed configuration for the exchange server
 */
public class ExchangeServerConfiguration extends Configuration
{
    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory userDatabase = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory() {
        return userDatabase;
    }

}
