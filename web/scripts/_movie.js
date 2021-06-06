const MOVIE_FORM = $("#movie-form");

function validateForm()
{
    let isValid = true;
    $("#messages").html("");
    
    if (!/[a-zA-Z0-9]+/g.test($("#title-input").val().trim()))
    {
        $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Please Verify the Movie's Title.</span>`);
        isValid = false;
    }
    if ($("#movie-year-input").val().trim() !== "")
    {
        if (Number.isNaN(parseInt($("#movie-year-input").val())))
        {
            $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle">&nbsp;Please Verify the Movie's Year.</i></span>`);
            isValid = false;
        }
    }
    if (!/[a-zA-Z0-9]+/g.test($("#director-input").val().trim()))
    {
        $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Please Verify the Movie's Director.</span>`);
        isValid = false;
    }    
    if (!/[a-zA-Z0-9]+/g.test($("#star-name-input").val().trim()))
    {
        $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Please Verify the Star's Name.</span>`);
        isValid = false;
    }
    if ($("#birth-year-input").val().trim() !== "")
    {
        if (Number.isNaN(parseInt($("#birth-year-input").val())))
        {
            $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle">&nbsp;Please Verify the Star's Birth Year.</i></span>`);
            isValid = false;
        }
    }
    if (!/[a-zA-Z]+/g.test($("#genre-input").val().trim()))
    {
        $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Please Verify the Movie's Genre.</span>`);
        isValid = false;
    }
    
    return isValid;
}

async function processRequest(event)
{
    event.preventDefault();
    if (validateForm() == true)
    {
        let response = await fetch("api/movie", { 
            method: "POST", 
            body: new URLSearchParams({
                "title": $("#title-input").val().trim(),
                "movieYear": $("#movie-year-input").val().trim(),
                "director": $("#director-input").val().trim(),
                "starName": $("#star-name-input").val().trim(),
                "starBirthYear": $("#birth-year-input").val().trim(),
                "genre": $("#genre-input").val().trim()
            }) 
        });
        let data = await response.json();
        
        $("#messages").html("");
        if (data.successful === true)
        {
            $("#messages").append(`<span class="success-message"><i class="las la-check"></i>&nbsp;${data.movieMessage}</span>`);
            $("#messages").append(`<span class="success-message"><i class="las la-check"></i>&nbsp;${data.starMessage}</span>`);
            $("#messages").append(`<span class="success-message"><i class="las la-check"></i>&nbsp;${data.genreMessage}</span>`);
        }
        else
        {
            $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;${data.errorMessage}</span>`);
        }
    }
}

$(document).ready(() => MOVIE_FORM.submit(processRequest));
