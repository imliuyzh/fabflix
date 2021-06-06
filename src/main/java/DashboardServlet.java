import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard")
public class DashboardServlet extends HttpServlet 
{
    private static final long serialVersionUID = 49657581841584318L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("text/html; charset=utf-8");
        try
        {
            response.sendRedirect(request.getContextPath() + "/_dashboard.html");
        }
        catch (Exception e)
        {
            response.setStatus(500);
        }
    }
}
