package org.jom.Controller;

import com.google.gson.Gson;
import org.jom.Model.LoginModel;
import org.jom.Model.UserModel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/signin")
public class LoginServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            Gson gson = new Gson();
            // json data to user object
            BufferedReader bufferedReader = request.getReader();
            LoginModel login = gson.fromJson(bufferedReader, LoginModel.class);
            UserModel user = login.getUser();

            System.out.println(login.getUsername());
            System.out.println(login.getPassword());
            System.out.println();
            System.out.println(user.getId());
//            System.out.println(user.getFirst_name());
//            System.out.println(user.getLast_name());
            System.out.println(user.getEmail());
            System.out.println(user.getPassword());
//            System.out.println(user.getPhone());
//            System.out.println(user.getAdd_line_1());
//            System.out.println(user.getAdd_line_2());
//            System.out.println(user.getAdd_line_3());

            if(user.getId() != 0){
                response.setStatus(HttpServletResponse.SC_OK);
                if(user.getPassword().equals(login.getPassword())) {
                    out.write("{\"message\": \"Login successfully\"}");
                    System.out.println("Login successful");
                }else{
                    out.write("{\"message\": \"Wrong Password\"}");
                    System.out.println("Wrong password");
                }
            }else{
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\": \"Invalid Email\"}");
                System.out.println("Login incorrect");
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }
}
