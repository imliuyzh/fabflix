import { getParameterByName, initializeAutoComplete, setGenres, setTitles } from "./utility.js";

const MOVIEID = getParameterByName("id");


function setTitle(name)
{
    $("title").text(`${name} - Fabflix`);
}

function setInfo(movie)
{
    let createParametersForGenres = (id) => new URLSearchParams({
        title: "",
        year: "",
        director: "",
        star: "",
        numResults: getParameterByName("numResults"),
        offset: 0,
        selectedGenre: id,
        selectedTitle: "",
        sortBy1: getParameterByName("sortBy1"),
        sortOrder1: getParameterByName("sortOrder1"),
        sortBy2: getParameterByName("sortBy2"),
        sortOrder2: getParameterByName("sortOrder2")
    });
    $("#info").prepend((movie.genres.length <= 0)
        ? `<span class="genre">Uncategorized</span>`
        : `<div class="genres">${movie.genres.map(({ id, name }) => `<a href="index.html?${createParametersForGenres(id)}" class="genre">${name}</a>`).join("&nbsp;·&nbsp;")}</div>`
    );
    
    let createParametersForMovie = (id) => new URLSearchParams({
        id,
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
    $("#movie-title-container").html(
        `<a href="movie.html?${createParametersForMovie(MOVIEID)}" id="movie-title">
            ${movie.title}
            <span id="movie-year">${movie.year}</span>
         </a>`
    );
    
    if (MOVIEID.startsWith("tt"))
    {
        $("#links").prepend(
            `<a href="https://www.imdb.com/title/${MOVIEID}/" id="imdb-link" class="info-link">
                <i class="lab la-imdb"></i>
                <span>IMDb</span>
             </a>`
        );
    }
    
    $("#google-link").attr("href", `https://www.google.com/search?q=${encodeURIComponent(movie.title)}`);
}

function setRating(movie)
{
    if (movie.rating === -1 && movie.numVotes === -1)
    {
        $("#score").text("N/A");
        $("#num-of-votes").text("0 Votes");
    }
    else
    {
        $("#score").text(movie.rating);
        $("#num-of-votes").text(`${movie.numVotes} Votes`);
    }
}

function setDirector(movie)
{
    $("#director-name-container").text(movie.director);
    $("#director-links").html(
        `<a href="https://www.google.com/search?q=${encodeURIComponent(movie.director)}" class="director-link">
            <i class="lab la-google"></i>
         </a>`
    );
}

function setStars(stars)
{
    if (stars.length > 0)
    {
        $("main").append(
            `<section id="stars-section">
                <h1 id="star-section-header" class="section-header">stars</h1>
                <div id="star-list"></div>
             </section>`
        );
        
        let createParametersForStars = (id) => new URLSearchParams({
            id,
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
        
        for (let { id, name, birthYear } of stars)
        {
            let starEntry = `<div class="star">
                                 <img id="${id}-poster" src="images/person.png" class="portrait" loading="lazy" />
                                 <div class="star-name-container">
                                     <a href="star.html?${createParametersForStars(id)}" class="star-name-link">${name}</a>
                                 </div>
                                 <span class="star-birthyear">${(birthYear === 0) ? "N/A" : birthYear}</span>
                                 <div class="star-links">`;
            if (id.includes("-") === false)
            {
                starEntry += `<a href="https://www.imdb.com/name/${id}/" class="star-link"><i class="lab la-imdb"></i></a>`;
            }
            starEntry += `<a href="https://www.google.com/search?q=${encodeURIComponent(name)}" class="star-link"><i class="lab la-google"></i></a></div></div>`;
            $("#star-list").append(starEntry);
            
            fetch(`api/portrait?id=${id}`)
                .then(response => response.json())
                .then(result => {
                    if (result.successful)
                    {
                        $(`#${id}-poster`).attr("src", result.path);
                    }
                });
        }
    }
    else
    {
        return "";
    }
}

function registerListeners()
{
    $("#addToCart.purchase").click(addToCart);
    $("#browse-button").click(() => $('#browse-modal').modal("toggle"));
}

function loadMovie()
{
    fetch(`api/movie?id=${MOVIEID}`)
        .then(response => response.json())
        .then(movie => {
            setTitle(movie.title);
            setInfo(movie);
            setRating(movie);
            setDirector(movie);
            setStars(movie.stars);
        });
        
    fetch(`api/poster?id=${MOVIEID}`)
        .then(response => response.json())
        .then(result => {
            if (result.successful)
            {
                $(`#poster`).attr("src", result.path);
            }
        });
}

function addToCart(event){
    event.preventDefault();
    
    $("#cart-messages").html("");
    $.ajax("api/cart", {
        method: "POST",
        data: "&item=" + MOVIEID
    })
    .done((data, textStatus, jqXHR) => {
        $("#cart-messages").append(`<span class="cart-success-message"><i class="las la-check"></i>&nbsp;Added Item to Cart.</span>`);
    })
    .fail((jqXHR, textStatus, errorThrown) => {
        $("#cart-messages").append(`<span class="cart-error-message"><i class="las la-exclamation-circle"></i>&nbsp;Failed to Add Item to Cart.</span>`);
    });
}

function validateForm()
{
    $("#title-input").val($("#title-input").val().trim());
    $("#year-input").val($("#year-input").val().trim());
    $("#director-input").val($("#director-input").val().trim());
    $("#star-input").val($("#star-input").val().trim());
    $("#error-messages").html("");
    
    if ($("#year-input").val().trim() !== "")
    {
        if (Number.isNaN(parseInt($("#year-input").val().trim())))
        {
            $("#search-error-messages").append(`<span class="search-error-message"><i class="las la-exclamation-circle">&nbsp;Verify Movie Year.</i></span>`);
            return false;
        }
    }
    return true;
}

function initializeSidebar()
{
    let parameters = new URLSearchParams({
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
    
    $("#logo-link").attr("href", `index.html?${parameters}`);
    $("#search-button").click((event) => {
        event.preventDefault();
        if (validateForm() === true)
        {
            let searchParameters = new URLSearchParams({
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
            window.location.href = `index.html?${searchParameters}`;
        }
    });
    
    initializeAutoComplete();
}

$(document).ready(() => {
    initializeSidebar();
    setGenres();
    setTitles();
    registerListeners();
    loadMovie();
});
