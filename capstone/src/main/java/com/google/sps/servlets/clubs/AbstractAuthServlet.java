package com.google.sps.servlets;
 
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.sps.util.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.util.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.CalendarList;
import com.google.gson.Gson;
import com.google.api.services.calendar.model.CalendarListEntry;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;   
import com.google.sps.util.*;
import com.google.sps.progress.*;
import com.google.gson.reflect.TypeToken;



// AbstractAuthServlet initializes the OAuth process. 
@WebServlet("/abstract")
public class AbstractAuthServlet extends AbstractAppEngineAuthorizationCodeServlet {
  String nickname = UserServiceFactory.getUserService().getCurrentUser().getNickname();
  String APPLICATION_NAME = "google.com:clubhub-step-2020";
  Calendar calendar;
  Gson gson = new Gson();
//   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/YYYY");  
//   LocalDateTime now = LocalDateTime.now();  

  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    response.setContentType("application/json;");
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();
    // Build calendar. 
    Credential credential = Utils.newFlow().loadCredential(userId);
    calendar = new Calendar.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build();

    com.google.api.services.calendar.Calendar.CalendarList.List listRequest = calendar.calendarList().list();
    listRequest.setFields("items(id)").setMaxResults(1);
    CalendarList feed = listRequest.execute();
   
    ArrayList<String> result = new ArrayList<String>();
      if (feed.getItems() != null) {
        for (CalendarListEntry entry : feed.getItems()) {
          result.add(entry.getId());
        }
      }    
  }
 
  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }
 
  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }
  
  public List<Event> getEventsInTimespan(DateTime minSpan, DateTime maxSpan) throws IOException{
    Events events = calendar.events().list("primary")
                .setTimeMin(minSpan)
                .setTimeMax(maxSpan)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
    List<Event> items = events.getItems();
    return items;
  }

  // Method for putting an event on the user's calendar. 
  public void insertEvent(Event event) throws IOException{
    Event myNewEvent = calendar.events().insert("primary", event).execute();
  }
}
