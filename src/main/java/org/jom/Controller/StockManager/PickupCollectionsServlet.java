package org.jom.Controller.StockManager;

import com.google.gson.Gson;
import org.jom.Auth.JwtUtils;
import org.jom.Dao.CollectorDAO;
import org.jom.Dao.EmployeeDAO;
import org.jom.Dao.Supplier.Collection.CollectionDAO;
import org.jom.Dao.Supplier.Collection.PickupDAO;
import org.jom.Dao.Supplier.Collection.SupplyDAO;
import org.jom.Dao.UserDAO;
import org.jom.Model.Collection.SupplyModel;
import org.jom.Model.EmployeeModel;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebServlet("/pickup-collections")
public class PickupCollectionsServlet extends HttpServlet {
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

        int collector = Integer.parseInt(request.getParameter("id"));
        String date = request.getParameter("date");

        try {
            if (user_id != 0) {
                if (role.equals("stock-manager")) {

                    ArrayList<YardModel> collections_count = new ArrayList<>();

                    Date currentDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);

                    // Get the first day of the month
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    Date firstDayOfMonth = calendar.getTime();

                    // Get the last day of the month
                    calendar.add(Calendar.MONTH, 1);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    Date lastDayOfMonth = calendar.getTime();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    // Loop through all days of the month
                    calendar.setTime(firstDayOfMonth);

                    SupplyDAO supplyDAO = new SupplyDAO();

                    while (calendar.getTime().before(lastDayOfMonth) || calendar.getTime().equals(lastDayOfMonth)) {
                        String currentDay = dateFormat.format(calendar.getTime());

                        int count_of_date = supplyDAO.getCollectorCollectionsCount(collector, currentDay);

                        YardModel object = new YardModel(count_of_date, currentDay);
                        collections_count.add(object);

                        // Move to the next day
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    List<SupplyModel> collections = supplyDAO.getAllCollectionByDate(collector, date);

                    Gson gson = new Gson();
                    String object = gson.toJson(collections); // Object array to json
                    String collection_count_array = gson.toJson(collections_count);

                    if (collections.size() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"collections\": " + object + ",\"calender\":" + collection_count_array + "}");
                        System.out.println("Send collections");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"collections\": \"No collections\",\"calender\":" + collection_count_array + "}");
                        System.out.println("No collections");
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
