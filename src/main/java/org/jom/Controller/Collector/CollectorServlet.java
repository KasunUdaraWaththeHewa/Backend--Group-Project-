package org.jom.Controller.Collector;

import com.google.gson.Gson;
import org.jom.Auth.JwtUtils;
import org.jom.Dao.CocoRateDAO;
import org.jom.Dao.EmployeeDAO;
import org.jom.Dao.Supplier.Collection.SupplyDAO;
import org.jom.Model.CocoModel;
import org.jom.Model.Collection.SupplyModel;
import org.jom.Model.EmployeeModel;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebServlet("/collector")
public class CollectorServlet extends HttpServlet {
    //Get collector dashboard data
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
        int employee_id = (int) jsonObject.get("sId");
        String role = (String) jsonObject.get("page");

        try {
            if (employee_id != 0) {
                if (role.equals("collector")) {

                    Date currentDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);

                    calendar.add(Calendar.DAY_OF_MONTH, 2);
                    Date nextDay = calendar.getTime();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    String today = dateFormat.format(currentDate);
                    String day_after_tomorrow = dateFormat.format(nextDay);

                    SupplyDAO supplyDAO = new SupplyDAO();

                    List<SupplyModel> today_collections = supplyDAO.getCollectionByDay(employee_id, today);
                    List<SupplyModel> missed_collections = supplyDAO.getMissedCollections(employee_id, today);
                    List<SupplyModel> upcoming_collections = supplyDAO.getUpcomingCollections(employee_id, today, day_after_tomorrow);
                    int today_count = supplyDAO.getCollectionCount(employee_id, today);

                    CocoRateDAO cocoRateDAO = new CocoRateDAO();
                    CocoModel cocoRate = cocoRateDAO.getLastRecord();

                    Gson gson = new Gson();

                    String today_collec = gson.toJson(today_collections); // Object array to json
                    String missed_collec = gson.toJson(missed_collections); // Object array to json
                    String upcoming_collec = gson.toJson(upcoming_collections); // Object array to json
                    String object = gson.toJson(cocoRate);

                    if (today_collections.size() != 0 && upcoming_collections.size() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"size\": " + today_collections.size() + ",\"today\":" + today_collec + ",\"upcoming\":" + upcoming_collec + ",\"count\":" + today_count + ",\"rate\": " + object + ",\"missed\":" + missed_collec + "}");
                        System.out.println("Collector dashboard tables contents");
                    } else if (today_collections.size() == 0 && upcoming_collections.size() == 0) {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"size\": \"-2\",\"count\":" + today_count + ",\"rate\":" + object + ",\"missed\":" + missed_collec + "}");
                        System.out.println("No collections");
                    } else if (today_collections.size() == 0) {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"size\": \"-1\",\"upcoming\":" + upcoming_collec + ",\"count\":" + today_count + ",\"rate\": " + object + ",\"missed\":" + missed_collec + "}");
                        System.out.println("No collections today");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"size\": " + today_collections.size() + ",\"today\":" + today_collec + ",\"count\":" + today_count + ",\"rate\": " + object + ",\"missed\":" + missed_collec + "}");
                        System.out.println("No upcoming collections");
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

    // get all past collections
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        int employee_id = (int) jsonObject.get("sId");
        String role = (String) jsonObject.get("page");

        try {
            if (employee_id != 0) {
                if (role.equals("collector")) {
                    SupplyDAO supplyDAO = new SupplyDAO();

                    List<SupplyModel> past_collections = supplyDAO.getAllPastCollections(employee_id);

                    Gson gson = new Gson();
                    String past = gson.toJson(past_collections); // Object array to json

                    if (past_collections.size() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"list\": " + past + "}");
                        System.out.println("Send all past collections");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"message\": \"No past collections\"}");
                        System.out.println("No past collections");
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

    // get all upcoming collections
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
        int employee_id = (int) jsonObject.get("sId");
        String role = (String) jsonObject.get("page");

        try {
            if (employee_id != 0) {
                if (role.equals("collector")) {
                    SupplyDAO supplyDAO = new SupplyDAO();

                    List<SupplyModel> upcoming_collections = supplyDAO.getAllUpcomingCollections(employee_id);

                    Gson gson = new Gson();
                    String upcoming = gson.toJson(upcoming_collections); // Object array to json

                    if (upcoming_collections.size() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"list\": " + upcoming + "}");
                        System.out.println("Send all upcoming collections");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"message\": \"No upcoming collections\"}");
                        System.out.println("No upcoming collections");
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
