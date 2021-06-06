import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "DatabaseMetadataServlet", urlPatterns = "/api/metadata")
public class DatabaseMetadataServlet extends HttpServlet
{
    private static final long serialVersionUID = 6131163390604762357L;

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
        (
            Connection dbcon = dataSource.getConnection();
            Statement statement = dbcon.createStatement()
        )
        {
            JsonObject resultJSON = new JsonObject();
            ArrayList<String> tables = getTables();
            for (String table : tables)
            {
                JsonArray columnArray = new JsonArray();
                ResultSet tableMetadata = statement.executeQuery(String.format("DESCRIBE %s", table));
                while (tableMetadata.next())
                {
                    JsonObject columnJSON = new JsonObject();
                    columnJSON.addProperty("field", tableMetadata.getString(1));
                    columnJSON.addProperty("type", tableMetadata.getString(2));
                    columnJSON.addProperty("null", tableMetadata.getString(3));
                    columnJSON.addProperty("key", tableMetadata.getString(4));
                    columnJSON.addProperty("default", tableMetadata.getString(5));
                    columnJSON.addProperty("extra", tableMetadata.getString(6));
                    columnArray.add(columnJSON);
                }
                tableMetadata.close();
                resultJSON.add(table, columnArray);
            }
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
    
    private ArrayList<String> getTables() throws SQLException
    {
        ArrayList<String> tables = new ArrayList<>();
        try
        (
            Connection dbcon = dataSource.getConnection();
            Statement statement = dbcon.createStatement();
            ResultSet results = statement.executeQuery("SHOW TABLES")
        )
        {
            while (results.next())
            {
                tables.add(results.getString(1));
            }
        }
        return tables;
    }
}
