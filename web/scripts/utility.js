export async function setGenres()
{
    let response = await fetch("api/genres"), genres = await response.json();
    genres.forEach(genre => {
        let parameters = new URLSearchParams({
            title: "",
            year: "",
            director: "",
            star: "",
            numResults: getParameterByName("numResults"),
            offset: 0,
            selectedGenre: genre.id,
            selectedTitle: "",
            sortBy1: getParameterByName("sortBy1"),
            sortOrder1: getParameterByName("sortOrder1"),
            sortBy2: getParameterByName("sortBy2"),
            sortOrder2: getParameterByName("sortOrder2")
        });
        
        $("#genre-list").append(
            `<li class="category-list-item">
                <a href="index.html?${parameters}">${genre.name}</a>
             </li>`
        );
    });
}

export function setTitles()
{
    for (let character of "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*")
    {
        let parameters = new URLSearchParams({
            title: "",
            year: "",
            director: "",
            star: "",
            numResults: getParameterByName("numResults"),
            offset: 0,
            selectedGenre: "",
            selectedTitle: character,
            sortBy1: getParameterByName("sortBy1"),
            sortOrder1: getParameterByName("sortOrder1"),
            sortBy2: getParameterByName("sortBy2"),
            sortOrder2: getParameterByName("sortOrder2")
        });
        
        $("#title-list").append(
            `<li class="category-list-item">
                <a href="index.html?${parameters}">${character}</a>
            </li>`
        );
    }
}

function validateForm()
{
    $("#title-input")[0].value = $("#title-input").val().trim();
    $("#year-input")[0].value = $("#year-input").val().trim();
    $("#director-input")[0].value = $("#director-input").val().trim();
    $("#star-input")[0].value = $("#star-input").val().trim();
    $("#error-messages").html("");
    
    if ($("#year-input").val().trim() !== "")
    {
        if (Number.isNaN(parseInt($("#year-input").val())))
        {
            $("#search-error-messages").append(`<span class="search-error-message"><i class="las la-exclamation-circle">&nbsp;Verify Movie Year.</i></span>`);
            return false;
        }
    }
    return true;
}

function redirectToNewResultPage()
{
    let parameters = new URLSearchParams({
        title: $("#title-input").val(),
        year: $("#year-input").val(),
        director: $("#director-input").val(),
        star: $("#star-input").val(),
        numResults: getParameterByName("numResults"),
        offset: 0,
        selectedGenre: "",
        selectedTitle: "",
        sortBy1: getParameterByName("sortBy1"),
        sortOrder1: getParameterByName("sortOrder1"),
        sortBy2: getParameterByName("sortBy2"),
        sortOrder2: getParameterByName("sortOrder2")
    });
    window.location.href = `index.html?${parameters}`;
}

function handleLookup(query, doneCallback) 
{
    console.log(`[${new Date().toLocaleString()}] The Autocomplete search is initiated.`);
    let cache = JSON.parse(window.localStorage.getItem("autocompleteCache"));
    if (query in cache)
    {
        console.log(`[${new Date().toLocaleString()}] Using cached results to do the Autocomplete search: `, cache[query]);
        doneCallback({ suggestions: cache[query] });
    }
    else
    {
        console.log(`[${new Date().toLocaleString()}] Sending an AJAX request to do the Autocomplete search...`);
        let keys = Object.keys(cache);
        $.ajax({
            "error": errorData => {
                console.log(`[${new Date().toLocaleString()}] Error in processing the request. `, errorData);
            },
            "method": "GET",
            "success": data => {
                console.log(`[${new Date().toLocaleString()}] The suggestion list returned from an AJAX request: `, data);
                if (keys.length >= 100)
                {
                    delete cache[keys[99]];
                }
                cache[query] = data;
                window.localStorage.setItem("autocompleteCache", JSON.stringify(cache));
                doneCallback({ suggestions: data });
            },
            "url": `autocomplete?query=${encodeURIComponent(query)}`
        });
    }
}

export function initializeSidebar()
{
    $("#logo-link").attr("href", "index.html?title=&year=&director=&star=&numResults=25&offset=0&selectedGenre=&selectedTitle=&sortBy1=title&sortOrder1=ASC&sortBy2=rating&sortOrder2=DESC");
    $("#search-button").click(event => {
        event.preventDefault();
        if (validateForm() === true)
        {
            redirectToNewResultPage();
        }
    });
}

export function initializeAutoComplete()
{
    if (window.localStorage.getItem("autocompleteCache") === null)
    {
        window.localStorage.setItem("autocompleteCache", JSON.stringify({}));
    }
    
    $('#title-input').autocomplete({
        autoSelectFirst: false,
        deferRequestBy: 300,
        lookup: (query, doneCallback) => {
            if ($("#title-input").val().trim() !== "")
            {
                handleLookup(query, doneCallback);
            }
        },
        minChars: 3,
        onSelect: suggestion => {
            let parameters = new URLSearchParams({
                id: suggestion.data.id,
                title: getParameterByName("title"),
                year: getParameterByName("year"),
                director: getParameterByName("director"),
                star: getParameterByName("star"),
                numResults: getParameterByName("numResults"),
                offset: getParameterByName("offset"),
                selectedGenre: getParameterByName("selectedGenre"),
                selectedTitle: getParameterByName("selectedTitle"),
                sortBy1: getParameterByName("sortBy1"),
                sortOrder1: getParameterByName("sortOrder1"),
                sortBy2: getParameterByName("sortBy2"),
                sortOrder2: getParameterByName("sortOrder2")
            });
            window.location.href = `movie.html?${parameters}`;
        },
        triggerSelectOnValidInput: false
    });
    $('#title-input').keypress(event => {
        if (event.keyCode === 13) 
        {
            redirectToNewResultPage();
        }
    });
}

export function getParameterByName(target) 
{
    let url = window.location.href, parsedTarget = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp(`[?&]${parsedTarget}(=([^&#]*)|&|#|$)`), results = regex.exec(url);

    return (!results) 
        ? null 
        : (!results[2]) 
            ? "" : decodeURIComponent(results[2].replace(/\+/g, " "));
}
