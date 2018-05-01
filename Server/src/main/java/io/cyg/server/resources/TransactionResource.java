package io.cyg.server.resources;

import io.cyg.server.core.SymbolSummary;
import io.cyg.server.core.Transaction;
import io.cyg.server.db.LockProvider;
import io.cyg.server.db.TransactionDAO;
import io.cyg.server.db.UserDAO;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Resource for the "transaction" endpoint
 */
@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource
{
    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private static Semaphore lock = LockProvider.getLock();

    /**
     * Initialize the resource with an instantiated DAO
     *
     * This dependency injection makes testing easier
     * @param dao Transaction DAO that has been initialized
     */
    public TransactionResource(TransactionDAO dao, UserDAO userDAO) {
        this.transactionDAO = dao;
        this.userDAO = userDAO;
    }

    /**
     * Endpoint allows "immediate" transactions that will either succeed or fail completely.
     * No partial trades will be executed, the result is all or nothing
     * @param transaction Transaction you wish to try
     * @return The result of attempting the immediate transaction. True means all shares have been traded.
     */
    @POST
    @Path("/immediate")
    @UnitOfWork(value = "hibernate.db_connector")
    public boolean immediateTransaction(@Valid Transaction transaction) {
        try {
            lock.acquire();

            char complementAction = transaction.getAction() == 'A' ? 'B' : 'A';
            boolean processed = false;
            List<Transaction> satisfiableTransactions = new ArrayList<>();
            Map<Integer, Double> fundTransfers = new HashMap<>();
            List<Transaction> list = transactionDAO.findAll();

            List<Transaction> matches = list.stream()
                    .filter(item -> item.getSymbol().equals(transaction.getSymbol()))
                    .filter(item -> item.getAction() == complementAction)
                    .sorted(transaction.getAction() == 'B' ?
                            Comparator.comparingDouble(Transaction::getPrice) :
                            Comparator.comparingDouble(Transaction::getPrice).reversed())
                    .collect(Collectors.toList());

            while (!processed) {
                if (transaction.getQuantity() == 0) break;

                if (matches.size() != 0) {
                    Transaction match = matches.remove(0);

                    if (transaction.getAction() == 'A') {
                        if (match.getPrice() >= transaction.getPrice()) {
                            makeVirtualTrades(transaction, satisfiableTransactions, match, fundTransfers);
                        } else {
                            processed = true;
                        }
                    } else {
                        if (match.getPrice() <= transaction.getPrice()) {
                            makeVirtualTrades(transaction, satisfiableTransactions, match, fundTransfers);
                        } else {
                            processed = true;
                        }
                    }
                } else {
                    processed = true;
                }
            }

            if (transaction.getQuantity() > 0) {
                return false;
            } else {
                satisfiableTransactions.forEach(transaction1 -> {
                    if (transaction1.getQuantity() == 0) {
                        transactionDAO.delete(transaction1);
                    } else {
                        transactionDAO.update(transaction1);
                    }
                });

                for (int id : fundTransfers.keySet()) {
                    userDAO.updateUserFunds(id, fundTransfers.get(id));
                }

                return true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.release();
        }
    }

    /**
     * Trade the transaction and match virtually, adjusting the quantities as needed and adding the remaining
     * transactions to the satisfiableTransactions list
     * @param transaction Transaction to be matched with match
     * @param satisfiableTransactions List of currently satisfied transactions
     * @param match The matching transaction to trade against
     */
    private void makeVirtualTrades(@Valid Transaction transaction, List<Transaction> satisfiableTransactions,
                                   Transaction match, Map<Integer, Double> fundTransfers) {

        int quantity;

        if (match.getQuantity() > transaction.getQuantity()) { //Transaction can be entirely fulfilled
            quantity = transaction.getQuantity();

            match.setQuantity(match.getQuantity() - transaction.getQuantity());
            transaction.setQuantity(0);
            satisfiableTransactions.add(match);
        } else { // Transaction cannot be fulfilled completely, but match can be deleted
            quantity = match.getQuantity();

            transaction.setQuantity(transaction.getQuantity() - match.getQuantity());
            match.setQuantity(0);
            satisfiableTransactions.add(match);
        }

        /*
         * Addresses issue of bidding on your own ask by flattening the funds
         */
        if (match.getAction() == 'A') {
            double startingMatchFunds = fundTransfers.getOrDefault(match.getOwnerID(), 0.0);
            fundTransfers.put(match.getOwnerID(), startingMatchFunds + (quantity * match.getPrice()));
            double startingTransactionFunds = fundTransfers.getOrDefault(match.getOwnerID(), 0.0);
            fundTransfers.put(transaction.getOwnerID(), startingTransactionFunds - (quantity * match.getPrice()));
        } else {
            double startingMatchFunds = fundTransfers.getOrDefault(match.getOwnerID(), 0.0);
            fundTransfers.put(match.getOwnerID(), startingMatchFunds - (quantity * transaction.getPrice()));
            double startingTransactionFunds = fundTransfers.getOrDefault(match.getOwnerID(), 0.0);
            fundTransfers.put(transaction.getOwnerID(), startingTransactionFunds + (quantity * transaction.getPrice()));
        }
    }

    /**
     * POST endpoint to add a transaction to the database, after first checking if it can make an immediate trade
     * @param transaction Transaction to add to the database
     * @return The transaction that was added to the database
     */
    @POST
    @UnitOfWork(value = "hibernate.db_connector")
    public Transaction addTransaction(@Valid Transaction transaction) {
        try {
            lock.acquire();

            char complementAction = transaction.getAction() == 'A' ? 'B' : 'A';
            boolean processed = false;
            while (!processed) {
                if (transaction.getQuantity() == 0) break;

                List<Transaction> list = transactionDAO.findAll();
                Optional<Transaction> optionalMatch = list.stream()
                        .filter(item -> item.getSymbol().equals(transaction.getSymbol()))
                        .filter(item -> item.getAction() == complementAction)
                        .sorted(transaction.getAction() == 'B' ?
                                Comparator.comparingDouble(Transaction::getPrice) :
                                Comparator.comparingDouble(Transaction::getPrice).reversed())
                        .findFirst();

                if (optionalMatch.isPresent()) {
                    Transaction match = optionalMatch.get();

                    if (transaction.getAction() == 'A') {
                        processed = !(match.getPrice() >= transaction.getPrice()) || makeTrade(transaction, match);
                    } else {
                        processed = !(match.getPrice() <= transaction.getPrice()) || makeTrade(transaction, match);
                    }
                } else {
                    processed = true;
                }
            }

            if (transaction.getQuantity() > 0) {
                transactionDAO.insert(transaction);
                return transaction;
            } else {
                return transaction;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }

    /**
     * Make a trade with the incoming trade against the match
     * @param incoming Trade that's incoming from a server request
     * @param match Trade that could fulfill the incoming trade from the stored transactions
     * @return True if the trade was completely satisfied, or false if there is still pending quantities
     */
    private boolean makeTrade(Transaction incoming, Transaction match) {
        System.out.println("SOLD!");

        boolean ret;
        int quantity;

        if (match.getQuantity() > incoming.getQuantity()) { //Transaction can be entirely fulfilled
            quantity = incoming.getQuantity();

            match.setQuantity(match.getQuantity() - incoming.getQuantity());
            incoming.setQuantity(0);
            transactionDAO.update(match);
            ret = true;
        } else { // Transaction cannot be fulfilled completely, but match can be deleted
            quantity = match.getQuantity();

            incoming.setQuantity(incoming.getQuantity() - match.getQuantity());
            transactionDAO.delete(match);
            ret = false;
        }

        if (match.getAction() == 'A') {
            userDAO.updateUserFunds(match.getOwnerID(), 0.0 + quantity * match.getPrice());
            userDAO.updateUserFunds(incoming.getOwnerID(), 0.0 - quantity * match.getPrice());
        } else {
            userDAO.updateUserFunds(match.getOwnerID(), 0.0 - quantity * incoming.getPrice());
            userDAO.updateUserFunds(incoming.getOwnerID(), 0.0 + quantity * incoming.getPrice());
        }

        return ret;
    }

    /**
     * GET endpoint to get all the transactions from the database
     * @return A list of transactions from the database
     */
    @GET
    @UnitOfWork(value = "hibernate.db_connector")
    public List<Transaction> getAllTransactions() {
        try {
            lock.acquire();

            List<Transaction> list = transactionDAO.findAll();

            list.forEach(item -> LOGGER.info(Double.toString(item.getPrice())));

            return list;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }


    /**
     * GET endpoint to get all the transactions from the database
     * @return A list of transactions from the database
     */
    @GET
    @Path("/{SYM}")
    @UnitOfWork(value = "hibernate.db_connector")
    public List<Transaction> getSymbolTransactions(@PathParam("SYM") String symbol) {
        try {
            lock.acquire();
            List<Transaction> list = transactionDAO.findAll();

            return list.stream().filter(transaction -> transaction.getSymbol().equalsIgnoreCase(symbol))
                    .collect(Collectors.toList());

        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }

    /**
     * GET endpoint for getting the summary of a symbol in the database
     * @param symbol Symbol to lookup in the transactions
     * @return A summary for that symbol
     */
    @GET
    @Path("/summary/{SYM}")
    @UnitOfWork(value = "hibernate.db_connector")
    public SymbolSummary getCurrentPrices(@PathParam("SYM") String symbol) {
        try {
            lock.acquire();
            return transactionDAO.getCurrentPriceForSymbol(symbol);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }

    @GET
    @Path("/symbols")
    @UnitOfWork(value = "hibernate.db_connector")
    public List<String> getAllSymbols() {
        try {
            lock.acquire();

            List<Transaction> list = transactionDAO.findAll();

            return list.stream()
                    .filter(distinctByKey(Transaction::getSymbol))
                    .map(Transaction::getSymbol)
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.release();
        }
    }

    // Stateful filter: https://stackoverflow.com/questions/23699371/java-8-distinct-by-property
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
