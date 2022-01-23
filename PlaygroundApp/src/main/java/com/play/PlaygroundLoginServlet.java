package com.play;

import com.google.gson.Gson;
import com.play.com.play.authentication.TokenHandler;
import com.play.dataObjects.User;
import com.play.database.DatabaseManager;
import org.apache.commons.io.IOUtils;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/register", "/login"})
public class PlaygroundLoginServlet extends HttpServlet {

    private DatabaseManager dbManager;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        dbManager = new DatabaseManager(getServletContext());
        String action = request.getServletPath();

        switch (action) {
            case "/login":
                verifyUser(request, response);
                break;
            case "/register":
                registerUser(request, response);
                break;
            default:
                RequestDispatcher view = request.getRequestDispatcher("index.html");
                view.forward(request, response);
        }
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonRequest = IOUtils.toString(request.getReader());

        Gson gson = new Gson();
        User user = gson.fromJson(jsonRequest, User.class);

        String username = user.getUsername();
        String email = user.getEmail();
        String password = user.getPassword();

        long usernameCount = dbManager.selectUsernameCount(username);
        long emailCount = dbManager.selectEmailCount(email);

        if (usernameCount == 0 && emailCount == 0) {

            int rowsAffected = dbManager.insertNewUser(username, email, hashPassword(password));

            if (rowsAffected <= 0) {
                user.setValid(false);
            } else {
                String token = TokenHandler.getAuthToken(username);

                Cookie authCookie = new Cookie("authToken", token);
                authCookie.setMaxAge((int) TokenHandler.SESSION_TIMEOUT);
                authCookie.setSecure(true);
                response.addCookie(authCookie);

                user.setValid(true);
            }
        } else {
            user.setValid(false);
        }

        printResponse(user, response.getWriter());
    }

    private void verifyUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonRequest = IOUtils.toString(request.getReader());

        Gson gson = new Gson();
        User user = gson.fromJson(jsonRequest, User.class);

        String usernameOrEmail = user.getUsernameOrEmail();
        String password = user.getPassword();

        String hashedPassword = dbManager.selectUserPassword(usernameOrEmail);

        if (checkPassword(password, hashedPassword)) {
            String token = TokenHandler.getAuthToken(usernameOrEmail);
            user.setToken(token);
            user.setValid(true);

            Cookie authCookie = new Cookie("authToken", user.getToken());
            authCookie.setMaxAge((int) TokenHandler.SESSION_TIMEOUT);
            authCookie.setSecure(true);
            response.addCookie(authCookie);
        } else {
            user.setValid(false);
        }

        printResponse(user, response.getWriter());
    }

    private void printResponse(User user, PrintWriter out) {
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(user);
        out.println(jsonResponse);
    }

    private String hashPassword(String plainTextPassword){
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    private boolean checkPassword(String plainPassword, String hashedPassword) {
        if (BCrypt.checkpw(plainPassword, hashedPassword))
            return true;
        else
            return false;
    }
}
