// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.collect.Streams;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final String COMMENT_PROP = "comment";
  private static final String DISPLAY_PROP = "name";
  private static final String TIME_PROP = "timestamp";
  private static final String EMAIL_PROP = "email";
  private static final String MAX_COMMENT_PROP = "max-comments";
  private static final String DEFAULT_VAL = "";

   @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort(TIME_PROP, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    int max = Integer.parseInt(request.getParameter(MAX_COMMENT_PROP));
    ImmutableList<Comment> comments = 
        Streams.stream(results.asIterable())
            .map(DataServlet::makeComment)
            .limit(max)
            .collect(toImmutableList());
            
    response.setContentType("application/json;");
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    String email = userService.getCurrentUser().getEmail();
    String commentText = getParameter(request, COMMENT_PROP, DEFAULT_VAL);
    String displayName = getParameter(request, DISPLAY_PROP, DEFAULT_VAL);
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty(COMMENT_PROP, commentText);
    commentEntity.setProperty(DISPLAY_PROP, displayName);
    commentEntity.setProperty(TIME_PROP, timestamp);
    commentEntity.setProperty(EMAIL_PROP, email);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }

    /**
   * Returns value of request parameter name, or a default value if not specified.
   * @param request -- client comment servlet request
   * @param name -- parameter of servlet request to return
   * @param defaultValue -- value to return if not specified by client
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client.
   */
  private static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.equals(DEFAULT_VAL)) {
      return defaultValue;
    }
    return value;
  }

  private static Comment makeComment(Entity ent) {
    Comment comment = new Comment(ent.getProperty(COMMENT_PROP).toString(), 
                                  ent.getProperty(DISPLAY_PROP).toString(),
                                  ent.getProperty(EMAIL_PROP).toString());
    return comment;
  }

  /* Holds all data for a single comment. */
  private static class Comment {
    private String commentText;
    private String displayName;
    private String email;

    public Comment(String commentText, String displayName, String email) {
      this.commentText = commentText;
      this.displayName = displayName;
      this.email = email;
    }
  }
}  
