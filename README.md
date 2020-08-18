# ClubHub: Google Student Training in Engineering Program (STEP) Capstone Project
Capstone Project by Megan Shi, Megha Kak, and Kevin Shao.

## Overview
The high-level goal of ClubHub is to ease access to resources and information provided 
by student organizations on college campuses and their officers. Many student organizations face 
difficulty in spreading awareness of their club’s goals and events they host. Frequently, officers 
are required to post on multiple social media platforms, such as Facebook, Instagram, Gmail, 
and many more in order to get a portion of their membership population to attend an event. 

ClubHub provides a central location for all student organizations to be listed, and for students and club
officers to interact smoothly. To learn about a club, or just to explore new clubs, students need only navigate 
to the web app, sign in (with Google, of course), and get started!

## Features
1. Explore page

The landing page for signed-in users is the explore page, where all clubs are listed. The explore page 
provides several options for viewing the clubs (as they will surely grow over time). The user may sort 
the clubs chronologically, alphabetically, and by size (number of members), as well as filter by labels 
which the clubs tag themselves with. By doing so, anyone can quickly and easily find a club they're looking for, 
or a club they didn't even know existed. If still unsatisfied, they can create their own. 

2. Club page

The club page has 3 panels: an information page, an announcements page, and a calendar page. The information page 
gives basic information about the clubs (description, size, labels, officers, etc.), and the announcements page shows 
recent announcements made by the club. Officers may edit information, post and edit announcements as they please. 
The calendar page displays the club calendar, which shows upcoming club events. 

3. Profile page

On each user's profile page, you can view and edit your personal information, such as class year, major, 
and screen name. Moreover, all clubs that the user is part of are listed, along with any recent announcements 
made by these clubs. As such, the profile page becomes an extremely convenient dashboard page, where you can quickly 
aggregate all upcoming events and announcements. 

4. Gmail integration

No webapp is complete without integrating with your everyday life. With Gmail integration, ClubHub can send notifications 
to each user via their email, such as when they first register, when events are about to happen, and when new announcements 
are made. As such, you don't even have to log in to ClubHub to be reminded of the next big meeting!


## Tools and Technologies
Some of the tools that will be used to build this webapp are:
* HTML
* CSS
* Javascript
* Java

## APIs
The following APIs were used: 
* Datastore
* Blobstore
* Google Authentication
* Gmail API
* Google Calendar API

## Deploying the project
To deploy the main project
1. Install maven (https://maven.apache.org/install.html)
2. Clone the project (`git clone https://github.com/googleinterns/step179-2020`)
3. Navigate to the capstone directory (`cd step179-2020/capstone`).
4. Modify pom.xml to contain the GCP project ID.
5. Run `mvn package appengine:deploy` to deploy the project live or 
`mvn package appengine:run` to run the project on the local server.

## License
This code is licensed under the Apache 2.0 License.