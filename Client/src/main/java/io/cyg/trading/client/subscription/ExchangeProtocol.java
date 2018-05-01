package io.cyg.trading.client.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cyg.trading.client.core.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class allows for different ways to interact with server updates
 */
public class ExchangeProtocol
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Static method that converts an incoming InputStream to a Transaction object
     * @param inputStream Input stream from the server
     * @return A transaction created from the InputStream
     * @throws IOException If there is an issue reading or converting the value
     */
    public static Transaction processTransactionNotification(InputStream inputStream) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder message = new StringBuilder();
        String inputLine;

        while((inputLine = inputReader.readLine()) != null) {
            message.append(inputLine);
        }

        return MAPPER.readValue(message.toString(), Transaction.class);
    }
}
