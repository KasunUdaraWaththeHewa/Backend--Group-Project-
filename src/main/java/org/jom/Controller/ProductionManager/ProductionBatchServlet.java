package org.jom.Controller.ProductionManager;

import com.google.gson.Gson;
import org.jom.Auth.JwtUtils;
import org.jom.Dao.ProductionDAO;
import org.jom.Dao.ProductsDAO;
import org.jom.Dao.UserDAO;
import org.jom.Model.BatchModel;
import org.jom.Model.ProductModel;
import org.jom.Model.ProductionModel;
import org.jom.Model.UserModel;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/production-batch")
public class ProductionBatchServlet extends HttpServlet {
    //send start data to new production
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

        try {
            if (user_id != 0) {
                if (role.equals("production-manager")) {

                    ProductionDAO productionDAO = new ProductionDAO();
                    ProductsDAO productsDAO = new ProductsDAO();
                    List<ProductionModel> productionModels = productionDAO.getAllAcceptedRequests();
                    List<ProductModel> products = productsDAO.getProducts();

                    Gson gson = new Gson();
                    String object = gson.toJson(productionModels);
                    String objectArray = gson.toJson(products);

                    if (productionModels.size() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"accepted\": " + object + ",\"products\":" + objectArray + "}");
                        System.out.println("Send production request");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"accepted\": \"No production request\",\"products\":" + objectArray + "}");
                        System.out.println("No production request");
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

    // Add new production batch
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

        StringBuilder requestBody = new StringBuilder();
        try {
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            JSONObject json_data = new JSONObject(requestBody.toString());

            // Retrieve arrays as JSONArrays
            JSONArray requestsArray = json_data.getJSONArray("requests");
            JSONArray amountsArray = json_data.getJSONArray("amounts");
            JSONArray productsArray = json_data.getJSONArray("products");
            JSONArray actualArray = json_data.getJSONArray("actual");
            JSONArray daysArray = json_data.getJSONArray("days");

            // Convert JSONArrays to String arrays
            int[] requests = new int[requestsArray.length()];
            int[] amounts = new int[requestsArray.length()];
            int[] actual = new int[requestsArray.length()];
            int[] days = new int[requestsArray.length()];
            int[] products = new int[productsArray.length()];
            int total_amount = 0;

            ProductionDAO productionDAO = new ProductionDAO();

            for (int i = 0; i < requestsArray.length(); i++) {
                requests[i] = requestsArray.getInt(i);
                days[i] = daysArray.getInt(i);
                amounts[i] = amountsArray.getInt(i);
                actual[i] = actualArray.getInt(i);
                if (amounts[i] < actual[i]) {
                    actual[i] = actual[i] - amounts[i];
                    productionDAO.updateActualAmount(requests[i], actual[i]);
                } else if (amounts[i] == actual[i]) {
                    actual[i] = 0;
                    productionDAO.updateActualAmount(requests[i], actual[i]);
                    productionDAO.updateProductionRequestStatus(requests[i], 4);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"message\": \"Production batch is not created\"}");
                    System.out.println("Production batch is not created");
                    return;
                }
                total_amount += amounts[i];
            }

            for (int i = 0; i < productsArray.length(); i++) {
                products[i] = productsArray.getInt(i);
            }

            String requests_string = intArrayToString(requests);
            String amounts_string = intArrayToString(amounts);
            String days_string = intArrayToString(days);
            String products_string = intArrayToString(products);

            BatchModel batchModel = new BatchModel(total_amount, amounts_string, requests_string, products_string, days_string);

            if (user_id != 0) {
                if (role.equals("production-manager")) {
                    if (batchModel.createBatch() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"message\": \"Production batch created successfully\"}");
                        System.out.println("Production batch created successfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"message\": \"Production batch is not created\"}");
                        System.out.println("Production batch is not created");
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

    public static String intArrayToString(int[] intArray) {
        StringBuilder resultBuilder = new StringBuilder();

        for (int i = 0; i < intArray.length; i++) {
            resultBuilder.append(intArray[i]);

            if (i < intArray.length - 1) {
                resultBuilder.append(",");
            }
        }

        return resultBuilder.toString();
    }
}
