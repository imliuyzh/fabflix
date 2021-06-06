import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "MovieServlet", urlPatterns = "/api/movie")
public class MovieServlet extends HttpServlet 
{
    private static final long serialVersionUID = 3256886466305207275L;

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
            JsonObject resultJSON = getMovie(id);
            resultJSON.add("genres", getGenres(id));
            resultJSON.add("stars", getStars(id));

            resultJSON.addProperty("successful", true);
            out.write(resultJSON.toString());
            response.setStatus(200);
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
    
    private JsonObject getMovie(String movieID) throws SQLException
    {
    	Connection dbcon = dataSource.getConnection();
		PreparedStatement statement = dbcon.prepareStatement(
		    "SELECT title, year, director, IFNULL(rating, -1) AS rating, IFNULL(numVotes, -1) AS numVotes " +
		    "FROM movies LEFT JOIN ratings ON movies.id = ratings.movieId " +
		    "WHERE movies.id = ?"
		);
    	statement.setString(1, movieID);
    	ResultSet results = statement.executeQuery();

	    JsonObject resultJSON = new JsonObject();
	    while (results.next()) 
	    {
			resultJSON.addProperty("title", results.getString("title"));
			resultJSON.addProperty("year", results.getInt("year"));
			resultJSON.addProperty("director", results.getString("director"));
			resultJSON.addProperty("rating", results.getDouble("rating"));
			resultJSON.addProperty("numVotes", results.getInt("numVotes"));
		}
        
        results.close();
        statement.close();
        dbcon.close();
        
        return resultJSON;
    }
    
    private JsonArray getGenres(String movieID) throws SQLException
	{
		Connection dbcon = dataSource.getConnection();
		PreparedStatement statement = dbcon.prepareStatement(
			"SELECT G.id, G.name " +
			"FROM genres_in_movies Gim, genres G " +
			"WHERE Gim.genreId = G.id AND Gim.movieId = ? " +
			"ORDER BY G.name ASC"
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
	    dbcon.close();
	    return array;
	}
	
    private JsonArray getStars(String movieID) throws SQLException
	{
		Connection dbcon = dataSource.getConnection();
		PreparedStatement statement = dbcon.prepareStatement(
			"SELECT S.id, S.name, S.birthYear " +
			"FROM stars_in_movies Sim, stars S, (" +
			    "SELECT Sim2.starId, COUNT(*) AS total " +
			    "FROM stars_in_movies Sim2 " +
			    "WHERE EXISTS (SELECT * FROM stars_in_movies Sim3 WHERE Sim3.movieId = ? AND Sim3.starId = Sim2.starId) " +
			    "GROUP BY Sim2.starId" +
			") AS Mc " +
			"WHERE Sim.starId = S.id AND Mc.starId = S.id AND Mc.starId = Sim.starId AND Sim.movieId = ? " +
			"ORDER BY Mc.total DESC, S.name ASC"
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
	    dbcon.close();
	    return array;
	}    
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        
        try
        {
            String movieId = findMovie(request);
            if (movieId == null)
            {
                JsonObject jsonObject = new JsonObject();
                String starId = findStar(request), genreId = findGenre(request);
                if (starId != null)
                {
                    jsonObject.addProperty("starMessage", String.format(
                        "%s (%s) was found.", request.getParameter("starName"), starId
                    ));
                }
                if (genreId != null)
                {
                    jsonObject.addProperty("genreMessage", String.format(
                        "%s (%s) was found.", request.getParameter("genre"), genreId
                    ));
                }
                
                Connection dbcon = dataSource.getConnection();
                
                CallableStatement insertStatement = dbcon.prepareCall("CALL add_movie(?, ?, ?, ?, ?, ?)");
                insertStatement.setString(1, request.getParameter("title"));
                insertStatement.setInt(2, Integer.parseInt(request.getParameter("movieYear")));
                insertStatement.setString(3, request.getParameter("director"));
                insertStatement.setString(4, request.getParameter("starName"));
                if (request.getParameter("starBirthYear").length() > 0)
                {
                    insertStatement.setInt(5, Integer.parseInt(request.getParameter("starBirthYear")));
                }
                else
                {
                    insertStatement.setNull(5, java.sql.Types.INTEGER);
                }
                insertStatement.setString(6, request.getParameter("genre"));
                insertStatement.executeUpdate();
                insertStatement.close();
                
                PreparedStatement statement = null;
                if (request.getParameter("starBirthYear").length() > 0)
                {
                    statement = dbcon.prepareStatement(
                        "SELECT M.id, S.id, G.id " +
                        "FROM movies M, stars S, stars_in_movies Sim, genres G, genres_in_movies Gim " +
                        "WHERE M.title = ? AND M.year = ? AND M.director = ? " + 
                            "AND S.name = ? AND S.birthYear = ? AND Sim.starId = S.id AND Sim.movieId = M.id " +
                            "AND G.name = ? AND Gim.genreId = G.id AND Gim.movieId = M.id AND Gim.movieId = Sim.movieId"
                    );
                    statement.setInt(5, Integer.parseInt(request.getParameter("starBirthYear")));
                    statement.setString(6, request.getParameter("genre"));
                }
                else
                {
                    statement = dbcon.prepareStatement(
                        "SELECT M.id, S.id, G.id " +
                        "FROM movies M, stars S, stars_in_movies Sim, genres G, genres_in_movies Gim " +
                        "WHERE M.title = ? AND M.year = ? AND M.director = ? " + 
                            "AND S.name = ? AND Sim.starId = S.id AND Sim.movieId = M.id " +
                            "AND G.name = ? AND Gim.genreId = G.id AND Gim.movieId = M.id AND Gim.movieId = Sim.movieId"
                    );
                    statement.setString(5, request.getParameter("genre"));
                }
                statement.setString(1, request.getParameter("title"));
                statement.setInt(2, Integer.parseInt(request.getParameter("movieYear")));
                statement.setString(3, request.getParameter("director"));
                statement.setString(4, request.getParameter("starName"));
                
                ResultSet rs = statement.executeQuery();
                if (rs.next())
                {
                    jsonObject.addProperty("successful", true);
                    jsonObject.addProperty("movieMessage", String.format(
                        "%s (%s) is entered.", request.getParameter("title"), rs.getString(1)
                    ));
                    if (starId == null)
                    {
                        jsonObject.addProperty("starMessage", String.format(
                            "%s (%s) is entered.", request.getParameter("starName"), rs.getString(2)
                        ));
                    }
                    if (genreId == null)
                    {
                        jsonObject.addProperty("genreMessage", String.format(
                            "%s (%s) is entered.", request.getParameter("genre"), rs.getString(3)
                        ));
                    }
                    out.write(jsonObject.toString());
                    response.setStatus(201);
                }
                else
                {
                    throw new Exception("Movie is not entered.");
                }
                
                rs.close();
                statement.close();
                dbcon.close();
            }
            else
            {
                throw new Exception(String.format(
                    "%s (%s) was found.", request.getParameter("title"), movieId
                ));
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
    
    private String findMovie(HttpServletRequest request) throws SQLException
    {
		Connection dbcon = dataSource.getConnection();
		PreparedStatement statement = dbcon.prepareStatement("SELECT id FROM movies WHERE UPPER(title) = UPPER(?) AND year = ? AND UPPER(director) = UPPER(?)");
		statement.setString(1, request.getParameter("title"));
		statement.setInt(2, Integer.parseInt(request.getParameter("movieYear")));
		statement.setString(3, request.getParameter("director"));
		
		ResultSet results = statement.executeQuery();
		String movieId = (results.next()) ? results.getString("id") : null;
	    
	    results.close();
	    statement.close();
	    dbcon.close();
	    
	    return movieId;
    }
    
    private String findStar(HttpServletRequest request) throws SQLException
    {
		Connection dbcon = dataSource.getConnection();
		PreparedStatement statement = null;
		if (request.getParameter("starName").length() > 0 && request.getParameter("starBirthYear").length() > 0)
		{
		    statement = dbcon.prepareStatement(
		        "SELECT id " +
		        "FROM stars " +
		        "WHERE UPPER(name) = UPPER(?) AND birthYear = ? " +
		        "LIMIT 1"
	        );
		    statement.setString(1, request.getParameter("starName"));
		    statement.setInt(2, Integer.parseInt(request.getParameter("starBirthYear")));
		}
		else
		{
		    statement = dbcon.prepareStatement("SELECT id FROM stars WHERE name = ? LIMIT 1");
		    statement.setString(1, request.getParameter("starName"));
		}
		
		ResultSet results = statement.executeQuery();
		String starId = (results.next()) ? results.getString("id") : null;
		
	    results.close();
	    statement.close();
	    dbcon.close();
	    
	    return starId;
    }
    
    private String findGenre(HttpServletRequest request) throws SQLException
    {
		Connection dbcon = dataSource.getConnection();
		PreparedStatement statement = dbcon.prepareStatement("SELECT id FROM genres WHERE UPPER(name) = UPPER(?)");
	    statement.setString(1, request.getParameter("genre"));
		
		ResultSet results = statement.executeQuery();
		String genreId = (results.next()) ? results.getString("id") : null;
	    
	    results.close();
	    statement.close();
	    dbcon.close();
	    
	    return genreId;
    }
}
