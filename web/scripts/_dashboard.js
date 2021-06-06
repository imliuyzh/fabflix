async function getTables()
{
    let response = await fetch("api/metadata"), data = await response.json();
    for (let table in data)
    {
        let strings = [];
        strings.push(
            `<div class="table-container">
                <header class="section-header">${table}</header>
                <table class="table table-striped table-borderless table-dark">
                    <thead>
                        <tr>
                            <th scope="col">Field</th>
                            <th scope="col">Type</th>
                            <th scope="col">Null</th>
                            <th scope="col">Key</th>
                            <th scope="col">Default</th>
                            <th scope="col">Extra</th>
                        </tr>
                    </thead>
                    <tbody>`
        );

        for (let metadataJSON of data[table])
        {
            strings.push(
                `<tr>
                    <td>${metadataJSON["field"]}</td>
                    <td>${metadataJSON["type"]}</td>
                    <td>${metadataJSON["null"]}</td>
                    <td>${metadataJSON["key"]}</td>
                    <td>${metadataJSON["default"]}</td>
                    <td>${metadataJSON["extra"]}</td>
                 </tr>`
            );
        }

        strings.push(`</tbody></table></div>`);
        $("#tables").append(strings.join(""));
    }
}

$(document).ready(() => getTables());
