import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/autocomplete")
public class AutoCompleteServlet extends HttpServlet
{
    private static final long serialVersionUID = 453658L;
    private DataSource dataSource;
    private HashSet<String> stopwords;

    public void init()
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            stopwords = new HashSet<>();
            stopwords.add("a");
            stopwords.add("about");
            stopwords.add("an");
            stopwords.add("are");
            stopwords.add("as");
            stopwords.add("at");
            stopwords.add("be");
            stopwords.add("by");
            stopwords.add("com");
            stopwords.add("de");
            stopwords.add("en");
            stopwords.add("for");
            stopwords.add("from");
            stopwords.add("how");
            stopwords.add("i");
            stopwords.add("in");
            stopwords.add("is");
            stopwords.add("it");
            stopwords.add("la");
            stopwords.add("of");
            stopwords.add("on");
            stopwords.add("or");
            stopwords.add("that");
            stopwords.add("the");
            stopwords.add("this");
            stopwords.add("to");
            stopwords.add("was");
            stopwords.add("what");
            stopwords.add("when");
            stopwords.add("where");
            stopwords.add("who");
            stopwords.add("will");
            stopwords.add("with");
            stopwords.add("und");
            stopwords.add("the");
            stopwords.add("www");
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
        PrintWriter out = response.getWriter();

        try
        {
            JsonArray resultJSON = generateResults(request.getParameter("query"));
            out.write(resultJSON.toString());
            response.setStatus(200);
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

    private JsonArray generateResults(String query) throws SQLException
    {
        Connection dbcon = dataSource.getConnection();
        PreparedStatement statement = dbcon.prepareStatement(
            "SELECT id, title, year " +
            "FROM movies " +
            "WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) OR LOWER(title) LIKE ? OR edth(LOWER(title), ?, ?) " +
            "LIMIT 10"
        );

        String[] titleTokens = Arrays
            .stream(query.split("[\\p{Punct}\\p{Digit}\\p{Space}]+"))
            .filter(token -> stopwords.contains(token.toLowerCase()) == false)
            .map(token -> "+" + token + "*")
            .toArray(String[]::new);
        statement.setString(1, String.join(" ", titleTokens));
        statement.setString(2, "%" + query.toLowerCase() + "%");
        statement.setString(3, query.toLowerCase());
        statement.setLong(4, getFuzzinessFactor(query));

        ResultSet results = statement.executeQuery();
        JsonArray resultJSON = new JsonArray();
        while (results.next()) 
        {
            JsonObject dataJsonObject = new JsonObject();
            dataJsonObject.addProperty("id", results.getString("id"));
            
            JsonObject entryJsonObject = new JsonObject();
            entryJsonObject.addProperty("value", results.getString("title"));
            entryJsonObject.add("data", dataJsonObject);
            resultJSON.add(entryJsonObject);
        }
        
        results.close();
        statement.close();
        dbcon.close();
        
        return resultJSON;
    }

    private long getFuzzinessFactor(String term)
    {
        return Arrays
            .stream(term.split("[\\p{Punct}\\p{Digit}\\p{Space}]+"))
            .mapToInt(String::length)
            .map(this::getEditTolerances)
            .sum();
    }

    private int getEditTolerances(int length)
    {
        if (length < 3)
        {
            return 0;
        }
        else if (length < 6)
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }
}
