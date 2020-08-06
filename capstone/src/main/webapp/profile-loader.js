showOrHideProfile();

/** Fetch login status and show or hide profile accordingly */
function showOrHideProfile() {
  fetch('/auth').then(response => response.text()).then((loginStatus) => {
    if (loginStatus.includes('logout')) {
      var authContent = document.getElementById('top-navigation');
      authContent.innerHTML = '<a id="logout-url" href="">Logout</a>' + authContent.innerHTML;
      document.getElementById('logout-url').href = loginStatus;
      getStudentInfo();
    }
    else {
      document.getElementById('profile-content').innerHTML = loginStatus;
    }
  })
}

/** Fetch student information and add it to the profile */
function getStudentInfo() {
  fetch('/student-data').then(response => response.json()).then((info) => {  
    var studentInfo = info['student'];
    var announcements = info['announcements'];

    // Update profile name
    document.getElementById('edit-name').innerHTML += studentInfo['name'];

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
    document.getElementById('email').innerHTML += studentInfo['email'];
    document.getElementById('edit-year').innerHTML += studentInfo['gradYear'];
    document.getElementById('edit-major').innerHTML += studentInfo['major'];
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

  //var updateProfile = document.getElementById("update-profile");
  document.getElementsByName('new-year')[0].value = newYear;
  document.getElementsByName('new-major')[0].value = newMajor;
  document.getElementsByName('new-name')[0].value = newName;

  document.forms['edit-profile'].submit();
}
