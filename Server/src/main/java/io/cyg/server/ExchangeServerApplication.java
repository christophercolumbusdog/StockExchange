package io.cyg.server;

import io.cyg.server.core.Transaction;
import io.cyg.server.core.User;
import io.cyg.server.db.TransactionDAO;
import io.cyg.server.db.UserDAO;
import io.cyg.server.resources.TransactionResource;
import io.cyg.server.resources.UserResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * Main server initialization for the exchange server application
 */
public class ExchangeServerApplication extends Application<ExchangeServerConfiguration>
{
    /**
     * Default main method for the exchange server
     * @param args Args from the commandline, none expected
     * @throws Exception If the server cannot be loaded for whatever reason
     */
    public static void main(String[] args) throws Exception {
        new ExchangeServerApplication().run(args);
    }

    /**
     * Hibernate bundle created for accessing the User database
     */
    private final HibernateBundle<ExchangeServerConfiguration> hibernateBundle =
            new HibernateBundle<ExchangeServerConfiguration>(User.class, Transaction.class)
            {
                @Override
                public DataSourceFactory getDataSourceFactory(ExchangeServerConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }

                @Override
                protected String name() {
                    return "hibernate.db_connector";
                }
            };

    /**
     * Get name of the application
     * @return Name of the application
     */
    @Override
    public String getName() {
        return "exchange-server";
    }

    /**
     * Initialize the Exchange Server.
     * @param bootstrap Bootstrap of the exchange server configuration
     */
    @Override
    public void initialize(Bootstrap<ExchangeServerConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<ExchangeServerConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ExchangeServerConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
    }

    /**
     * Run the Exchange server. Here is where all of the resources are registered so that
     * requests can be routed to the proper method.
     *
     * @param exchangeServerConfiguration Configuration for this application
     * @param environment Environment the server is launched in
     * @throws Exception If there are any errors in starting the application
     */
    public void run(ExchangeServerConfiguration exchangeServerConfiguration, Environment environment) throws Exception {
        final SubscriptionManager manager = new SubscriptionManager();
        final UserDAO userDAO = new UserDAO(hibernateBundle.getSessionFactory());
        final TransactionDAO transactionDAO =
                new TransactionDAO(hibernateBundle.getSessionFactory(), manager);

        environment.jersey().register(new UserResource(userDAO, manager));
        environment.jersey().register(new TransactionResource(transactionDAO, userDAO));
        environment.lifecycle().manage(manager);

        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
