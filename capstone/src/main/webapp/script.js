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

/** Add top navigation bar to the HTML */
const topNavBar = 
    '<div id="top-navigation">'
    + '<a href="profile.html">My Profile</a>'
    + '<a href="explore.html">Explore</a>'
    + '</div>';
document.write(topNavBar);

/** Load 'About Us' Club info tab. Default page displayed when user first enters club page. */
async function getClubInfo() {
  var params = new URLSearchParams(window.location.search);
  const response = await fetch('/clubs?name=' + params.get('name'));
  if (response.status == 400) {
    alert("Invalid club! Returning to Explore.");
    window.location.replace("explore.html");
  } else {
    if (params.get('is-invalid') == 'true') {
      alert('Unable to update officers list: no officer was a member of the club.');
    }
    const allInfo = await response.json()
    const clubInfo = allInfo.club;
    imageUrl = 'images/logo.png';
    if (clubInfo['logo'] != '') {
      imageUrl = await getImageUrl(clubInfo['logo']);
    }
    document.getElementById('club-logo-small').src = imageUrl;
    document.getElementById('club-name').innerHTML = clubInfo['name'];
    document.getElementById('description').innerHTML = clubInfo['description'];
    var officerList = document.getElementById('officers-list');
    var officers = clubInfo['officers'];
    for (const officer of officers) {
      officerList.innerHTML += '<li>' + officer + '</li>';
    }

    var labelsList = document.getElementById('labels');
    var labels = clubInfo['labels'];
    for (const label of labels) {
      labelsList.innerHTML += '<li>' + label + '</li>';
    }

    const membersElement = document.getElementById('members');
    if (clubInfo['members'].length == 1) {
      membersElement.innerHTML = 'There is 1 member in this club.';
    } else {
      membersElement.innerHTML = 'There are ' + clubInfo['members'].length + ' members in this club.';
    }
    document.getElementById('website').innerHTML = clubInfo['website'];
    if(clubInfo['isOfficer']) {
      document.getElementById('edit-button').style.display = 'inline-block';
      document.getElementById('delete-button').style.display = 'inline-block';
    }

    // Update join and interested buttons if needed
    const studentClubs = allInfo.studentClubs;
    const interestedClubs = allInfo.studentInterestedClubs;
    document.getElementsByClassName('join-button')[0].value = clubInfo['name'];
    document.getElementsByClassName('interested-join-button')[0].value = clubInfo['name'];
    editButton(clubInfo['name'], studentClubs, 'join-button');
    editButton(clubInfo['name'], interestedClubs, 'interested-join-button');
  }
}

/**Shows or hides the area to post announcements depending on if user is authorized. */
async function showHidePostAnnouncement () {
  var params = new URLSearchParams(window.location.search);
  const query = '/officer?name=' + params.get('name');
  const response = await fetch(query);
  const text = await response.text();
  if (JSON.parse(text)) {
    document.getElementById('post-announcement').removeAttribute('hidden');
    document.getElementById('scheduled-announcements').removeAttribute('hidden');
  }
}

/** Accesses and displays club announcement data from servlet. */
async function loadAnnouncements () {
  var params = new URLSearchParams(window.location.search);
  const query = '/announcements?name=' + params.get('name');
  const response = await fetch(query);
  const json = await response.json();
  const template = document.querySelector('#announcement-element');

  var backgroundColor;
  const color1 = '#AAA';
  const color2 = '#BBB';
  const editedFlag = ' (edited)';
  var evenOdd = true;
  for (var announcement of json) {
    const id = announcement.author + announcement.content + announcement.time; // Unique string to identiy this announcement.

    var pictureSrc;
    if (announcement.picture) {
        pictureSrc = await fetch('/get-image?blobKey=' + announcement.picture);
        pictureSrc = pictureSrc.url;
    } else {
        pictureSrc = 'images/profile.jpeg';
    }
    template.content.querySelector('img').src = pictureSrc;
    template.content.querySelector('.announcement-author').innerHTML = announcement.authorName;
    template.content.querySelector('.announcement-content').innerHTML = announcement.content;
    if (JSON.parse(announcement.edited)) {
        template.content.querySelector('.announcement-author').innerHTML += editedFlag;
    }
    template.content.querySelector('.announcement-content').id = id;

    const dateString = new Date(announcement.time).toLocaleDateString("en-US");
    const timeString = new Date(announcement.time).toLocaleTimeString("en-US");
    template.content.querySelector('.announcement-time').innerHTML = timeString + " on " + dateString;

    backgroundColor = evenOdd ? color1 : color2; // In order to switch background colors every announcement
    template.content.querySelector('#announcement-container').style.backgroundColor = backgroundColor;
    evenOdd = !evenOdd;

    if (JSON.parse(announcement.isAuthor)) {
      template.content.querySelector('.delete-announcement').style.display = 'inline-block';
      template.content.querySelector('.edit-announcement').style.display = 'inline-block';
      template.content.querySelector('.edit-announcement').id = id + '-edit';
      template.content.querySelector('.save-announcement').id = id + '-save';
      template.content.querySelector('.announcement-new-content').id = id + '-new';
      template.content.querySelector('.announcement-id').value = id;
      template.content.querySelector('.club').value = announcement.club;
      template.content.querySelector('.change-form').id = id + '-form';
      
      template.content.querySelector('#club').value = announcement.club;
      template.content.querySelector('#author').value = announcement.author;
      template.content.querySelector('#content').value = announcement.content;
      template.content.querySelector('#time').value = announcement.time;
    } else {
      template.content.querySelector('.delete-announcement').style = "display: none;";
      template.content.querySelector('.edit-announcement').style = "display: none;";
    }

    var clone = document.importNode(template.content, true);
    document.getElementById('announcements-display').appendChild(clone);

    if (JSON.parse(announcement.isAuthor)) {
      document.getElementById(id + '-edit').onclick = function () {
        allowEditAnnouncement(id);
      };
      document.getElementById(id + '-save').onclick = function () {
        saveAnnouncement(id);
      };
    }
  }
}

/** Accesses and displays club announcement data from servlet. */
async function loadScheduledAnnouncements() {
  var params = new URLSearchParams(window.location.search);
  const isOfficerQuery = '/officer?name=' + params.get('name');
  const isOfficerResponse = await fetch(isOfficerQuery);
  const isOfficer = await isOfficerResponse.text();
  if (!JSON.parse(isOfficer)) {
    return; // Not an officer, don't load scheduled announcements. Servlet won't give anything anyway. 
  }    

  const query = '/schedule-announcement?name=' + params.get('name');
  const response = await fetch(query);
  const json = await response.json();
  const template = document.querySelector('#announcement-element');

  var backgroundColor;
  const color1 = '#AAA';
  const color2 = '#BBB';
  var evenOdd = true;
  for (var announcement of json) {
    const pictureSrc = await fetch('/get-image?blobKey=' + announcement.picture);
    var pictureSrc;
    if (announcement.picture) {
        pictureSrc = await fetch('/get-image?blobKey=' + announcement.picture);
        pictureSrc = pictureSrc.url;
    } else {
        pictureSrc = 'images/profile.jpeg';
    }
    template.content.querySelector('img').src = pictureSrc;
    template.content.querySelector('.announcement-author').innerHTML = announcement.authorName;
    template.content.querySelector('.announcement-content').innerHTML = announcement.content;
    const dateString = new Date(announcement.time).toLocaleDateString("en-US");
    const timeString = new Date(announcement.time).toLocaleTimeString("en-US");
    template.content.querySelector('.announcement-time').innerHTML = timeString + " on " + dateString;

    backgroundColor = evenOdd ? color1 : color2; // In order to switch background colors every announcement
    template.content.querySelector('#announcement-container').style.backgroundColor = backgroundColor;
    evenOdd = !evenOdd;

    var clone = document.importNode(template.content, true);
    document.getElementById('scheduled-announcements').appendChild(clone);

  }
}

function allowEditAnnouncement (id) {
  document.getElementById(id).contentEditable = 'true';
  document.getElementById(id + '-edit').style = 'display: none;';
  document.getElementById(id + '-save').style.display = 'inline-block';
}

function saveAnnouncement (id) {
  const newContent = document.getElementById(id).innerHTML;
  document.getElementById(id + '-new').value = newContent;
  document.getElementById(id + '-form').submit();
}

async function loadCalendar () {
  var params = new URLSearchParams(window.location.search);
  const response = await fetch('/clubs?name=' + params.get('name'));
  const json = await response.json();

  // Check that calendar ID exists before updating iframe
  if (json['club']['calendar'].length != 0) {
    document.getElementById('calendar-element').src = "https://calendar.google.com/calendar/embed?src=" + json['club']['calendar'];
  }
  document.getElementById('club-name-cal').value = params.get('name');
}

/** Displays a certain tab for a club, by first checking for a GET parameter 
    that specifies which tab to load, then if that doesn't exist, loads a default tab.
    This should be used at the initial load for the about-us.html page and to redirect
    back to a specific tab. Similar to showTab(tabName), which displays the given tab. 
 */
function displayTab() {
  const params = new URLSearchParams(window.location.search);
  const tabToLoad = params.get('tab');
  const defaultTab = '#about-us';
  if (tabToLoad) {
    showTab('#' + tabToLoad);
  } else {
    showTab(defaultTab);
  }
}

/** Displays club info tab, depending on which tab is passed in. Similar to showTab(), where
    this one should be called with a specific tab to load.
*/
function showTab(tabName) {
  var template = document.querySelector(tabName);

  const params = new URLSearchParams(window.location.search);
  if (tabName === '#announcements') {
    template.content.querySelector('#club-name').value = params.get('name');
    template.content.querySelector('#schedule-club-name').value = params.get('name');
    template.content.querySelector('#timezone').value = Intl.DateTimeFormat().resolvedOptions().timeZone;
  }

  const node = document.importNode(template.content, true);
  document.getElementById('tab').innerHTML = '';
  document.getElementById('tab').appendChild(node);

  if (tabName === '#about-us') {
    getClubInfo();
  } else if (tabName === '#announcements') {
    getClubInfo();
    loadAnnouncements();
    showHidePostAnnouncement();
    loadScheduledAnnouncements();
  } else if (tabName === '#calendar') {
    loadCalendar();
  }
}

/** Fetches blobstore image upload url. */
async function fetchBlobstoreUrl() {
  fetch('/blobstore-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('logo-form');
        messageForm.action = imageUploadUrl;
      });
}

/** Displays status of club registration form submission. */
function getRegMessage() {
  var params = new URLSearchParams(window.location.search);
  var valid = params.get('is-valid');
  var template;
  if (valid == 'true') {
    template = document.querySelector('#valid');
  } else {
    template = document.querySelector('#invalid'); 
  }
  const node = document.importNode(template.content, true);
  document.body.appendChild(node);  
}

/** Make club info content editable when user enables editing. */
function showEdit() {
  document.getElementById('description').contentEditable = 'true';
  document.getElementById('website').contentEditable = 'true';
  document.getElementById('officers-list').contentEditable = 'true';
  document.getElementById('edit-button').style.display = 'none';
  document.getElementById('labels').contentEditable = 'true';
  document.getElementById('labels').innerHTML += '<li></li>';
  document.getElementById('edit-form').removeAttribute('hidden');
  document.getElementById('logo-form').removeAttribute('hidden');
  document.getElementById('logo-club-name').value = document.getElementById('club-name').innerHTML;
  fetchBlobstoreUrl();
}

/** Store edited content from club page */
function saveClubChanges() {
  const params = new URLSearchParams(window.location.search);

  const newDesc = document.getElementById("description").innerHTML;
  const newWebsite = document.getElementById("website").innerHTML;

  var newOfficers = [];
  const officerListElement = document.getElementById('officers-list');
  const officerList = officerListElement.getElementsByTagName('li');
  for (const officer of officerList) {
    newOfficers.push(officer.innerText);
  }
  
  var newLabels = [];
  const labelsListElement = document.getElementById('labels');
  const labelsList = labelsListElement.getElementsByTagName('li');
  for (const label of labelsList) {
    newLabels.push(label.innerText.replace(/\s+/g, '')); // Get rid of whitespaces
  }

  document.getElementById('new-desc').value = newDesc;
  document.getElementById('new-web').value = newWebsite;
  document.getElementById('new-officers').value = newOfficers;
  document.getElementById('new-labels').value = newLabels;
  document.getElementById('new-name').value = params.get('name');
  document.forms['edit-form'].submit();
  alert('Changes submitted!');
}

async function getImageUrl(logoKey) {
    return await fetch('/get-image?blobKey=' + logoKey)
        .then((pic) => {
          return pic.url;
        });
}

async function deleteClub() {
  if (window.confirm('Are you sure you want to delete your club? It will be gone forever :(')) {
    var clubName = document.getElementById('club-name').innerHTML;
    const response = await fetch('/delete-club?name=' + clubName, {method: 'POST'});
    window.alert('Your club has been deleted.');
    window.location.href = "/explore.html";
  }
}