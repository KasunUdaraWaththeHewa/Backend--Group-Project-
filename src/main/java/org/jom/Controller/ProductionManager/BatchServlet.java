package org.jom.Controller.ProductionManager;

import com.google.gson.Gson;
import org.jom.Auth.JwtUtils;
import org.jom.Dao.BatchDAO;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/batch")
public class BatchServlet extends HttpServlet {
    //send a batch
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

        int batch_id = Integer.parseInt(request.getParameter("id"));

        try {
            if (user_id != 0) {
                if (role.equals("production-manager") || role.equals("sales-manager")) {

                    BatchDAO batchDAO = new BatchDAO();
                    BatchModel batchModel = batchDAO.getBatch(batch_id);

                    List<Integer> idList = Arrays.stream(batchModel.getProducts().split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());

                    ProductsDAO productsDAO = new ProductsDAO();
                    List<ProductModel> products = productsDAO.getProductionProducts(idList);

                    Gson gson = new Gson();
                    String object = gson.toJson(batchModel);
                    String objectArray = gson.toJson(products);

                    if (batchModel.getId() != 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"batch\": " + object + ",\"products\":" + objectArray + "}");
                        System.out.println("Send batch");
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        out.write("{\"message\": \"No Batch\"}");
                        System.out.println("No batch");
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

    // Complete production batch
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
        try {
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            JSONObject json_data = new JSONObject(requestBody.toString());

            // Retrieve arrays as JSONArrays
            JSONArray countArray = json_data.getJSONArray("count");
            int batch_id = json_data.getInt("id");

            // Convert JSONArrays to String arrays
            int[] products_count = new int[countArray.length()];
            int[] distribution_count = new int[countArray.length()];

            for (int i = 0; i < countArray.length(); i++) {
                products_count[i] = countArray.getInt(i);
                distribution_count[i] = 0;
            }

            String products_count_string = intArrayToString(products_count);
            String distribution_count_string = intArrayToString(distribution_count);

            if (user_id != 0) {
                if (role.equals("production-manager")) {

                    BatchDAO batchDAO = new BatchDAO();
                    if (batchDAO.completeBatch(batch_id, products_count_string, distribution_count_string)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.write("{\"message\": \"Production batch completed successfully\"}");
                        System.out.println("Production batch completed successfully");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write("{\"message\": \"Production batch is not completed\"}");
                        System.out.println("Production batch is not completed");
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
