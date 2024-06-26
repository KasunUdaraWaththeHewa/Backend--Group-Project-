package org.jom.Controller.StockManager;

import com.google.gson.Gson;
import org.jom.Auth.JwtUtils;
import org.jom.Dao.CocoRateDAO;
import org.jom.Dao.UserDAO;
import org.jom.Model.CocoModel;
import org.jom.Model.OutletModel;
import org.jom.Model.UserModel;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebServlet("/coco-rate")
public class CocoRateServlet extends HttpServlet {
    // Add or update rate
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        try {
            StringBuilder requestBody = new StringBuilder();

            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            JSONObject json_data = new JSONObject(requestBody.toString());
            String price = json_data.getString("price");

            if (user_id != 0) {
                if (role.equals("stock-manager")) {
                    Date currentDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String today = dateFormat.format(currentDate);

                    CocoModel cocoModel = new CocoModel(today, price);

                    CocoRateDAO cocoRateDAO = new CocoRateDAO();
                    CocoModel cocoRate = cocoRateDAO.getLastRecord();

                    if (cocoRate.getDate().equals(today)) {
                        if (cocoRateDAO.updateRate(cocoModel)) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.write("{\"message\": \"Coco rate updated successfully\"}");
                            System.out.println("Coco rate updated successfully");
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.write("{\"message\": \"Coco rate is not updated\"}");
                            System.out.println("Coco rate is not updated");
                        }
                    } else {
                        if (cocoRateDAO.addRate(cocoModel) != 0) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.write("{\"message\": \"Coco rate added successfully\"}");
                            System.out.println("Coco rate added successfully");
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.write("{\"message\": \"Coco rate is not added\"}");
                            System.out.println("Coco rate is not added");
                        }
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

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
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

            if (user_id != 0) {
                if (role.equals("stock-manager")) {

                    CocoRateDAO cocoRateDAO = new CocoRateDAO();
                    CocoModel cocoRate = cocoRateDAO.getLastRecord();
                    List<CocoModel> last_seven_cocoRate = cocoRateDAO.getLastSevenRecords();

                    if (cocoRate.getId() != 0) {
                        Gson gson = new Gson();
                        String object = gson.toJson(cocoRate); // Object array to json
                        String cocoRate_array = gson.toJson(last_seven_cocoRate);

                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"rate\": " + object + ",\"last_seven\":" + cocoRate_array + "}");
                        System.out.println("Send rate");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"rate\": \"No rates\"}");
                        System.out.println("No rates");
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
