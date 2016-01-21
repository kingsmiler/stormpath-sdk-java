package com.stormpath.spring.filter;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.sdk.servlet.filter.HttpFilter;
import com.stormpath.spring.security.token.ThirdPartyAuthenticationToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Fix for https://github.com/stormpath/stormpath-sdk-java/issues/450
 *
 * @since 1.0.RC.8.1
 **/
@Component
public class SpringSecurityResolvedAccountFilter extends HttpFilter implements InitializingBean {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    protected void filter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws Exception {

        Account account = AccountResolver.INSTANCE.getAccount(request);

        if (account != null) {
            Authentication authentication = new ThirdPartyAuthenticationToken(account);
            authentication = authenticationManager.authenticate(authentication);
            SecurityContextHolder.clearContext();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(authenticationManager != null, "AuthenticationManager is required");
    }
}
