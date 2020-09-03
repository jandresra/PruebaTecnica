package mx.rodattec.rodapps.pruebatecnica.library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    public String[] getHttpRequest(String[] strUrl) { /*m.getHttpRequest*/
        HttpURLConnection http = null;
        HttpURLConnection httpCoordinates = null;
        String[] content = new String[2];

        try { /*try 1*/
            URL url = new URL(strUrl[0]);
            http = (HttpURLConnection)url.openConnection();
            http.setRequestProperty("Accept", "application/json");

            URL urlCoordinates = new URL(strUrl[1]);
            httpCoordinates = (HttpURLConnection)urlCoordinates.openConnection();
            httpCoordinates.setRequestProperty("Accept", "application/json");

            if( http.getResponseCode() == HttpURLConnection.HTTP_OK ) { /*1.*/
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader( http.getInputStream() ));
                String line;

                while ((line = reader.readLine()) != null) { /*1.1*/
                    sb.append(line);
                } /*1.1*/
                content[0] = sb.toString();
                reader.close();
            } /*1.*/

            if( httpCoordinates.getResponseCode() == HttpURLConnection.HTTP_OK ) { /*2.*/
                StringBuilder sbc = new StringBuilder();
                BufferedReader readerCoordinates = new BufferedReader(
                        new InputStreamReader( httpCoordinates.getInputStream() ));
                String line;

                while ((line = readerCoordinates.readLine()) != null) { /*1.1*/
                    sbc.append(line);
                } /*1.1*/
                content[1] = sbc.toString();
                readerCoordinates.close();
            } /*2.*/
        } /*try 1*/
        catch(Exception e) { /*catch 1.1*/
            e.printStackTrace();
        } /*catch 1.1*/
        finally { /*finally 1.1*/
            if(http != null) { /*1.*/
                http.disconnect();

                if(httpCoordinates != null) { /*1.1*/
                    httpCoordinates.disconnect();
                } /*1.1*/
            } /*1.*/
        } /*finally 1.1*/

        return content;
    } /*m.getHttpRequest*/


} //Fin clase MyHttpRequest
