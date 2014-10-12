/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.partlycloudy.swishswish;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter which ensures that prevents unauthenticated users from accessing the
 * web app
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class AuthFilter implements Filter {
  private static final Logger LOG = Logger.getLogger(AuthFilter.class.getSimpleName());

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      // skip auth for static content, middle of auth flow, notify servlet
      if (httpRequest.getRequestURI().startsWith("/static") ||
          httpRequest.getRequestURI().equals("/oauth2callback") ||
          httpRequest.getRequestURI().equals("/salesforce") ||
          httpRequest.getRequestURI().equals("/notify2")||
          httpRequest.getRequestURI().equals("/notify")) {
        LOG.info("Skipping auth check during auth flow");
        filterChain.doFilter(request, response);
        return;
      }

      LOG.fine("Checking to see if anyone is logged in");
      if (AuthUtil.getUserId(httpRequest) == null
          || AuthUtil.getCredential(AuthUtil.getUserId(httpRequest)) == null
          || AuthUtil.getCredential(AuthUtil.getUserId(httpRequest)).getAccessToken() == null) {
        // redirect to auth flow
        httpResponse.sendRedirect(WebUtil.buildUrl(httpRequest, "/oauth2callback"));
        return;
      }

      // Things checked out OK :)
      filterChain.doFilter(request, response);
    } else {
      LOG.warning("Unexpected non HTTP servlet response. Proceeding anyway.");
      filterChain.doFilter(request, response);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }
}
