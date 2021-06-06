function setPrice(totalCost)
{
    $("#cost").text(`$${totalCost}`);
}

function addItem(event)
{
    event.preventDefault();
    $.ajax("api/cart", {
        method: "POST",
        data: "&item=" + event.target.id.split("-")[0]
    })
    .done((data, textStatus, jqXHR) => {
        location.reload();
    });
}

function decreaseItem(event)
{
    event.preventDefault();
    $.ajax("api/cart", {
        method: "POST",
        data: "&operation=decrease&item=" + event.target.id.split("-")[0]
    })
    .done((data, textStatus, jqXHR) => {
        location.reload();
    });
}

function deleteItem(event)
{
    event.preventDefault();
    $.ajax("api/cart?item=" + event.target.id.split("-")[0], {
        method: "DELETE",
    })
    .done((data, textStatus, jqXHR) => {
        location.reload();
    });
}

function generateCartRow(movie)
{
    $("#cart-table-body").append(
        `<tr id="${movie.id}-row">
            <th id="${movie.id}-row-title" scope="row">${movie.title}</th>
            <td id="${movie.id}-row-quantity">${movie.quantity}</td>
            <td id="${movie.id}-row-price">${movie.price}</td>
            <td id="${movie.id}-row-add">
                <i class="las la-plus cart-button" id="${movie.id}-row-add-button"></i>
            </td>
            <td id="${movie.id}-row-decrease">
                <i class="las la-minus cart-button" id="${movie.id}-row-decrease-button"></i>
            </td>
            <td id="${movie.id}-row-delete">
                <i class="las la-times cart-button" id="${movie.id}-row-delete-button"></i>
            </td>
        </tr>`
    );
    $("#" + movie.id + "-row-add").click(addItem);
    $("#" + movie.id + "-row-decrease").click(decreaseItem);
    $("#" + movie.id + "-row-delete").click(deleteItem);
}

function generateCart(cartItems)
{
    $("#cart").append(
        `<table class="table table-striped table-borderless table-dark" id="cart-table">
            <thead>
                <tr>
                    <th scope="col">Title</th>
                    <th scope="col">Quantity</th>
                    <th scope="col">Price</th>
                    <th scope="col">Add</th>
                    <th scope="col">Decrease</th>
                    <th scope="col">Delete</th>
                </tr>
            </thead>
            <tbody id="cart-table-body"></tbody>
        </table>`
    );
    cartItems.forEach(item => generateCartRow(item));
}

function generatePage()
{
    fetch(`api/cart`)
        .then(response => response.json())
        .then(item => {
            generateCart(item.cartItems);
            setPrice(item.totalCost);
            
            if (item.length <= 0 || $("#cost").text() === "$0")
            {
                $("#payment-link").remove();
            }
        }
    );
}

$(document).ready(() => generatePage());
