import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet 
{
    private static final long serialVersionUID = -5435788639661208512L;

    private DataSource dataSource;

    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
 
        try 
        {
            Connection dbcon = dataSource.getConnection();
            Statement statement = dbcon.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM genres ORDER BY name ASC");

            JsonArray jsonArray = new JsonArray();
            while (results.next())
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", results.getString("id"));
                jsonObject.addProperty("name", results.getString("name"));
                jsonArray.add(jsonObject);
            }
            out.write(jsonArray.toString());
            response.setStatus(200);

            results.close();
            statement.close();
            dbcon.close();
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
}
