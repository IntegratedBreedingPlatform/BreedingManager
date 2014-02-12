package org.generationcp.browser.util.awhere;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AWhereUtil {

	private String BaseURL;
	private String loginURL;
	private String seasonURL;
	
	private String username;
	private String password;
	private String loginQueryString;
	
	private String aWhereCookie;
	
	private Integer lastResponseCode;
	private String lastResponseMessage;
	
	public AWhereUtil() {
		BaseURL = "https://data.awhere.com/api/weather";
		loginURL = BaseURL + "/Login/Index";
		seasonURL = BaseURL + "/season";
		
		username = "nzlabs@efficio.us.com";
		password = "9ZBKreXb";
    }
    

	public Boolean authenticate(String username, String password) throws Exception {
		this.username = username;
		this.password = password;
		return this.authenticate();
	}
	
	@SuppressWarnings("deprecation")
	public Boolean authenticate() throws Exception {
		loginQueryString = "uid="+URLEncoder.encode(username)+"&pwd="+URLEncoder.encode(password)+"&ReturnUrl=";
		
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        URL url = new URL(loginURL);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        
        HttpsURLConnection.setFollowRedirects(false);
        connection.setInstanceFollowRedirects(false);
        
        connection.setDoOutput(true);
        connection.setDoInput(true);
        
        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(loginQueryString);
        writer.flush();
        writer.close();
        
        aWhereCookie = connection.getHeaderField("Set-Cookie");
        
        connection.disconnect();

        lastResponseCode = connection.getResponseCode();
        lastResponseMessage = connection.getResponseMessage();
        
        if(lastResponseCode!=200 && lastResponseCode!=302){
        	return false;
        }
        
        return true;
    }

    
    @SuppressWarnings("deprecation")
	public String getSeason(Double latitude, Double longitude, Date planting, Date harvest) throws Exception {
    	
    	if(aWhereCookie.equals(null) || aWhereCookie.equals(""))
    		return null;
    	
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        SimpleDateFormat dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        String jsonParams = "[{\"lat\":"+latitude+", \"lon\":"+longitude+",\"planting\":\""+dmyFormat.format(planting)+"\", \"harvest\":\""+dmyFormat.format(harvest)+"\"}]";
        
        URL url = new URL(seasonURL+"?json_request="+URLEncoder.encode(jsonParams));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", aWhereCookie);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();
        
        connection.disconnect();
        
        lastResponseCode = connection.getResponseCode();
        lastResponseMessage = connection.getResponseMessage();
        
        return sb.toString();
        
    }
    
    
    
    private static class DefaultTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
    
    
    public int getLastResponseCode(){
    	return lastResponseCode;
    }
    
    public String getLastResponseMessage(){
    	return lastResponseMessage;
    }
}