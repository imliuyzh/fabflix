const PAYMENT_FORM = $("#payment-form");

function setPrice()
{
    fetch(`api/cart`)
        .then(response => response.json())
        .then(({ totalCost }) => {
            $("#cost").text("$" + totalCost);
        });
}

function validateForm()
{
    $("#error-messages").html("");
    
    let isValid = true;
    if (!/[a-zA-Z]+/g.test($("#first-name")[0].value.trim()))
    {
        $("#error-messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Invalid First Name Format.</span>`);
        isValid = false;
    }
    if (!/[a-zA-Z]+/g.test($("#last-name")[0].value.trim()))
    {
        $("#error-messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Invalid Last Name Format.</span>`);
        isValid = false;
    }
    if (!/\d+/g.test($("#credit-card")[0].value.trim()))
    {
        $("#error-messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Invalid Credit Card Format.</span>`);
        isValid = false;
    }
    if ($("#expiration-date").val() === "")
    {
        $("#error-messages").append(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Invalid Expiration Date Format.</span>`);
        isValid = false;
    }
    return isValid;
}

function processPayment(event)
{
    event.preventDefault();
    if (validateForm() == true)
    {
        fetch(`api/payment`, { method: "POST", body: new URLSearchParams(PAYMENT_FORM.serialize()) })
            .then(response => response.json())
            .then(result => {
                if (result.successful === true)
                {
                    window.localStorage.setItem("sales", JSON.stringify(result));
                    window.location.replace("confirmation.html");
                }
                else
                {
                    $("#error-messages").html(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Invalid Payment Information.</span>`);   
                }
            })
            .catch(() => $("#error-messages").html(`<span class="error-message"><i class="las la-exclamation-circle"></i>&nbsp;Invalid Payment Information.</span>`));
    }
}

$(document).ready(() => {
    setPrice(); 
    PAYMENT_FORM.submit(processPayment); 
});
