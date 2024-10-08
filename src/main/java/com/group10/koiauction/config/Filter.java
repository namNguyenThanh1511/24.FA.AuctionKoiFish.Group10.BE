package com.group10.koiauction.config;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.exception.AuthenException;
import com.group10.koiauction.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class Filter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;


    @Autowired
    @Qualifier("handlerExceptionResolver")
    HandlerExceptionResolver handlerExceptionResolver;

    private final List<String> AUTH_PERMISSION = List.of( //những api mà ai cũng truy cập đc
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/login",
            "/api/register",
            "/api/register-member",
            "/api/forgot-password"
//            "/api/account/{id}",
//            "/api/account/update-profile/{id}",
//
    );

    public boolean checkIsPublicAPI(String uri) {
        //Nếu gặp những API như list trên -> cho phép truy cập luôn
        //ngược lại , phân quyền ( authorization) , check token
        AntPathMatcher matcher = new AntPathMatcher();// check pattern vs uri người dùng truy
        return AUTH_PERMISSION.stream().anyMatch(pattern ->  matcher.match(pattern, uri));// nếu match -> true ; else -> false

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //check before access controller
        //check xem api user that user request allow who can access ( có phải là 1 public api hay ko , ai cũng dùng đc )
        boolean isPublicAPI = checkIsPublicAPI(request.getRequestURI());
        if(isPublicAPI) {
            filterChain.doFilter(request, response); // cho phep truy cap luon
        }else {
            //nếu ko phải public api -> kiểm tra token
            String token = getTokenFromRequest(request);
            if(token == null){
                // ko dc phep truy cap
                handlerExceptionResolver.resolveException(request,response,null,new AuthenException("Token is missing"));
                return;
            }
            // => co' token
            // check xem token co' đúng hay ko -> lấy thông tin account từ token
            Account account;
            try{
                account = tokenService.getAccountByToken(token);
            }catch (ExpiredJwtException e){
                handlerExceptionResolver.resolveException(request,response,null,new AuthenException("Expired token"));
                return;
            }catch (MalformedJwtException e){
                //token sai dinh dang / format
                handlerExceptionResolver.resolveException(request,response,null,new AuthenException("Invalid token format"));
                return;
            }
            //=> token chuẩn -> cho phép truy cập , lưu lại thông tin của account này
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    account
                    , token
                    , account.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken); // Lưu thông tin user vào SecurityContext để biết chính xác đâu là thằng
            //token ok , cho truy cap
            filterChain.doFilter(request,response);
        }
    }
    public String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);  // Extract token after "Bearer "
    }
}
