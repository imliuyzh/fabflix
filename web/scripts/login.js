const LOGIN_FORM = $("#login-form");


function validateForm()
{
    let validEmail = false, validPassword = false, validRecaptcha = false;
    $("#error-messages").css({ "display": "none" });
    $("#error-messages").html("");

    let email = $("#email")[0].value.trim(), password = $("#password")[0].value.trim();
    if (email.length <= 0)
    {
        $("#error-messages").css({ "display": "flex" });
        $("#error-messages").append(`<span class="error-message"><i class="bi bi-exclamation-circle">&nbsp;Verify Your Email.</i></span>`);
    }
    else
    {
        validEmail = true;
    }
    
    if (password.length <= 0)
    {
        $("#error-messages").css({ "display": "flex" });
        $("#error-messages").append(`<span class="error-message"><i class="bi bi-exclamation-circle">&nbsp;Verify Your Password.</i></span>`);
    }
    else
    {
        validPassword = true;
    }
    
    if (grecaptcha.getResponse().length === 0)
    {
        $("#error-messages").css({ "display": "flex" });
        $("#error-messages").append(`<span class="error-message"><i class="bi bi-exclamation-circle">&nbsp;Please Complete reCAPTCHA.</i></span>`);
    }
    else
    {
        validRecaptcha = true;
    }
    
    return validEmail && validPassword && validRecaptcha;
}

function loadUser(event)
{
    event.preventDefault();
    if (validateForm() === true)
    {
        $.ajax("api/login", {
            method: "POST",
            data: LOGIN_FORM.serialize() + "&userType=customer"
        })
        .done((data, textStatus, jqXHR) => {
            if (jqXHR.responseJSON.successful === true) 
            {
                window.location.replace("index.html?title=&year=&director=&star=&numResults=25&offset=0&selectedGenre=&selectedTitle=&sortBy1=title&sortOrder1=ASC&sortBy2=rating&sortOrder2=DESC");
            }
        })
        .fail((jqXHR, textStatus, errorThrown) => {
            $("#error-messages").css({ "display": "none" });
            
            if (jqXHR.status === 401 || jqXHR.status === 500)
            {
                $("#error-messages").css({ "display": "flex" });
                $("#error-messages").html(`<span class="error-message"><i class="bi bi-exclamation-octagon">&nbsp;${jqXHR.responseJSON.errorMessage}</i></span>`);
            }
            else
            {
                $("#error-messages").css({ "display": "flex" });
                $("#error-messages").html(`<span class="error-message"><i class="bi bi-exclamation-octagon">&nbsp;Unknown Error. Please Try Again.</i></span>`);
            }
            
            grecaptcha.reset();
        });
    }
}

$(document).ready(() => LOGIN_FORM.submit(loadUser));
