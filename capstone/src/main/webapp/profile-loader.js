showOrHideProfile();

/** Fetch login status and show or hide profile accordingly */
function showOrHideProfile() {
  fetch('/auth').then(response => response.text()).then((loginStatus) => {
    var authContent = document.getElementById('top-navigation');
    if (loginStatus.includes('Logout')) {
      authContent.innerHTML = loginStatus + authContent.innerHTML;
      getStudentInfo();
    }
    else {
      authContent = document.getElementById('profile-content');
      authContent.innerHTML = loginStatus;
    }
  })
}

/** Fetch student information and add it to the profile */
function getStudentInfo() {
  fetch('/student-data').then(response => response.json()).then((info) => {  
    var studentInfo = info['student'];
    var announcements = info['announcements'];
    const editableName = 'class="edit-name" contenteditable="true">';
    const pencilIcon = '  <i class="far fa-edit"></i>';

    // Update profile name
    var profileTitle = document.getElementById('heading');
    profileTitle.innerHTML += '<span id="edit-name"' + editableName + studentInfo['name'] + pencilIcon + '</span>';

    // Update profile club list
    var clubList = document.getElementById('club-content');
    const clubs = studentInfo['clubs'];
    for(const club of clubs) {
      clubList.appendChild(createClubElement(club));
    }

    // Add announcements to student's inbox
    var inbox = document.getElementById('inbox');
    inbox.appendChild(addAnnoucements(announcements));

    // Add additional student information and allow year and major to be editable
    var personalInfo = document.getElementById('student-info');
    const newLine = '<br>';
    // Create variables for start of div tag
    const startOfDivYear = '<div id="edit-year"';
    const startOfDivMajor = '<div id="edit-major"';
    const editableDiv = 'class="edit-profile" contenteditable="true">';
    const endDiv = '</div>';

    personalInfo.innerHTML += 'Email: ' + studentInfo['email'] + newLine;
    personalInfo.innerHTML += 'Grad Year: ' + startOfDivYear + editableDiv + studentInfo['gradYear'] + pencilIcon + endDiv + newLine;
    personalInfo.innerHTML += 'Major: ' + startOfDivMajor + editableDiv + studentInfo['major'] + pencilIcon + endDiv + newLine;
  });
}

/** Create ul and li elements for each club's announcements */
function addAnnoucements(announcements) {
  var inboxList = document.createElement('ul');
  for(const announcement of announcements){
    var liElement = document.createElement('li');
    liElement.innerText = announcement;
    inboxList.appendChild(liElement);
  }
  return inboxList;
}

/** Create an <li> element containing club name and leave button */
function createClubElement(text) {
  var liElement = document.createElement('li');

  // Create leave button and set value to its respective club
  liElement.innerHTML += text
    + '  <button name="leave" value="'
    + text
    + '" formmethod="POST">Leave</button>';
  return liElement;
}

/** Store edited content from profile page */
function saveProfileChanges() {
  const newYear = document.getElementById("edit-year").innerHTML;
  const newMajor = document.getElementById("edit-major").innerHTML;
  const newName = document.getElementById("edit-name").innerHTML;

  var updateProfile = document.getElementById("update-profile");

  updateProfile.innerHTML = '<input type="hidden" name="new-year" value="' + newYear + '">';
  updateProfile.innerHTML += '<input type="hidden" name="new-major" value="' + newMajor + '">';
  updateProfile.innerHTML += '<input type="hidden" name="new-name" value="' + newName + '">';

  document.forms['edit-profile'].submit();
}
