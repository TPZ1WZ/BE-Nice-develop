//package com.proj.webprojrct.config;
//
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.web.servlet.error.ErrorController;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@ConditionalOnProperty(name = "app.error.custom-enabled", havingValue = "true")
//@Controller
//public class CustomErrorController implements ErrorController {
//
//    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Map<String, Object>> handleErrorJson(HttpServletRequest request) {
//        // ✅ XỬ LÝ LỖI CHO API (trả JSON)
//        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
//        String path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) != null
//            ? request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString()
//            : request.getRequestURI();
//
//        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
//
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("status", statusCode);
//        errorResponse.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
//        errorResponse.put("path", path);
//
//        // Custom error messages
//        String errorMessage;
//        switch (statusCode) {
//            case 401:
//                errorMessage = "Vui lòng đăng nhập để tiếp tục";
//                break;
//            case 403:
//                errorMessage = "Bạn không có quyền truy cập";
//                break;
//            case 404:
//                errorMessage = "Không tìm thấy tài nguyên";
//                break;
//            case 500:
//                errorMessage = "Lỗi máy chủ nội bộ";
//                break;
//            default:
//                errorMessage = message != null ? message.toString() : "Đã xảy ra lỗi";
//        }
//        errorResponse.put("message", errorMessage);
//
//        System.out.println("🔴 API Error: " + statusCode + " - " + errorMessage + " - Path: " + path);
//
//        return ResponseEntity.status(statusCode).body(errorResponse);
//    }
//
//    @RequestMapping(value = "/error", produces = MediaType.TEXT_HTML_VALUE)
//    public String handleErrorHtml(HttpServletRequest request, Model model) {
//        // ✅ XỬ LÝ LỖI CHO BROWSER (trả HTML)
//        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//
//        if (status != null) {
//            int statusCode = Integer.parseInt(status.toString());
//            model.addAttribute("statusCode", statusCode);
//
//            switch (statusCode) {
//                case 401:
//                    model.addAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục");
//                    break;
//                case 403:
//                    model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này");
//                    break;
//                case 404:
//                    model.addAttribute("errorMessage", "Không tìm thấy trang này");
//                    break;
//                case 500:
//                    model.addAttribute("errorMessage", "Lỗi máy chủ nội bộ");
//                    break;
//                default:
//                    model.addAttribute("errorMessage", "Đã xảy ra lỗi");
//            }
//        }
//
//        System.out.println("🌐 HTML Error Page: Status = " + (status != null ? status : "unknown"));
//
//        return "error"; // Render error.html (Thymeleaf)
//    }
//}