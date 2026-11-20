package ua.fictionallibrary.digital_lib.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final JwtValidator jwtValidator;

    public JwtAuthFilter(AuthenticationEntryPoint authenticationEntryPoint, UserDetailsService userDetailsService, JwtValidator jwtValidator) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token for {} {}, continuing as anonymous", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            String username = jwtValidator.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (!userDetails.isEnabled())
                    throw new DisabledException("User " + username + " is deactivated");
                if (jwtValidator.isTokenValid(token, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated '{}' with authorities {} for {} {}",
                            username, userDetails.getAuthorities(), request.getMethod(), request.getRequestURI());
                } else {
                    log.debug("JWT for '{}' failed validation for {} {}, continuing as anonymous",
                            username, request.getMethod(), request.getRequestURI());
                }
            } else if (username == null) {
                log.debug("JWT without subject for {} {}, continuing as anonymous", request.getMethod(), request.getRequestURI());
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException | DisabledException e) {
            log.debug("Rejected JWT for {} {}: {} - {}",
                    request.getMethod(), request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Invalid or expired JWT", e));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
