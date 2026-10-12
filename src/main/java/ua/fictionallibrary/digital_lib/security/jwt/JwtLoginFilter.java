package ua.fictionallibrary.digital_lib.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.security.AuthResponse;
import ua.fictionallibrary.digital_lib.security.LoginUserRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtService jwtService;
    private final ObjectMapper mapper;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    public JwtLoginFilter(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ObjectMapper objectMapper,
            AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.mapper = objectMapper;
        this.authenticationEntryPoint = authenticationEntryPoint;
        setRequiresAuthenticationRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/login"));
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            throw new AuthenticationServiceException("Unsupported method: " + request.getMethod());
        }
        try {
            LoginUserRequest loginRequest = mapper.readValue(request.getInputStream(), LoginUserRequest.class);
            if (loginRequest == null || loginRequest.username() == null || loginRequest.password() == null) {
                throw new BadCredentialsException("Not found username or/and password");
            }
            var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                    loginRequest.username().trim(), loginRequest.password());
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        long expiresInSeconds = jwtService.getExpirationInSeconds();
        AuthResponse authResponse = new AuthResponse(token, "Bearer", expiresInSeconds);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(response.getWriter(), authResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, failed);
    }
}
