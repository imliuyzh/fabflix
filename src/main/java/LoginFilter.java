import com.flixster.entity.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter 
{
    private ArrayList<String> allowedURIs, customerURIS, employeeURIs;
    
    public void init(FilterConfig fConfig) 
    {
        allowedURIs = new ArrayList<>();
        allowedURIs.add("images/favicon.png");
        allowedURIs.add("images/login-pattern-1.png");
        allowedURIs.add("images/login-pattern-2.png");
        allowedURIs.add("images/logo.png");
        allowedURIs.add("login.html");
        allowedURIs.add("styles/login.css");
        allowedURIs.add("scripts/login.js");
        allowedURIs.add("_login.html");
        allowedURIs.add("styles/_login.css");
        allowedURIs.add("scripts/_login.js");
        allowedURIs.add("api/login");
        
        customerURIS = new ArrayList<>();
        customerURIS.add("login.html");
        customerURIS.add("index.html");
        customerURIS.add("movie.html");
        customerURIS.add("star.html");
        customerURIS.add("cart.html");
        customerURIS.add("payment.html");
        customerURIS.add("confirmation.html");
        
        employeeURIs = new ArrayList<>();
        employeeURIs.add("_login.html");
        employeeURIs.add("_dashboard.html");
        employeeURIs.add("_movie.html");
        employeeURIs.add("_star.html");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException 
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI()))
        {
            if (httpRequest.getSession().getAttribute("customer") != null 
                && httpRequest.getSession().getAttribute("employee") == null 
                && httpRequest.getRequestURI().toLowerCase().contains("_"))
            {
                httpResponse.setStatus(401);
            }
            else if (httpRequest.getSession().getAttribute("employee") != null && httpRequest.getRequestURI().toLowerCase().endsWith("_login.html"))
            {
                request.getRequestDispatcher("/_dashboard").forward(request, response);
            }
            else if (httpRequest.getSession().getAttribute("customer") != null && httpRequest.getRequestURI().toLowerCase().endsWith("login.html"))
            {
                Customer user = (Customer) httpRequest.getSession().getAttribute("customer");
                httpResponse.sendRedirect("index.html?" + user.getSessionStateString());
            }
            else
            {
                chain.doFilter(request, response);
            }
        }
        else
        {
            if (httpRequest.getSession().getAttribute("customer") == null && httpRequest.getSession().getAttribute("employee") == null)
            {
                if (httpRequest.getRequestURI().contains("_"))
                {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/_login.html");
                }
                else
                {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
                }
            }
            else if (httpRequest.getSession().getAttribute("customer") != null 
                && httpRequest.getSession().getAttribute("employee") == null 
                && httpRequest.getRequestURI().toLowerCase().contains("_"))
            {
                httpResponse.setStatus(401);
            }
            else if (httpRequest.getSession().getAttribute("customer") == null 
                && httpRequest.getSession().getAttribute("employee") == null 
                && isEmployeeUrl(httpRequest.getRequestURI())) 
            {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/_login.html");
            }
            else if (httpRequest.getSession().getAttribute("customer") == null 
                && isCustomerUrl(httpRequest.getRequestURI())
                && isEmployeeUrl(httpRequest.getRequestURI()) == false)
            {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            }
            else 
            {
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) 
    {
        return allowedURIs
            .stream()
            .anyMatch(requestURI.toLowerCase()::endsWith);
    }
    
    private boolean isCustomerUrl(String requestURI) 
    {
        return customerURIS
            .stream()
            .anyMatch(requestURI.toLowerCase()::endsWith);
    }
    
    private boolean isEmployeeUrl(String requestURI) 
    {
        return employeeURIs
            .stream()
            .anyMatch(requestURI.toLowerCase()::endsWith);
    }
}
