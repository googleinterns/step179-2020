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

/** Load navigation bar */
$(document).ready(function() {
  $('#top-navigation').load('top-navbar.html');
});

/** Load 'About Us' Club info tab. Default page displayed when user first enters club page. */
function getClubInfo() {
  var template = document.querySelector('#about-us');
  const node = document.importNode(template.content, true);
  document.getElementById('tab').innerHTML = '';
  document.getElementById('tab').appendChild(node);

  fetch('/clubs').then(response => response.json()).then((clubInfo) => {
    document.getElementById('club-name').innerHTML = clubInfo['name'];
    document.getElementById('description').innerHTML = clubInfo['description'];
    
    var officerList = document.getElementById('officers');
    var officers = clubInfo['officers'];
    officerList.innerHTML = 'Officers:';
    officerList.innerHTML += '<ul>';
    for (const officer of officers) {
      officerList.innerHTML += '<li>' + officer + '</li>';
    }
    officerList.innerHTML += '</ul>'

    document.getElementById('members').innerHTML = '# of Members: ' + clubInfo['members'].length;
    document.getElementById('website').innerHTML = 'Website: ' + clubInfo['website'];
  });
}

/** Accesses and displays club announcement data from servlet. */
async function loadAnnouncements () {
  const query = '/announcements';
  const response = await fetch(query);
  const json = await response.json();
   const announcementsSection = document.getElementById('announcements-display');
  announcementsSection.innerHTML = '<h1>Announcements</h1>';
  announcementsSection.innerHTML += '<ul>';
  for (var index in json) {
      announcementsSection.innerHTML += '<li>'+json[index]+'</li>';
  }
  announcementsSection.innerHTML += '</ul>';
}

/** Displays club info tab, depending on which tab user selected. */
function showTab(tabNum) {
  if (tabNum == 1) {
    getClubInfo();
  } else {
    if (tabNum == 2) {
      var template = document.querySelector('#announcements');
      loadAnnouncements();
    } else if (tabNum == 3) {
      var template = document.querySelector('#calendar');
    }
    const node = document.importNode(template.content, true);
    document.getElementById('tab').innerHTML = '';
    document.getElementById('tab').appendChild(node);
  }
}