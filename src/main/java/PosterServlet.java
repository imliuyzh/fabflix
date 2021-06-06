import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PosterServlet", urlPatterns = "/api/poster")
public class PosterServlet extends HttpServlet 
{
    private static final long serialVersionUID = 4992595471393230154L;
    private static final String POSTER_REQUEST_URL = "https://api.themoviedb.org/3/find/%s?api_key=%s&external_source=imdb_id";
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w154";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();

        try
        {
            OkHttpClient client = new OkHttpClient();
            String id = request.getParameter("id");
            Request posterRequest = new Request.Builder()
                .url(String.format(POSTER_REQUEST_URL, id, Keys.POSTER_SECRET_KEY))
                .build();
            JsonObject result = processResponse(client.newCall(posterRequest).execute());
            out.write(result.toString());
        }
        catch (Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
        finally
        {
            out.close();
        }
    }

    private JsonObject processResponse(Response resp) throws IOException
    {
        JsonObject result = new JsonObject();
        if (resp.code() == 200)
        {
            JsonArray movies = new Gson()
                .fromJson(resp.body().string(), JsonObject.class)
                .getAsJsonArray("movie_results");

            if (movies.size() > 0)
            {
                JsonElement path = movies.get(0).getAsJsonObject().get("poster_path");
                if (!path.isJsonNull())
                {
                    result.addProperty("successful", true);
                    result.addProperty("path", IMAGE_URL + path.getAsString());
                } 
                else 
                {
                    result.addProperty("successful", false);
                    result.addProperty("path", "");
                }
            }
            else 
            {
                result.addProperty("successful", false);
                result.addProperty("path", "");
            }
        } 
        else 
        {
            result.addProperty("successful", false);
            result.addProperty("path", "");
        }
        return result;
    }
}
