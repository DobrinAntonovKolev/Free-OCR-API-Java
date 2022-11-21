import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OCRClass {

    public static String getStringWithOCR(File file, String apiKEY, String fileType, String lang) {
        try {
            String encodedString = encodeFileToBase64Binary(file);
            String postRequest = "https://api.ocr.space/parse/image";
            URL obj = new URL(postRequest); // OCR API Endpoints
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            JSONObject postDataParams = new JSONObject();

            postDataParams.put("apikey", apiKEY);
            postDataParams.put("isOverlayRequired", false);
            postDataParams.put("scale",true);

            if (fileType != null) postDataParams.put("filetype",fileType.toLowerCase());
            if (lang != null) postDataParams.put("language", lang.toLowerCase());

            encodedString = "data:image/" + fileType + ";base64," + encodedString;

            postDataParams.put("base64Image", encodedString);

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(getPostDataString(postDataParams));
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //return result
            return getParsedText(String.valueOf(response));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> getStringArrayWithOCR(File file, String apiKEY, String fileType, String lang) {

        String fullMsg = getStringWithOCR(file, apiKEY, fileType, lang);
        assert fullMsg != null;
        String[] fullMsgArr = fullMsg.split(System.getProperty("line.separator"));

        return new ArrayList<>(Arrays.asList(fullMsgArr));

    }

    public static String getStringWithOCR(String imgURL, String apiKEY, String fileType, String lang) {
        String website = "";

        String getRequest = "https://api.ocr.space/parse/imageurl?apikey=";
        website = getRequest + apiKEY + "&url=" + imgURL + "&scale=true";
        website = (fileType == null) ? (website) : (website = "&filetype=" + fileType.toLowerCase());
        website = (lang == null) ? (website) : (website = "&language=" + lang.toLowerCase());

        try {
            URL url = new URL(website);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(40000);

            BufferedReader br;
            String line;
            StringBuilder response = new StringBuilder();

            int status = con.getResponseCode();
            if (con.getResponseCode() >= 300) {

                System.out.println("There was a problem while sending a request to the API.\nPlease check your connection and try again.");
                System.out.println("Status code: " + status);
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                System.out.println("Response: " + response.toString());

                return null;
            }

            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            String result = response.toString();

            String fullMsg = getParsedText(result);

            con.disconnect();

            return fullMsg;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> getStringArrayWithOCR(String imgURL, String apiKEY, String fileType, String lang) {
        String fullMsg = getStringWithOCR(imgURL, apiKEY, fileType, lang);
        ArrayList<String> lines = new ArrayList<>();
        assert fullMsg != null;
        String[] fullMsgArr = fullMsg.split(System.getProperty("line.separator"));

        Collections.addAll(lines, fullMsgArr);

        return lines;
    }

    public static String getStringWithOCR(RenderedImage img, String apiKEY, String fileType, String lang) {
        try {
            File outputfile = new File("imagetemporary.png");
            String newType = (fileType != null) ? fileType : "png";
            ImageIO.write(img, newType, outputfile);

            String ocrString = getStringWithOCR(outputfile, apiKEY, fileType, lang);
            System.out.println("Temporary file deleted?: " + outputfile.delete());
            outputfile.deleteOnExit();

            return ocrString;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> getStringArrayWithOCR(RenderedImage img, String apiKEY, String fileType, String lang) {
        String fullMsg = getStringWithOCR(img, apiKEY, fileType, lang);
        ArrayList<String> lines = new ArrayList<>();
        assert fullMsg != null;
        String[] fullMsgArr = fullMsg.split(System.getProperty("line.separator"));

        Collections.addAll(lines, fullMsgArr);

        return lines;
    }

    private static String getParsedText(String fullResult) {
        try {
            JSONParser jp = new JSONParser();
            JSONObject obj = (JSONObject) jp.parse(fullResult);
            JSONArray arr = (JSONArray) obj.get("ParsedResults");
            JSONObject text = (JSONObject) arr.get(0);
            return (String) text.get("ParsedText");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String encodeFileToBase64Binary(File file) {
        String encodedString = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedString = new String(Base64.getEncoder().encode(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedString;
    }

    private static String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : (Iterable<String>) params.keySet()) {

            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));

        }
        return result.toString();
    }
}
