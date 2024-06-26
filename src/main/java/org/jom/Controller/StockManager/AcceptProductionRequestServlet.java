package org.jom.Controller.StockManager;

import org.jom.Auth.JwtUtils;
import org.jom.Dao.EmployeeDAO;
import org.jom.Dao.ProductionDAO;
import org.jom.Dao.Supplier.Collection.CollectionDAO;
import org.jom.Dao.UserDAO;
import org.jom.Dao.YardDAO;
import org.jom.Model.EmployeeModel;
import org.jom.Model.ProductionModel;
import org.jom.Model.UserModel;
import org.jom.Model.YardModel;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/accept-production")
public class AcceptProductionRequestServlet extends HttpServlet {
    //Accept request
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get all cookies from the request
        Cookie[] cookies = request.getCookies();
        JwtUtils jwtUtils = new JwtUtils();

        if (!jwtUtils.CheckJWT(cookies)) {
            if (jwtUtils.CheckRefresh(cookies))
                response.addCookie(jwtUtils.getNewJWT(cookies));
            else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"message\": \"UnAuthorized\"}");
                return;
            }
        }

        // get auth payload data
        JSONObject jsonObject = jwtUtils.getAuthPayload();
        int user_id = (int) jsonObject.get("user");
        String role = (String) jsonObject.get("page");

        int request_id = Integer.parseInt(request.getParameter("id"));

        try {
            if (user_id != 0) {
                if (role.equals("stock-manager")) {

                    ProductionDAO productionDAO = new ProductionDAO();
                    ProductionModel productionModel = productionDAO.getProductionRequest(request_id);

                    YardDAO yardDAO = new YardDAO();
                    YardModel yardModel = yardDAO.getBlockData("yard" + productionModel.getYard(), productionModel.getBlock());
                    yardDAO.updateBlockAmount("yard" + productionModel.getYard(), yardModel.getCount() - productionModel.getAmount(), productionModel.getBlock());

                    if (productionDAO.acceptProductionRequestStatus(request_id, 2, yardModel.getDays())) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"message\": \"production request accepted\"}");
                        System.out.println("production request accepted");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"message\": \"Not accepted\"}");
                        System.out.println("Not accepted");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write("{\"message\": \"Invalid User\"}");
                    System.out.println("Invalid User");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"message\": \"Invalid User\"}");
                System.out.println("Invalid User");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }

    //Decline request
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get all cookies from the request
        Cookie[] cookies = request.getCookies();
        JwtUtils jwtUtils = new JwtUtils();

        if (!jwtUtils.CheckJWT(cookies)) {
            if (jwtUtils.CheckRefresh(cookies))
                response.addCookie(jwtUtils.getNewJWT(cookies));
            else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"message\": \"UnAuthorized\"}");
                return;
            }
        }

        // get auth payload data
        JSONObject jsonObject = jwtUtils.getAuthPayload();
        int user_id = (int) jsonObject.get("user");
        String role = (String) jsonObject.get("page");

        StringBuilder requestBody = new StringBuilder();

        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        JSONObject json_data = new JSONObject(requestBody.toString());
        int request_id = json_data.getInt("id");
        String reason = json_data.getString("reason");

        try {
            if (user_id != 0) {
                if (role.equals("stock-manager")) {

                    ProductionDAO productionDAO = new ProductionDAO();

                    if (productionDAO.rejectProductionRequest(request_id, reason)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"message\": \"production request rejected\"}");
                        System.out.println("production request rejected");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"message\": \"Not rejected\"}");
                        System.out.println("Not rejected");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write("{\"message\": \"Invalid User\"}");
                    System.out.println("Invalid User");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"message\": \"Invalid User\"}");
                System.out.println("Invalid User");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }
}
