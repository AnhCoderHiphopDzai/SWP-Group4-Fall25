/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;

import dal.UserDAO;
import entity.User;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@WebServlet(name = "VerifyEmailServlet", urlPatterns = {"/VerifyEmail"})
public class VerifyEmailServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * Forwards the user to the verification page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * Manages two actions: 'sendcode' and 'verify'.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // check user login chua
        if (user == null) {
            request.setAttribute("error", "You must be logged in to perform this action.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        String action = request.getParameter("action");

        if (action == null) {
            request.setAttribute("error", "Invalid action.");
            request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
            return;
        }

        // gui verify
        if (action.equals("sendcode")) {
            String newEmail = request.getParameter("newEmail");

            // check trung email nguoi khac khong
            User existingUser = userDAO.getUserByEmail(newEmail);
            if (existingUser != null && existingUser.getUserID() != user.getUserID()) {
                request.setAttribute("error", "This email is already in use by another account.");
                request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
                return;
            }

            // tao code 6 so
            String code = String.format("%06d", new Random().nextInt(999999));
            boolean emailSent = sendVerificationEmail(newEmail, code);

            if (emailSent) {
                session.setAttribute("verificationCode", code);
                session.setAttribute("verificationEmail", newEmail);
                session.setAttribute("verificationExpiry", System.currentTimeMillis() + 600000);
                request.setAttribute("emailSent", newEmail);
                request.setAttribute("message", "A verification code has been sent to " + newEmail + ". It will expire in 10 minutes.");
            } else {
                request.setAttribute("error", "Failed to send verification email. Please check the email address or try again later.");
            }

            request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
        
        // verify code
        } else if (action.equals("verify")) {
            String formCode = request.getParameter("verificationCode");
            String savedCode = (String) session.getAttribute("verificationCode");
            String savedEmail = (String) session.getAttribute("verificationEmail");
            Long expiryTime = (Long) session.getAttribute("verificationExpiry");

            // Check data session
            if (savedCode == null || savedEmail == null || expiryTime == null) {
                request.setAttribute("error", "Your verification session has expired. Please request a new code.");
                request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
                return;
            }

            // Check xem expire chua
            if (System.currentTimeMillis() > expiryTime) {
                request.setAttribute("error", "Your verification code has expired. Please request a new one.");
                session.removeAttribute("verificationCode");
                session.removeAttribute("verificationEmail");
                session.removeAttribute("verificationExpiry");
                request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
                return;
            }

            // check code dung chua
            if (formCode.equals(savedCode)) {
                boolean success = userDAO.updateEmailAndVerification(user.getUserID(), savedEmail, true);
                if (success) {
                    user.setEmail(savedEmail);
                    user.setIsEmailVerified(true);
                    session.setAttribute("user", user);
                    request.setAttribute("message", "Email successfully verified and updated!");
                    session.removeAttribute("verificationCode");
                    session.removeAttribute("verificationEmail");
                    session.removeAttribute("verificationExpiry");
                } else {
                    request.setAttribute("error", "A database error occurred. Could not update email.");
                    request.setAttribute("emailSent", savedEmail);
                }

            } else {
                request.setAttribute("error", "Invalid verification code. Please try again.");
                request.setAttribute("emailSent", savedEmail);
            }
            
            request.getRequestDispatcher("verifyEmail.jsp").forward(request, response);
        }
    }

    private boolean sendVerificationEmail(String toEmail, String code) {
        final String fromEmail = "justaclonecc@gmail.com"; 
        final String password = "zkmg qcqk vvup kyad"; 
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587"); // TLS Port
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        Session session = Session.getInstance(props, auth);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject("Your Cinema Management Verification Code");
            
            String emailBody = "Hello,\n\n"
                    + "Your verification code is: " + code + "\n\n"
                    + "This code will expire in 10 minutes.\n\n"
                    + "Thank you,\n"
                    + "Cinema Management Team";
            
            msg.setText(emailBody);

            Transport.send(msg);
            return true; 
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}