package fr.delta.bedwars.data;

import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import fr.delta.bedwars.Bedwars;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;

public class SkinFetcher {

    /**
     * Gets skin data from file.
     *
     * @param skinID  identifier of the skin
     * @param input file containing the skin
     * @return property containing skin value and signature if successful, otherwise null.
     */
    public static Property setSkinFromFile(Identifier skinID, InputStream input) {
        try {
            var bytes = input.readAllBytes();

            if(bytes[0] == (byte)137) {
                try {
                    String reply = urlRequest(new URL("https://api.mineskin.org/generate/upload?model=" + (isSlim(new ByteArrayInputStream(bytes)) ? "slim" : "steve")), false, skinID, bytes);
                    return getSkinFromReply(reply);
                } catch (IOException e) {
                    // Error uploading
                    Bedwars.LOGGER.warn(e.getMessage());
                }
            }
        } catch (IOException e) {
            // Not an image
            Bedwars.LOGGER.warn(e.getMessage());
        }
        return null;
    }

    /**
     * Sets skin by playername.
     *
     * @param playername name of the player who has the skin wanted
     * @return property containing skin value and signature if successful, otherwise null.
     */
    @Nullable
    public static Property fetchSkinByName(String playername) {
        try {
            String reply = urlRequest(new URL("https://api.mojang.com/users/profiles/minecraft/" + playername), true, null, null);

            if(reply == null || !reply.contains("id")) {
                reply = urlRequest(new URL(String.format("http://skinsystem.ely.by/textures/signed/%s.png?proxy=true", playername)), false, null, null);
            } else {
                String uuid = JsonParser.parseString(reply).getAsJsonObject().get("id").getAsString();
                reply = urlRequest(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"), true, null, null);
            }
            return getSkinFromReply(reply);
        } catch (IOException e) {
            Bedwars.LOGGER.warn(e.getMessage());
        }
        return null;
    }

    /**
     * Sets skin from reply that was got from API.
     * Used internally only.
     *
     * @param reply API reply
     * @return property containing skin value and signature if successful, otherwise null.
     */
    @Nullable
    protected static Property getSkinFromReply(String reply) {
        if(reply == null || reply.contains("error") || reply.isEmpty()) {
            return null;
        }

        String value = reply.split("\"value\":\"")[1].split("\"")[0];
        String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

        return new Property(PlayerSkinProvider.TEXTURES, value, signature);
    }

    /**
     * Gets reply from a skin website.
     * Used internally only.
     *
     * @param url url of the website
     * @param useGetMethod whether to use GET method instead of POST
     * @param input image to upload, otherwise null
     * @return reply from website as string
     * @throws IOException IOException is thrown when connection fails for some reason.
     */
    private static String urlRequest(URL url, boolean useGetMethod, Identifier skinID, byte[] input) throws IOException {
        URLConnection connection = url.openConnection();

        String reply = null;

        if(connection instanceof HttpsURLConnection httpsConnection) {
            httpsConnection.setUseCaches(false);
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);
            httpsConnection.setRequestMethod(useGetMethod ? "GET" : "POST");
            if(skinID != null && input != null) {
                String boundary = UUID.randomUUID().toString();
                httpsConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                httpsConnection.setRequestProperty("User-Agent", "User-Agent");

                OutputStream outputStream = httpsConnection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

                final String LINE = "\r\n";
                writer.append("--").append(boundary).append(LINE);
                writer.append("Content-Disposition: form-data; name=\"file\"").append(LINE);
                writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE);
                writer.append(LINE);
                writer.append(skinID.getPath() + ".png").append(LINE);
                writer.flush();

                writer.append("--").append(boundary).append(LINE);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(skinID.getPath() + ".png").append("\"").append(LINE);
                writer.append("Content-Type: image/png").append(LINE);
                writer.append("Content-Transfer-Encoding: binary").append(LINE);
                writer.append(LINE);
                writer.flush();

                //byte[] fileBytes =  input.readAllBytes();
                outputStream.write(input,  0, input.length);

                outputStream.flush();
                writer.append(LINE);
                writer.flush();

                writer.append("--").append(boundary).append("--").append(LINE);
                writer.close();
            }
            if(httpsConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                reply = getContent(connection);
            else
                Bedwars.LOGGER.error("Error uploading skin: " + httpsConnection.getResponseCode() + " " + httpsConnection.getResponseMessage());
            httpsConnection.disconnect();
        }
        else {
            reply = getContent(connection);
        }
        return reply;
    }

    /**
     * Reads response from API.
     * Used just to avoid duplicate code.
     *
     * @param connection connection where to take output stream from
     * @return API reply as String
     * @throws IOException exception when something went wrong
     */
    private static String getContent(URLConnection connection) throws IOException {
        try (
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                Scanner scanner = new Scanner(isr)
        ) {
            StringBuilder reply = new StringBuilder();
            while(scanner.hasNextLine()) {
                String line = scanner.next();
                if(line.trim().isEmpty())
                    continue;
                reply.append(line);
            }

            return reply.toString();
        }
    }

    /**
     * Checks if the skin is slim.
     *
     * @param input texture of the skin
     * @return true if the skin is slim, otherwise false
     */

    private static boolean isSlim(InputStream input)
    {
        try
        {
            var image = ImageIO.read(input); //this check borders of the arm

            for(int y = 20; y < 31; y++)
                for(int x = 54; x < 55; x++)
                    if(image.getRGB(x, y) != 0) return false;
            for (int y = 36; y < 47; y++)
                for(int x = 54; x < 55; x++)
                    if(image.getRGB(x, y) != 0) return false;

            var xs = new int[]{46, 47, 62, 63};

            for(int y= 52; y < 63; y++)
                for(var x : xs)
                {
                    if(image.getRGB(x, y) != 0) return false; // there is a pixel in the area of the arm
                }
        }
        catch (IOException e)
        {
            Bedwars.LOGGER.warn("Error reading image: " + e.getMessage());
            return false;
        }
        return true;
    }
}

