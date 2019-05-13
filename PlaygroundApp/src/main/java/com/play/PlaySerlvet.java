package com.play;

import com.play.database.DatabaseManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/foundation", "/play"})
public class PlaySerlvet extends HttpServlet {

    private DatabaseManager dbManager;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        dbManager = new DatabaseManager(getServletContext());
        String path = request.getServletPath();

        switch (path) {
            case "/play":
                break;
            case "/foundation":
                RequestDispatcher view = request.getRequestDispatcher("foundation.html");
                view.forward(request, response);
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}