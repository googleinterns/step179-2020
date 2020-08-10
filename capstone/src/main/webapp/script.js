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
    + '<a href="index.html">Explore</a>'
    + '</div>';
document.write(topNavBar);

/** Load 'About Us' Club info tab. Default page displayed when user first enters club page. */
async function getClubInfo() {
  var params = new URLSearchParams(window.location.search);
  const response = await fetch('/clubs?name=' + params.get('name'));
  if (response.status == 400) {
    alert("Invalid club! Returning to Explore.");
    window.location.replace("index.html");
  } else {
    if (params.get('is-invalid') == 'true') {
      alert('Unable to update officers list: no officer was a member of the club.');
    }
    const clubInfo = await response.json();
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

    const membersElement = document.getElementById('members');
    if (clubInfo['members'].length == 1) {
      membersElement.innerHTML = 'There is 1 member in this club.';
    } else {
      membersElement.innerHTML = 'There are ' + clubInfo['members'].length + ' members in this club.';
    }
    document.getElementById('website').innerHTML = clubInfo['website'];

    if(clubInfo['isOfficer']) {
      document.getElementById('edit-button').removeAttribute('hidden');
    }
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
  const color1 = "#AAA";
  const color2 = "#BBB";
  var evenOdd = true;
  for (var announcement in json) {
    template.content.querySelector('img').src = 'images/profile.jpeg';
    template.content.querySelector('#announcement-author').innerHTML = json[announcement].authorName;
    template.content.querySelector('#announcement-content').innerHTML = json[announcement].content;

    const dateString = new Date(json[announcement].time).toLocaleDateString("en-US");
    const timeString = new Date(json[announcement].time).toLocaleTimeString("en-US");
    template.content.querySelector('#announcement-time').innerHTML = timeString + " on " + dateString;

    backgroundColor = evenOdd ? color1 : color2; //In order to switch background colors every announcement
    template.content.querySelector('#announcement-container').style.backgroundColor = backgroundColor;
    evenOdd = !evenOdd;

    if (JSON.parse(json[announcement].isAuthor)) {
      template.content.querySelector('.delete-announcement').style = "display: inline-block;";
      template.content.querySelector('#club').value = json[announcement].club;
      template.content.querySelector('#author').value = json[announcement].author;
      template.content.querySelector('#content').value = json[announcement].content;
      template.content.querySelector('#time').value = json[announcement].time;
    } else {
      template.content.querySelector('.delete-announcement').style = "display: none;";
    }

    var clone = document.importNode(template.content, true);
    document.getElementById('announcements-display').appendChild(clone);
  }
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
  }
}

/** Fetches blobstore image upload url. */
async function fetchBlobstoreUrl() {
  fetch('/blobstore-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('club-form');
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
  document.getElementById('edit-button').hidden = 'true';
  document.getElementById('edit-form').removeAttribute('hidden');
}

/** Store edited content from club page */
function saveClubChanges() {
  const newDesc = document.getElementById("description").innerHTML;
  const newWebsite = document.getElementById("website").innerHTML;
  var newOfficers = [];

  const list = document.getElementById('officers-list');
  const officerList = list.getElementsByTagName('li');
  for (var i = 0; i < officerList.length; i++) {
    newOfficers.push(officerList[i].innerText);
  }
  
  document.getElementById('new-desc').value = newDesc;
  document.getElementById('new-web').value = newWebsite;
  document.getElementById('new-officers').value = newOfficers;
  document.getElementById('name').value = document.getElementById('club-name').innerHTML;
  document.forms['edit-form'].submit();
  alert('Changes submitted!');
}

async function getImageUrl(logoKey) {
    return await fetch('/get-image?blobKey=' + logoKey)
        .then((pic) => {
          return pic.url;
        });
}