const STAR_FORM = $("#star-form");

function validateForm()
{
    $("#messages").html("");
    
    let isValid = true;
    if (!/[a-zA-Z0-9]+/g.test($("#full-name").val().trim()))
    {
        $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Please Verify the Star's Name.</span>`);
        isValid = false;
    }
    if ($("#year-input").val().trim() !== "")
    {
        if (Number.isNaN(parseInt($("#year-input").val())))
        {
            $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle">&nbsp;Please Verify the Year of Birth.</i></span>`);
            isValid = false;
        }
    }
    
    return isValid;
}

async function processRequest(event)
{
    event.preventDefault();
    if (validateForm() == true)
    {
        let response = await fetch(`api/star`, { 
            method: "POST", 
            body: new URLSearchParams({
                "name": $("#full-name").val().trim(),
                "birthYear": $("#year-input").val().trim()
            }) 
        });
        let data = await response.json();
        
        $("#messages").html("");
        if (response.status === 201)
        {
            $("#messages").append(`<span class="success-message"><i class="las la-check"></i>&nbsp;${$("#full-name").val()} (${data.id}) is Entered.</span>`);
        }
        else if (response.status === 409)
        {
            $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;This Star Already Exists.</span>`);
        }
        else
        {
            $("#messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;${data.errorMessage}</span>`);
        }
    }
}

$(document).ready(() => STAR_FORM.submit(processRequest));
