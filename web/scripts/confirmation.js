$(document).ready(() => {
    let info = JSON.parse(window.localStorage.getItem("sales"));
    $("#cost").text(`$${info.totalCost}`);
    
    for (let sale of info.sales)
    {
        $("#record-table-body").append(
            `<tr>
                <th scope="row">${sale.saleId}</th>
                <td>${sale.title}</td>
                <td>${sale.quantity}</td>
             </tr>`
        );
     }
     
     localStorage.removeItem("sales");
});
