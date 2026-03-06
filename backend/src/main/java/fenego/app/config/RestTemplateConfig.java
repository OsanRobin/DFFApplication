package fenego.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig
{
    @Bean
    public RestTemplate restTemplate() throws Exception
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
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        return new RestTemplate(requestFactory);
    }
}