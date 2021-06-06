import com.flixster.entity.CartItem;
import com.flixster.entity.Customer;
import com.flixster.entity.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet 
{
    private static final long serialVersionUID = 21L;

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
        PrintWriter out = response.getWriter();

        try
        {
            HttpSession session = request.getSession();
            Boolean successful = (Boolean) session.getAttribute("paymentSuccessful");

            if (successful == null)
            {
                successful = false;
            }

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("successful", successful);
            out.write(responseJson.toString());

        } 
        catch (Exception e)
        {
            handleError(e, out, response);
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
            HttpSession session = request.getSession();

            String firstName = request.getParameter("first-name");
            String lastName = request.getParameter("last-name");
            String creditcard = request.getParameter("credit-card");
            String expirationDate = request.getParameter("expiration-date");

            Connection dbcon = dataSource.getConnection();
            PreparedStatement statement = dbcon.prepareStatement(
                "SELECT * from creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?"
            );
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, creditcard);
            statement.setString(4, expirationDate);
            ResultSet rs = statement.executeQuery();

            JsonObject responseJson = new JsonObject();

            if (rs.next())
            {
                Customer user = (Customer) request.getSession().getAttribute("customer");
                ArrayList<CartItem> cartItems = (ArrayList<CartItem>) session.getAttribute("cartItems");
                writeSalesData(user, cartItems, responseJson);
                responseJson.addProperty("successful", true);
                session.setAttribute("paymentSuccessful", true);
                session.setAttribute("cartItems", new ArrayList<>());
            } 
            else 
            {
                response.setStatus(400);
                responseJson.addProperty("successful", false);
                session.setAttribute("paymentSuccessful", false);
            }
            rs.close();
            statement.close();
            dbcon.close();
            out.write(responseJson.toString());


        } 
        catch (Exception e)
        {
            handleError(e, out, response);
        } 
        finally 
        {
            out.close();
        }
    }

    private void handleError(Exception e, PrintWriter out, HttpServletResponse response)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("errorMessage", e.getMessage());
        out.write(jsonObject.toString());
        response.setStatus(500);
    }

    private void writeSalesData(Customer user, ArrayList<CartItem> items, JsonObject responseJson) throws SQLException
    {
        Connection dbcon = dataSource.getConnection();
        responseJson.add("sales", new JsonArray());
        int cost = 0;
        for (CartItem item : items)
        {
            cost += (item.getPrice() * item.getQuantity());
            PreparedStatement statement = dbcon.prepareStatement(
                "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, CURDATE(), ?);",
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, user.getID());
            statement.setString(2, item.getItemId());
            statement.setInt(3, item.getQuantity());
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next())
            {
                JsonObject sale = new JsonObject();
                sale.addProperty("saleId", rs.getInt(1));
                sale.addProperty("title", item.getTitle());
                sale.addProperty("quantity", item.getQuantity());
                responseJson.getAsJsonArray("sales").add(sale);
            }
            rs.close();
            statement.close();
        }
        dbcon.close();
        responseJson.addProperty("totalCost", cost);
    }
}
