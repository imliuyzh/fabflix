import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "StarServlet", urlPatterns = "/api/star")
public class StarServlet extends HttpServlet 
{
    private static final long serialVersionUID = -4495189981791518288L;

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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("application/json; charset=utf-8");
        String id = request.getParameter("id");
        PrintWriter out = response.getWriter();

        try
        {
            Connection dbcon = dataSource.getConnection();
            PreparedStatement statement = dbcon.prepareStatement(
                "SELECT S.name, S.birthYear, M.id, M.title, M.year " +
                "FROM stars S, stars_in_movies Sim, movies M " +
                "WHERE M.id = Sim.movieId and Sim.starId = S.id and S.id = ? " +
                "ORDER BY M.year DESC, M.title ASC"
            );
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            JsonObject resultJSON = new JsonObject();
            
            JsonArray jsonArray = new JsonArray();
            while (rs.next())
            {
                resultJSON.addProperty("name", rs.getString("name"));
                resultJSON.addProperty("birthYear", rs.getInt("birthYear"));
                JsonObject obj = new JsonObject();
                obj.addProperty("id", rs.getString("id"));
                obj.addProperty("title", rs.getString("title"));
                obj.addProperty("year", rs.getInt("year"));
                jsonArray.add(obj);
            }
            resultJSON.add("movies", jsonArray);
            resultJSON.addProperty("successful", true);

            out.write(resultJSON.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        }
        catch (Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("successful", false);
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
        finally
        {
            out.close();
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        
        try
        {
            String starId = findStar(request.getParameter("name"));
            if ((request.getParameter("name").length() > 0 && request.getParameter("birthYear").length() > 0) || (starId == null))
            {
                JsonObject jsonObject = insertStar(request, response);
                out.write(jsonObject.toString());
                response.setStatus(201);
            }
            else
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("successful", false);
                jsonObject.addProperty("errorMessage", String.format("%s (%s) was found.", request.getParameter("name"), starId));
                out.write(jsonObject.toString());
                response.setStatus(409);
            }
        }
        catch (Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("successful", false);
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
        finally
        {
            out.close();
        }
    }
    
    private String findStar(String name) throws SQLException
    {
        Connection dbcon = dataSource.getConnection();
        PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM stars S WHERE S.name = ? AND S.birthYear IS NULL LIMIT 1");
        statement.setString(1, name);
        
        String starId = null;
        ResultSet results = statement.executeQuery();
        if (results.next())
        {
            starId = results.getString("id");
        }
        
        results.close();
        statement.close();
        dbcon.close();
        
        return starId;
    }
    
    private JsonObject insertStar(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        String id = UUID.randomUUID().toString().substring(0, 10), name = request.getParameter("name");
        Connection dbcon = dataSource.getConnection();
        PreparedStatement statement = dbcon.prepareStatement("INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)");
        statement.setString(1, id);
        statement.setString(2, name);
        if (request.getParameter("birthYear").length() > 0)
        {
            statement.setInt(3, Integer.parseInt(request.getParameter("birthYear")));
        }
        else
        {
            statement.setNull(3, java.sql.Types.INTEGER);
        }
        
        JsonObject jsonObject = new JsonObject();
        if (statement.executeUpdate() > 0)
        {
            jsonObject.addProperty("successful", true);
            jsonObject.addProperty("id", id);
        }
        else
        {
            throw new Exception("Star is not entered.");
        }
        
        statement.close();
        dbcon.close();
        
        return jsonObject;
    }
}
