package fenego.app.intershop;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IntershopClient
{
    @Value("${intershop.base-url}")
    private String baseUrl;

    @Value("${intershop.username}")
    private String username;

    @Value("${intershop.password}")
    private String password;

    @Value("${intershop.organization:Operations}")
    private String organization;

    public boolean login()
    {
        try
        {
            HttpClient client = createUnsafeHttpClient();

            String credentials = username + ":" + password;
            String basicAuth = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/login"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Accept", "application/json")
                    .header("UserOrganization", organization)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() >= 200 && response.statusCode() < 300;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Intershop login failed", e);
        }
    }

    private HttpClient createUnsafeHttpClient() throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager()
                {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                    {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                    {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("");

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build();
    }
}