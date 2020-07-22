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

/** Load navigation bars */
$(document).ready(function() {
  $('#top-navigation').load('top-navbar.html');
});

$(document).ready(function() {
  $('#navbar').load('navbar.html');
});

/** Fetch student information and add it to the profile */
function getStudentInfo() {
  fetch('/student-data').then(response => response.json()).then((studentInfo) => {  
    // Update profile name
    var profileTitle = document.getElementById('heading');
    profileTitle.innerHTML += studentInfo['name'];

    // Update profile club list
    var clubList = document.getElementById('club-content');
    const clubs = studentInfo['clubs'];
    for(const key in clubs) {
      clubList.appendChild(createClubElement(clubs[key]));
    }

    // Add additional student information
    var personalInfo = document.getElementById('student-info');
    const newLine = '<br>';
    personalInfo.innerHTML = 'Email: ' + studentInfo['email'] + newLine;
    personalInfo.innerHTML += 'Grad Year: ' + studentInfo['gradYear'] + newLine;
    personalInfo.innerHTML += 'Major: ' + studentInfo['major'] + newLine;
  });
}

/** Create an <li> element containing club name and leave button */
function createClubElement(text) {
  var liElement = document.createElement('li');
  liElement.innerText = text + "  ";
  liElement.innerHTML += '<button>Leave</button>';
  return liElement;
}