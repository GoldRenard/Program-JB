/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class NetworkUtils {

    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);

    public static String localIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        int p = ipAddress.indexOf("%");
                        if (p > 0) {
                            ipAddress = ipAddress.substring(0, p);
                        }
                        if (log.isTraceEnabled()) {
                            log.trace("localIPAddress: {}", ipAddress);
                        }
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("Could not detect IP Address", e);
        }
        return "127.0.0.1";
    }

    public static String responseContent(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(new URI(url));
        InputStream is = client.execute(request).getEntity().getContent();
        BufferedReader inb = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder("");
        String line;
        String NL = System.getProperty("line.separator");
        while ((line = inb.readLine()) != null) {
            sb.append(line).append(NL);
        }
        inb.close();
        return sb.toString();
    }

    public static String responseContentUri(URI uri) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost();
        request.setURI(uri);
        InputStream is = client.execute(request).getEntity().getContent();
        BufferedReader inb = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder("");
        String line;
        String NL = System.getProperty("line.separator");
        while ((line = inb.readLine()) != null) {
            sb.append(line).append(NL);
        }
        inb.close();
        return sb.toString();
    }

    public static String spec(String host, String botid, String custid, String input) {
        if (log.isDebugEnabled()) {
            log.debug("Network spec --> custId = {}", custid);
        }
        String spec = "";
        try {
            if ("0".equals(custid))      // get custid on first transaction with Pandorabots
                spec = String.format("%s?botid=%s&input=%s",
                        "http://" + host + "/pandora/talk-xml",
                        botid,
                        URLEncoder.encode(input, "UTF-8"));
            else spec =                 // re-use custid on each subsequent interaction
                    String.format("%s?botid=%s&custid=%s&input=%s",
                            "http://" + host + "/pandora/talk-xml",
                            botid,
                            custid,
                            URLEncoder.encode(input, "UTF-8"));
        } catch (Exception e) {
            log.error("spec failed", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Network spec result --> {}", spec);
        }
        return spec;
    }
}
