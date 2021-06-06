import { getParameterByName, initializeAutoComplete, setGenres, setTitles } from "./utility.js";

const STARID = getParameterByName("id");


function setName(name)
{
    $("title").text(`${name} - Fabflix`);
    $("#star-name").text(name);
    
    let parameters = new URLSearchParams({
        id: STARID,
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
    $("#star-name").attr("href", `star.html?${parameters}`);
}

function setBirthYear(birthYear)
{
    if (birthYear === 0)
    {
        $("#birthyear").text("N/A");
    }
    else
    {
        $("#birthyear").text(birthYear);
    }
}

function setLinks(name)
{
    if (STARID.startsWith("nm"))
    {
        $("#links").prepend(
            `<a href="https://www.imdb.com/name/${STARID}/" id="imdb-link" class="star-info">
                <i class="lab la-imdb"></i>
                <span>IMDb</span>
             </a>`
        );
    }
    $("#google-link").attr("href", encodeURI(`https://www.google.com/search?q=${name}`));
}

function setMovies(movies)
{
    if (movies.length > 0)
    {
        $("main").append(
            `<h1 id="movie-section-header">movies</h1>
             <section id="movie-list"></section>`
        );
        
        let createParameters = (id) => new URLSearchParams({
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
        
        for (let { id, title, year } of movies)
        {
            let movieEntry = `<div class="movie">
                                  <img id="${id}-poster" src="images/poster.jpg" class="poster" loading="lazy" />
                                  <div class="movie-title-container">
                                      <a href="movie.html?${createParameters(id)}" class="movie-title-link">${title}</a>
                                  </div>
                                  <span class="movie-year">${year}</span>
                                  <div class="movie-links">`;
            if (id.includes("-") === false)
            {
                movieEntry += `<a href="https://www.imdb.com/title/${id}/" class="movie-link"><i class="lab la-imdb"></i></a>`;
            }
            movieEntry += `<a href="https://www.google.com/search?q=${encodeURIComponent(title)}" class="movie-link"><i class="lab la-google"></i></a></div></div>`;
            $("#movie-list").append(movieEntry);
            
            fetch(`api/poster?id=${id}`)
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

function loadStar()
{
    fetch(`api/star?id=${STARID}`)
        .then(response => response.json())
        .then(({ name, birthYear, movies }) => {
            setName(name);
            setBirthYear(birthYear);
            setLinks(name);
            setMovies(movies);
        });
        
    fetch(`api/portrait?id=${STARID}`)
        .then(response => response.json())
        .then(result => {
            if (result.successful)
            {
                $("#portrait").attr("src", result.path);
            }
        });
}

function registerListeners()
{
    $("#browse-button").click(() => $('#browse-modal').modal("toggle"));
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
    registerListeners();
    setGenres();
    setTitles();
    loadStar();
});
