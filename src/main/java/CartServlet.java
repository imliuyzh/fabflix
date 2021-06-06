import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.flixster.entity.CartItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet
{
    private static final long serialVersionUID = -4533464990877418630L;
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();

        try
        {
            HttpSession session = request.getSession();
            ArrayList<CartItem> cartItems = (ArrayList<CartItem>) session.getAttribute("cartItems");

            if (cartItems == null)
            {
                cartItems = new ArrayList<>();
                session.setAttribute("cartItems", cartItems);
            }

            JsonObject responseJson = new JsonObject();
            JsonArray cartItemsJsonArray = new JsonArray();
            synchronized (cartItems)
            {
                cartItems.forEach((n) -> cartItemsJsonArray.add(n.getJSON()));
            }
            responseJson.add("cartItems", cartItemsJsonArray);
            writePrice(responseJson, cartItems);
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();

        try
        {
            String item = request.getParameter("item");
            String operation = request.getParameter("operation");

            HttpSession session = request.getSession();
            ArrayList<CartItem> cartItems = (ArrayList<CartItem>) session.getAttribute("cartItems");

            Connection dbcon = dataSource.getConnection();
            PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM movies WHERE id = ?");
            statement.setString(1, item);
            ResultSet rs = statement.executeQuery();

            String title = null;
            int price = -1;
            if (rs.next())
            {
                title = rs.getString("title");
                price = rs.getInt("price");
            }

            if (title != null && price != -1 && item != null)
            {
                if (cartItems == null)
                {
                    cartItems = new ArrayList<>();
                    cartItems.add(new CartItem(item, title, price));
                    session.setAttribute("cartItems", cartItems);
                }
                else
                {
                    synchronized (cartItems)
                    {
                        CartItem cartItem = cartItems.stream().filter(i -> i.getItemId().equals(item)).findFirst().orElse(null);
                        if (cartItem != null)
                        {
                            if(operation != null && operation.equals("decrease")){
                                cartItem.setQuantity(cartItem.getQuantity() - 1);
                            }
                            else
                            {
                                cartItem.setQuantity(cartItem.getQuantity() + 1);
                            }
                        }
                        else
                        {
                            cartItems.add(new CartItem(item, title, price));
                        }
                    }
                }
            }
            else
            {
                throw new Exception("Invalid Item Being Added To Cart");
            }

            synchronized (cartItems)
            {
                cartItems.removeIf(cartItem -> cartItem.getQuantity() <= 0);
            }

            JsonObject responseJson = new JsonObject();
            JsonArray cartItemsJsonArray = new JsonArray();
            synchronized (cartItems)
            {
                cartItems.forEach((n) -> cartItemsJsonArray.add(n.getJSON()));
            }
            responseJson.add("cartItems", cartItemsJsonArray);
            writePrice(responseJson, cartItems);
            out.write(responseJson.toString());

            rs.close();
            statement.close();
            dbcon.close();

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

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        
        try
        {
            String item = request.getParameter("item");
            HttpSession session = request.getSession();
            ArrayList<CartItem> cartItems = (ArrayList<CartItem>) session.getAttribute("cartItems");

            if (cartItems != null)
            {
                synchronized (cartItems)
                {
                    cartItems.removeIf(cartItem -> cartItem.getItemId().equals(item));
                }
            } 
            else 
            {
                cartItems = new ArrayList<>();
            }

            JsonObject responseJson = new JsonObject();
            JsonArray cartItemsJsonArray = new JsonArray();
            synchronized (cartItems)
            {
                cartItems.forEach((n) -> cartItemsJsonArray.add(n.getJSON()));
            }
            responseJson.add("cartItems", cartItemsJsonArray);
            writePrice(responseJson, cartItems);
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

    private void writePrice(JsonObject response, ArrayList<CartItem> cartItems)
    {
        synchronized (cartItems)
        {
            int totalCost = cartItems.stream().mapToInt(n -> (n.getQuantity() * n.getPrice())).sum();
            response.addProperty("totalCost", totalCost);
        }
    }
}
