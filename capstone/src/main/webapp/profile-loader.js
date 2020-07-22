$(document).ready(function() {
  showOrHideProfile();
});

/** Fetch login status and show or hide profile accordingly */
function showOrHideProfile() {
  fetch('/auth').then(response => response.text()).then((loginStatus) => {
    var authContent = document.getElementById('profile-content');
    if (loginStatus.includes('Logout')) {
      authContent.innerHTML += loginStatus;
      getStudentInfo();
    }
    else {
      authContent.innerHTML = loginStatus;
    }
  })
}

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
  liElement.innerText = text + '  ';
  liElement.innerHTML += '<button>Leave</button>';
  return liElement;
}