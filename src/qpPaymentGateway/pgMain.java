package qpPaymentGateway;

import java.math.BigDecimal;
import java.net.*;
import com.braintreegateway.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class pgMain
{

    public static void main(String[] args) throws IOException
    {
        int portNumber = 4001;

        while(true)
            {
            try (ServerSocket serverSocket = new ServerSocket(portNumber))
                {
                while(true)
                    {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader incoming = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BraintreeGateway gateway = new BraintreeGateway(Environment.SANDBOX,"4jjdnypspttw92qy","q9ksry76n4tpk7zv","d720c12a7d30e2703e4917ec4c3329a1");
                        {
                        if(clientSocket.isConnected())

                            {
                            String testInput = incoming.readLine();
                            if (testInput.equals("token"))
                                {
                                System.out.println("incoming was: " + testInput);
                                out.println(gateway.clientToken().generate());
                                }
                            if (testInput.contains("nonce"))
                                {
                                System.out.println("incoming was: " + testInput);
                                out.println(testInput);
                                JSONObject input = new JSONObject(testInput);
                                BigDecimal total = new BigDecimal(input.getInt("total")).movePointLeft(2);
                                String nonceFromTheClient = input.getString("nonce");

                                TransactionRequest request = new TransactionRequest()
                                        .amount(total)
                                        .paymentMethodNonce(nonceFromTheClient)
                                        .options()
                                        .submitForSettlement(true)
                                        .done();

                                Result<Transaction> result = gateway.transaction().sale(request);

                                if (result.isSuccess())
                                {
                                Transaction transaction = result.getTarget();
                                System.out.println("Success!: " + "TransID: " + transaction.getId() + ", Total: " + total);
                                out.println("Success!: " + transaction.getId());
                                }
                                else if (result.getTransaction() != null)
                                {
                                Transaction transaction = result.getTransaction();
                                System.out.println("Error processing transaction:");
                                System.out.println("  Status: " + transaction.getStatus());
                                System.out.println("  Code: " + transaction.getProcessorResponseCode());
                                System.out.println("  Text: " + transaction.getProcessorResponseText());
                                out.println("Error processing transaction:" + "  Status: " + transaction.getStatus() +
                                        "  Code: " + transaction.getProcessorResponseCode() + "  Text: " + transaction.getProcessorResponseText());
                                }
                                else
                                    {
                                    for (ValidationError error : result.getErrors().getAllDeepValidationErrors())
                                        {
                                        System.out.println("Attribute: " + error.getAttribute());
                                        System.out.println("  Code: " + error.getCode());
                                        System.out.println("  Message: " + error.getMessage());
                                        out.println("Error: " + error.getCode() + " Message: " + error.getMessage());
                                        }
                                    if (incoming.readLine() == null)
                                        {
                                        out.println("Nothing received");
                                        out.flush();
                                        }
                                    }

                                serverSocket.close();
                                }
                            }
                        }
                    }
                }

                catch (IOException e)
                    {
                    System.out.println(e.getMessage());
                    } catch (JSONException e)
                    {
                    e.printStackTrace();
                    }
            }
    }
}