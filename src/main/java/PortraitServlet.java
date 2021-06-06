import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PortraitServlet", urlPatterns = "/api/portrait")
public class PortraitServlet extends HttpServlet 
{
    private static final long serialVersionUID = 6626713032858978389L;
    private static final String PORTRAIT_URL = "https://api.themoviedb.org/3/find/%s?api_key=bf61788756f697124fce1f3d70b8bce6&external_source=imdb_id";
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w185";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();

        try
        {
            String id = request.getParameter("id");
            JsonObject result = processResponse(id);
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
    
    private JsonObject processResponse(String id) throws IOException
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(String.format(PORTRAIT_URL, id, Keys.POSTER_SECRET_KEY))
            .build();
        Response resp = client.newCall(request).execute();
        
        JsonObject result = new JsonObject();
        if (resp.code() == 200)
        {
            JsonArray persons = new Gson()
                .fromJson(resp.body().string(), JsonObject.class)
                .getAsJsonArray("person_results");

            if (persons.size() > 0)
            {
                JsonElement path = persons.get(0).getAsJsonObject().get("profile_path");
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
        resp.close();
        return result;
    }
}
