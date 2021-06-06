import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.flixster.entity.Customer;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet 
{
    private static final long serialVersionUID = 3909433141967055584L;

    private DataSource dataSource;
    private HashSet<String> stopwords;

    public void init(ServletConfig config)
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
            Customer user = (Customer) request.getSession().getAttribute("customer");
            if (request.getParameter("title").length() > 0
                || request.getParameter("year").length() > 0
                || request.getParameter("director").length() > 0
                || request.getParameter("star").length() > 0)
            {
                getSearchRequestParameters(request);
                processSearchRequest(request, response);
            }
            else if (request.getParameter("selectedGenre").length() > 0 || request.getParameter("selectedTitle").length() > 0)
            {
                getBrowseRequestParameters(request);
                processBrowseRequest(request, response);
            }
            else
            {
                getDefaultRequestParameters(request);
                processDefaultRequest(request, response);
            }
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
    
    private void getSearchRequestParameters(HttpServletRequest request)
    {
        Customer user = (Customer) request.getSession().getAttribute("customer");
        user.setSessionProperty("title", request.getParameter("title"));
        user.setSessionProperty("year", request.getParameter("year"));
        user.setSessionProperty("director", request.getParameter("director"));
        user.setSessionProperty("star", request.getParameter("star"));
        user.setSessionProperty("numResults", request.getParameter("numResults"));
        user.setSessionProperty("offset", request.getParameter("offset"));
        user.setSessionProperty("selectedGenre", "");
        user.setSessionProperty("selectedTitle", "");
        user.setSessionProperty("sortBy1", request.getParameter("sortBy1"));
        user.setSessionProperty("sortOrder1", request.getParameter("sortOrder1"));
        user.setSessionProperty("sortBy2", request.getParameter("sortBy2"));
        user.setSessionProperty("sortOrder2", request.getParameter("sortOrder2"));
    }
    
    private String buildSearchQuery(Customer user)
    {
        StringBuilder query = new StringBuilder(
            "SELECT M.id, M.title, M.year, M.director, IFNULL(R.rating, -1) AS rating, IFNULL(R.numVotes, -1) AS numVotes " +
            "FROM movies M LEFT OUTER JOIN ratings R ON M.id=R.movieId "
        );
        StringBuilder joinConditions = new StringBuilder(
            "WHERE 1=1 "
        );
        
        if (user.getSessionProperty("title").length() > 0)
        {
            joinConditions.append("AND MATCH (M.title) AGAINST (? IN BOOLEAN MODE) OR LOWER(M.title) LIKE ? OR edth(LOWER(M.title), ?, ?) ");
        }
        if (user.getSessionProperty("year").length() > 0)
        {
            joinConditions.append("AND M.year = ? ");
        }
        if (user.getSessionProperty("director").length() > 0)
        {
            joinConditions.append("AND LOWER(M.director) LIKE ? ");
        }
        if (user.getSessionProperty("star").length() > 0)
        {
            query.append(", stars S, stars_in_movies Sim ");
            joinConditions.append("AND M.id = Sim.movieId AND S.id = Sim.starId AND LOWER(S.name) LIKE ? ");
        }
        
        query.append(joinConditions);
        
        if (user.getSessionProperty("sortBy1").equals("title"))
        {
            query.append("ORDER BY M.title ");
        }
        else if (user.getSessionProperty("sortBy1").equals("rating"))
        {
            query.append("ORDER BY R.rating ");
        }
        if (user.getSessionProperty("sortOrder1").equals("ASC"))
        {
            query.append("ASC, ");
        }
        else if (user.getSessionProperty("sortOrder1").equals("DESC"))
        {
            query.append("DESC, ");
        }
        
        if (user.getSessionProperty("sortBy2").equals("title"))
        {
            query.append("M.title ");
        }
        else if (user.getSessionProperty("sortBy2").equals("rating"))
        {
            query.append("R.rating ");
        }
        if (user.getSessionProperty("sortOrder2").equals("ASC"))
        {
            query.append("ASC ");
        }
        else if (user.getSessionProperty("sortOrder2").equals("DESC"))
        {
            query.append("DESC ");
        }
        
        query.append("LIMIT ? ");
        query.append("OFFSET ?");
        
        return query.toString();
    }
    
    private void processSearchRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException
    {
        Customer user = (Customer) request.getSession().getAttribute("customer");
        PrintWriter out = response.getWriter();
        
        Connection dbcon = dataSource.getConnection();
        PreparedStatement statement = dbcon.prepareStatement(buildSearchQuery(user));
        
        String[] parameters = new String[] { "title", "year", "director", "star" };
        int counter = 1;
        for (String parameter : parameters)
        {
            if (user.getSessionProperty(parameter).length() > 0)
            {
                if (parameter.equals("year"))
                {
                    statement.setInt(counter, Integer.parseInt(user.getSessionProperty(parameter)));
                }
                else if (parameter.equals("title"))
                {
                    String[] titleTokens = Arrays
                        .stream(user.getSessionProperty(parameter).split("[\\p{Punct}\\p{Digit}\\p{Space}]+"))
                        .filter(token -> stopwords.contains(token.toLowerCase()) == false)
                        .map(token -> "+" + token + "*")
                        .toArray(String[]::new);
                    statement.setString(counter, String.join(" ", titleTokens));
                    
                    ++counter;
                    statement.setString(counter, "%" + user.getSessionProperty(parameter).toLowerCase() + "%");
                    
                    ++counter;
                    statement.setString(counter, user.getSessionProperty(parameter).toLowerCase());
                    
                    ++counter;
                    statement.setLong(counter, getFuzzinessFactor(user.getSessionProperty("title")));
                }
                else
                {
                    if (user.getSessionProperty(parameter) != null)
                    {
                        statement.setString(counter, "%" + user.getSessionProperty(parameter).toLowerCase() + "%");
                    }
                }
                ++counter;
            }
        }
        statement.setInt(counter, Integer.parseInt(user.getSessionProperty("numResults")));
        statement.setInt(counter + 1, Integer.parseInt(user.getSessionProperty("offset")));
        ResultSet results = statement.executeQuery();
        
        JsonArray resultJSON = new JsonArray();
        while (results.next()) 
        {
            JsonArray genres = getGenres(results.getString("id"), dbcon), stars = getStars(results.getString("id"), dbcon);
            JsonObject jsonObject = new JsonObject();
            
            jsonObject.addProperty("id", results.getString("id"));
            jsonObject.addProperty("title", results.getString("title"));
            jsonObject.addProperty("year", results.getInt("year"));
            jsonObject.addProperty("director", results.getString("director"));
            jsonObject.addProperty("rating", results.getDouble("rating"));
            jsonObject.addProperty("numVotes", results.getInt("numVotes"));
            jsonObject.add("genres", genres);
            jsonObject.add("stars", stars);
            
            resultJSON.add(jsonObject);
        }
        out.write(resultJSON.toString());
        response.setStatus(200);

        results.close();
        statement.close();
        dbcon.close();
    }
    
    private void getBrowseRequestParameters(HttpServletRequest request)
    {
        Customer user = (Customer) request.getSession().getAttribute("customer");
        user.setSessionProperty("title", "");
        user.setSessionProperty("year", "");
        user.setSessionProperty("director", "");
        user.setSessionProperty("star", "");
        user.setSessionProperty("numResults", request.getParameter("numResults"));
        user.setSessionProperty("offset", request.getParameter("offset"));
        user.setSessionProperty("selectedGenre", request.getParameter("selectedGenre"));
        user.setSessionProperty("selectedTitle", request.getParameter("selectedTitle"));
        user.setSessionProperty("sortBy1", request.getParameter("sortBy1"));
        user.setSessionProperty("sortOrder1", request.getParameter("sortOrder1"));
        user.setSessionProperty("sortBy2", request.getParameter("sortBy2"));
        user.setSessionProperty("sortOrder2", request.getParameter("sortOrder2"));
    }
    
    private String buildBrowseQuery(Customer user)
    {
        StringBuilder query = new StringBuilder("");
        if (user.getSessionProperty("selectedGenre").length() > 0)
        {
            query.append(
                "SELECT M.id, M.title, M.year, M.director, IFNULL(rating, -1) as rating, IFNULL(numVotes, -1) as numVotes " +
                "FROM movies M LEFT OUTER JOIN ratings R ON M.id=R.movieId LEFT OUTER JOIN genres_in_movies Gim ON M.id=Gim.movieId " +
                "WHERE Gim.genreId = ? " +
                "ORDER BY "
            );
        }
        else if (user.getSessionProperty("selectedTitle").length() > 0)
        {
            query.append(
                "SELECT M.id, M.title, M.year, M.director, IFNULL(rating, -1) as rating, IFNULL(numVotes, -1) as numVotes " +
                "FROM movies M LEFT OUTER JOIN ratings R ON M.id=R.movieId " +
                "WHERE UPPER(M.title) REGEXP ? " +
                "ORDER BY "
            );
        }
        
        if (user.getSessionProperty("sortBy1").equals("title"))
        {
            query.append("M.title ");
        }
        else if (user.getSessionProperty("sortBy1").equals("rating"))
        {
            query.append("R.rating ");
        }
        if (user.getSessionProperty("sortOrder1").equals("ASC"))
        {
            query.append("ASC, ");
        }
        else if (user.getSessionProperty("sortOrder1").equals("DESC"))
        {
            query.append("DESC, ");
        }
        
        if (user.getSessionProperty("sortBy2").equals("title"))
        {
            query.append("M.title ");
        }
        else if (user.getSessionProperty("sortBy2").equals("rating"))
        {
            query.append("R.rating ");
        }
        if (user.getSessionProperty("sortOrder2").equals("ASC"))
        {
            query.append("ASC ");
        }
        else if (user.getSessionProperty("sortOrder2").equals("DESC"))
        {
            query.append("DESC ");
        }
        
        query.append("LIMIT ? ");
        query.append("OFFSET ?");
        
        return query.toString();
    }
    
    private void processBrowseRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException
    {
        Customer user = (Customer) request.getSession().getAttribute("customer");
        PrintWriter out = response.getWriter();
        
        Connection dbcon = dataSource.getConnection();
        PreparedStatement statement = dbcon.prepareStatement(buildBrowseQuery(user));
        if (user.getSessionProperty("selectedGenre").length() > 0)
        {
            statement.setInt(1, Integer.parseInt(user.getSessionProperty("selectedGenre")));
        }
        else if (user.getSessionProperty("selectedTitle").length() > 0)
        {
            if (user.getSessionProperty("selectedTitle").equals("*"))
            {
                statement.setString(1, "^[^A-Z0-9]");
            }
            else
            {
                statement.setString(1, "^" + user.getSessionProperty("selectedTitle"));
            }
        }
        statement.setInt(2, Integer.parseInt(user.getSessionProperty("numResults")));
        statement.setInt(3, Integer.parseInt(user.getSessionProperty("offset")));
        ResultSet results = statement.executeQuery();
        
        JsonArray resultJSON = new JsonArray();
        while (results.next()) 
        {
            JsonArray genres = getGenres(results.getString("id"), dbcon), stars = getStars(results.getString("id"), dbcon);
            JsonObject jsonObject = new JsonObject();
            
            jsonObject.addProperty("id", results.getString("id"));
            jsonObject.addProperty("title", results.getString("title"));
            jsonObject.addProperty("year", results.getInt("year"));
            jsonObject.addProperty("director", results.getString("director"));
            jsonObject.addProperty("rating", results.getDouble("rating"));
            jsonObject.addProperty("numVotes", results.getInt("numVotes"));
            jsonObject.add("genres", genres);
            jsonObject.add("stars", stars);
            
            resultJSON.add(jsonObject);
        }
        out.write(resultJSON.toString());
        response.setStatus(200);

        results.close();
        statement.close();
        dbcon.close();
    }
    
    private void getDefaultRequestParameters(HttpServletRequest request)
    {
        Customer user = (Customer) request.getSession().getAttribute("customer");
        user.setSessionProperty("title", "");
        user.setSessionProperty("year", "");
        user.setSessionProperty("director", "");
        user.setSessionProperty("star", "");
        user.setSessionProperty("numResults", request.getParameter("numResults"));
        user.setSessionProperty("offset", request.getParameter("offset"));
        user.setSessionProperty("selectedGenre", "");
        user.setSessionProperty("selectedTitle", "");
        user.setSessionProperty("sortBy1", request.getParameter("sortBy1"));
        user.setSessionProperty("sortOrder1", request.getParameter("sortOrder1"));
        user.setSessionProperty("sortBy2", request.getParameter("sortBy2"));
        user.setSessionProperty("sortOrder2", request.getParameter("sortOrder2"));
    }
    
    private String buildDefaultQuery(Customer user)
    {
        StringBuilder query = new StringBuilder(
            "SELECT M.id, M.title, M.year, M.director, IFNULL(rating, -1) AS rating, IFNULL(numVotes, -1) AS numVotes " +
            "FROM movies M LEFT OUTER JOIN ratings R ON M.id = R.movieId " +
            "ORDER BY "
        );
        
        if (user.getSessionProperty("sortBy1").equals("title"))
        {
            query.append("M.title ");
        }
        else if (user.getSessionProperty("sortBy1").equals("rating"))
        {
            query.append("R.rating ");
        }
        if (user.getSessionProperty("sortOrder1").equals("ASC"))
        {
            query.append("ASC, ");
        }
        else if (user.getSessionProperty("sortOrder1").equals("DESC"))
        {
            query.append("DESC, ");
        }
        
        if (user.getSessionProperty("sortBy2").equals("title"))
        {
            query.append("M.title ");
        }
        else if (user.getSessionProperty("sortBy2").equals("rating"))
        {
            query.append("R.rating ");
        }
        if (user.getSessionProperty("sortOrder2").equals("ASC"))
        {
            query.append("ASC ");
        }
        else if (user.getSessionProperty("sortOrder2").equals("DESC"))
        {
            query.append("DESC ");
        }
        
        query.append("LIMIT ? ");
        query.append("OFFSET ?");
        
        return query.toString();
    }
    
    private void processDefaultRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException
    {
        Customer user = (Customer) request.getSession().getAttribute("customer");
        PrintWriter out = response.getWriter();
        
        Connection dbcon = dataSource.getConnection();
        PreparedStatement statement = dbcon.prepareStatement(buildDefaultQuery(user));
        statement.setInt(1, Integer.parseInt(user.getSessionProperty("numResults")));
        statement.setInt(2, Integer.parseInt(user.getSessionProperty("offset")));
        ResultSet results = statement.executeQuery();
        
        JsonArray resultJSON = new JsonArray();
        while (results.next()) 
        {
            JsonArray genres = getGenres(results.getString("id"), dbcon), stars = getStars(results.getString("id"), dbcon);
            JsonObject jsonObject = new JsonObject();
            
            jsonObject.addProperty("id", results.getString("id"));
            jsonObject.addProperty("title", results.getString("title"));
            jsonObject.addProperty("year", results.getInt("year"));
            jsonObject.addProperty("director", results.getString("director"));
            jsonObject.addProperty("rating", results.getDouble("rating"));
            jsonObject.addProperty("numVotes", results.getInt("numVotes"));
            jsonObject.add("genres", genres);
            jsonObject.add("stars", stars);
            
            resultJSON.add(jsonObject);
        }
        out.write(resultJSON.toString());
        response.setStatus(200);

        results.close();
        statement.close();
        dbcon.close();
    }
    
    private JsonArray getGenres(String movieID, Connection dbcon) throws SQLException
    {
        PreparedStatement statement = dbcon.prepareStatement(
	        "SELECT G.id, G.name " +
	        "FROM genres_in_movies Gim, genres G " +
	        "WHERE Gim.genreId = G.id AND Gim.movieId = ? " +
	        "ORDER BY G.name ASC " +
	        "LIMIT 3"
        );
        statement.setString(1, movieID);
        ResultSet results = statement.executeQuery();

        JsonArray array = new JsonArray();
        while (results.next())
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", results.getInt("id"));
            jsonObject.addProperty("name", results.getString("name"));
            array.add(jsonObject);
        }
	    
	    results.close();
	    statement.close();
	    return array;
	}
	
    private JsonArray getStars(String movieID, Connection dbcon) throws SQLException
	{
		PreparedStatement statement = dbcon.prepareStatement(
			"SELECT S.id, S.name, S.birthYear " +
			"FROM stars_in_movies Sim, stars S, (" +
			    "SELECT Sim2.starId, COUNT(*) AS total " +
			    "FROM stars_in_movies Sim2 " +
			    "WHERE EXISTS (SELECT * FROM stars_in_movies Sim3 WHERE Sim3.movieId = ? AND Sim3.starId = Sim2.starId) " +
			    "GROUP BY Sim2.starId" +
			") AS Mc " +
			"WHERE Sim.starId = S.id AND Mc.starId = S.id AND Mc.starId = Sim.starId AND Sim.movieId = ? " +
			"ORDER BY Mc.total DESC, S.name ASC " +
			"LIMIT 3"
		);
		statement.setString(1, movieID);
		statement.setString(2, movieID);
		ResultSet results = statement.executeQuery();

		JsonArray array = new JsonArray();
	    while (results.next())
	    {
	        JsonObject jsonObject = new JsonObject();
	        jsonObject.addProperty("id", results.getString("id"));
	        jsonObject.addProperty("name", results.getString("name"));
	        jsonObject.addProperty("birthYear", results.getInt("birthYear"));
	        array.add(jsonObject);
	    }
	    
	    results.close();
	    statement.close();
	    return array;
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
