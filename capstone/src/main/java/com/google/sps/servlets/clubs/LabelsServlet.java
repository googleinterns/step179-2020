package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
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

    Query query = new Query(Constants.CLUB_ENTITY_PROP);
    PreparedQuery prepared = datastore.prepare(query);

    Streams.stream(prepared.asIterable())
        .flatMap(entity -> ServletUtil.getPropertyList(entity, Constants.LABELS_PROP).stream())
        .forEach(
            label -> {
              if (labelFrequency.keySet().contains(label)) {
                labelFrequency.put(label, labelFrequency.get(label) + 1);
              } else {
                labelFrequency.put(label, 1);
              }
            });

    ImmutableList<String> labels =
        labelFrequency.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .map(Map.Entry::getKey)
            .collect(toImmutableList());

    Gson gson = new Gson();
    String json = gson.toJson(labels);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
