$(document).foundation();

document.getElementById('postRegistrationData').addEventListener('submit', postRegistrationData);

 function postRegistrationData(event){
    event.preventDefault();

    $('#registrationErrorText').css('display', 'none');

    let user = document.getElementById('username').value;
    let email = document.getElementById('email').value;
    let pw = document.getElementById('password').value;

    fetch('/PlaygroundApp/register', {

        headers: {
            "Content-Type": "application/json"
        },
        method: 'POST',
        body: JSON.stringify({username:user, password:pw, email:email})

    }).then(response => {
        if (response.ok) {
            return response.json();
        } else {
            Promise.reject(response.json());
        }
    }).then((responseJson) => {
        console.log(responseJson);

        if (responseJson.isValid) {
            localStorage.setItem('token', responseJson.token);
            window.location.href = "/PlaygroundApp/foundation";
        } else {
            $('#loginErrorText').css('display', 'inline');
        }
    }).catch((error) => {
        console.log(error);
    });
}