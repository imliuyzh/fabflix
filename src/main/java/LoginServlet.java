import com.google.gson.JsonObject;
import com.flixster.entity.*;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet 
{
    private static final long serialVersionUID = 9148555034844992869L;

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
		response.setContentType("application/json; charset=utf-8");
        
        try 
        {
            //mobile devices will identify with a mobile deviceType. If it is null we will assume browser behavior.
            if (request.getParameter("deviceType") == null)
            {
                RecaptchaVerifyUtils.verify(request.getParameter("g-recaptcha-response"));
            }
        } 
        catch (Exception e) 
        {
            PrintWriter out = response.getWriter();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("successful", false);
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
            out.close();
            return;
        }
        
        if (request.getParameter("userType").equals("customer"))
        {
            handleCustomerLoginRequest(request, response);
        }
        else
        {
            handleEmployeeLoginRequest(request, response);
        }
    }
    
    private void handleCustomerLoginRequest(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        String email = request.getParameter("email"), password = request.getParameter("password");
        PrintWriter out = response.getWriter();
        
        try 
        (
            Connection dbcon = dataSource.getConnection();
            PreparedStatement statement = dbcon.prepareStatement(
                "SELECT C.id, C.firstName, C.lastName, C.email, C.password, C.ccId " +
                "FROM customers C " +
                "WHERE C.email = ?"
            );
        )
        {
            statement.setString(1, email);
            ResultSet results = statement.executeQuery();
    	
            JsonObject resultJSON = new JsonObject();
            if (results.next())
            {
                if (new StrongPasswordEncryptor().checkPassword(password, results.getString("password")))
                {
                    response.setStatus(200);
                    request.getSession().setAttribute("customer", new Customer(
                        results.getString("id"),
                        results.getString("firstName"),
                        results.getString("lastName"),
                        results.getString("ccId"),
                        results.getString("email")
                    ));
                    resultJSON.addProperty("successful", true);
                    resultJSON.addProperty("id", results.getString("id"));
                    resultJSON.addProperty("firstName", results.getString("firstName"));
                    resultJSON.addProperty("lastName", results.getString("lastName"));
                }
                else
                {
                    response.setStatus(401);
                    resultJSON = handlePasswordFailure();
                }
            }
            else
            {
                response.setStatus(401);
                resultJSON = handlePasswordFailure();
            }
            
            out.write(resultJSON.toString());
            results.close();
        }
        catch (Exception e)
        {
            response.setStatus(500);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
        }
        finally
		{
			out.close();
		}
    }
    
    private void handleEmployeeLoginRequest(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        String email = request.getParameter("email"), password = request.getParameter("password");
        PrintWriter out = response.getWriter();
        
        try 
        (
            Connection dbcon = dataSource.getConnection();
            PreparedStatement statement = dbcon.prepareStatement(
                "SELECT E.email, E.password, E.fullname " +
                "FROM employees E " +
                "WHERE E.email = ?"
            );
        )
        {
            statement.setString(1, email);
            ResultSet results = statement.executeQuery();
    	
            JsonObject resultJSON = new JsonObject();
            if (results.next())
            {
                if (new StrongPasswordEncryptor().checkPassword(password, results.getString("password")))
                {
                    response.setStatus(200);
                    String[] firstLastName = results.getString("fullname").split(" ");
                    request.getSession().setAttribute("employee", new User(results.getString("email"), firstLastName[0], firstLastName[1]));
                    resultJSON.addProperty("successful", true);
                    resultJSON.addProperty("email", results.getString("email"));
                    resultJSON.addProperty("firstName", firstLastName[0]);
                    resultJSON.addProperty("lastName", firstLastName[1]);
                }
                else
                {
                    response.setStatus(401);
                    resultJSON = handlePasswordFailure();
                }
            }
            else
            {
                response.setStatus(401);
                resultJSON = handlePasswordFailure();
            }
            
            out.write(resultJSON.toString());
            results.close();
        }
        catch (Exception e)
        {
            response.setStatus(500);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("successful", false);
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
        }
        finally
		{
			out.close();
		}
    }

    private JsonObject handlePasswordFailure()
    {
        JsonObject resultJSON = new JsonObject();
        resultJSON.addProperty("successful", false);
        resultJSON.addProperty("errorMessage", "Login or password is invalid.");
        return resultJSON;
    }
}
