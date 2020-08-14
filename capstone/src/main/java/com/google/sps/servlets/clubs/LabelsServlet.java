package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/labels")
public class LabelsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Map<String, Integer> labelFrequency = new HashMap<String, Integer>();
    List<String> labels = new ArrayList<String>();

    Query query = new Query(Constants.CLUB_ENTITY_PROP);
    PreparedQuery prepared = datastore.prepare(query);

    for (Entity entity : prepared.asIterable()) {
      for (String label : ServletUtil.getPropertyList(entity, Constants.LABELS_PROP)) {
        if (labelFrequency.keySet().contains(label)) {
          labelFrequency.put(label, labelFrequency.get(label) + 1);
        } else {
          labelFrequency.put(label, 1);
        }
      }
    }

    labelFrequency.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .forEachOrdered(x -> labels.add(x.getKey()));

    Gson gson = new Gson();
    String json = gson.toJson(labels);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
