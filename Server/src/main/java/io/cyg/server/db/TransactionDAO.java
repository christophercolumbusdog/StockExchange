package io.cyg.server.db;

import io.cyg.server.SubscriptionManager;
import io.cyg.server.core.SymbolSummary;
import io.cyg.server.core.Transaction;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Database Access Object for interfacing with the transaction database
 */
public class TransactionDAO extends AbstractDAO<Transaction>
{
    private SubscriptionManager manager;

    /**
     * Default constructor needs to initialize the AbstractDAO for functionality
     * @param sessionFactory Session factory from the application config for the DB
     */
    public TransactionDAO(SessionFactory sessionFactory, SubscriptionManager manager) {
        super(sessionFactory);
        this.manager = manager;
    }

    /**
     * Find a current transaction that exists in the database by looking up the id
     * @param id ID of the transaction to lookup
     * @return Transaction object with that ID
     */
    public Transaction findById(int id) {
        return currentSession().get(Transaction.class, id);
    }

    /**
     * Insert a new transaction into the database
     * @param transaction Transaction to insert
     * @return The result of inserting the transaction (which is the transaction itself)
     */
    public Transaction insert(Transaction transaction) {
        manager.addCompletedTransaction(transaction);
        return persist(transaction);
    }

    /**
     * Delete an existing transaction from the database
     * @param transaction Transaction to delete
     */
    public void delete(Transaction transaction) {
        manager.addCompletedTransaction(transaction);
        currentSession().delete(transaction);
    }

    /**
     * Update an existing transaction in the database
     * @param transaction Transaction to update with this transaction object
     */
    public void update(Transaction transaction) {
        manager.addCompletedTransaction(transaction);
        currentSession().saveOrUpdate(transaction);
    }

    /**
     * Finds all transactions in the database
     * @return A list of all transactions
     */
    public List<Transaction> findAll() {
        return (List<Transaction>) list(namedQuery("io.cyg.server.core.Transaction.findAll"));
    }

    /**
     * Get the current price for a given symbol, based on the transactions in the database
     * @param symbol Symbol in question to find the summary for
     * @return A summary of that symbol's price
     */
    public SymbolSummary getCurrentPriceForSymbol(String symbol) {
        List<Transaction> results;

        results = list(namedQuery("io.cyg.server.core.Transaction.findAll"));

        Optional<Transaction> askTransaction = results.stream()
                .filter(transaction -> transaction.getSymbol().equals(symbol))
                .filter(transaction -> transaction.getAction() == 'A')
                .sorted(Comparator.comparingDouble(Transaction::getPrice))
                .findFirst();

        Optional<Transaction> bidTransaction = results.stream()
                .filter(transaction -> transaction.getSymbol().equals(symbol))
                .filter(transaction -> transaction.getAction() == 'B')
                .sorted(Comparator.comparingDouble(Transaction::getPrice).reversed())
                .findFirst();

        if (askTransaction.isPresent() && bidTransaction.isPresent()) {
            Transaction ask = askTransaction.get();
            Transaction bid = bidTransaction.get();

            return new SymbolSummary(symbol, bid.getPrice(), ask.getPrice());
        } else {
            return new SymbolSummary(symbol, -1, -1);
        }
    }
}
