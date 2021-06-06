import { initializeSidebar, initializeAutoComplete, getParameterByName, setGenres, setTitles } from "./utility.js";

function createGenreString(genres)
{
    function generateGenreString(genre)
    {
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
        return `<a href="index.html?${parameters}" class="genre">${genre.name}</a>`;
    }
    
    if (genres.length <= 0)
    {
        return `<span class="genre">Uncategorized</span>`;
    }
    else
    {
        return `<div class="genres">
                    ${genres.map(genre => generateGenreString(genre)).join("&nbsp;·&nbsp;")}
                </div>`;
    }
}

function createStarString(stars)
{
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
    
    return (stars.length <= 0)
        ? ""
        : stars
            .map(({ id, name, _ }) => 
                `<a href="star.html?${createParameters(id)}">
                    <span class="credit-name">${name}</span>
                 </a>`
            )
            .join(",&nbsp;");
}

function createMovieEntry(movie)
{
    let parameters = new URLSearchParams({
        id: movie.id,
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
    
    $("#movie-list").append(
        `<div class="movie">
            <img id="${movie.id}-poster" src="images/poster.jpg" class="poster" loading="lazy">
            <div class="info">
                ${createGenreString(movie.genres)}
                <a href="movie.html?${parameters}" class="movie-title-link">
                    <span class="movie-title">${movie.title}<span class="year">${movie.year}</span></span>
                </a>
                <div class="rating-and-credit">
                    <div class="rating">
                        <div class="score">${(movie.rating > -1) ? movie.rating.toFixed(1) : "N/A"}</div>
                        <div class="num-of-votes">${(movie.numVotes > -1) ? `${movie.numVotes} Votes` : "0 Votes"}</div>
                    </div>
                    <div class="credits">
                        <div class="credit">
                            <i class="las la-user credit-icon"></i>&nbsp;
                            <span class="credit-name">${movie.director}</span>
                        </div>
                        <div class="credit">
                            <i class="las la-star credit-icon"></i>&nbsp;
                            ${createStarString(movie.stars)}
                        </div>
                    </div>
                </div>
            </div>
            <a id="${movie.id}_purchase" class="purchase"><i class="las la-cart-plus"></i>&nbsp;Add to Cart</a>
            <div class="cart-messages" id="cart-messages-${movie.id}"></div>
        </div>`
    );
    
    $("#" + movie.id + "_purchase").click(addToCart);
}

function addToCart(event){
    event.preventDefault();
    
    $("#cart-messages-" + event.target.id.split("_")[0]).html("");
    $.ajax("api/cart", {
        method: "POST",
        data: "&item=" + event.target.id.split("_")[0]
    })
    .done((data, textStatus, jqXHR) => {
        $("#cart-messages-" + event.target.id.split("_")[0]).append(`<span class="cart-success-message"><i class="las la-check"></i>&nbsp;Added Item to Cart.</span>`);
    })
    .fail((jqXHR, textStatus, errorThrown) => {
        $("#cart-messages-" + event.target.id.split("_")[0]).append(`<span class="cart-error-message"><i class="las la-exclamation-circle"></i>&nbsp;Failed to Add Item to Cart.</span>`);
    });
}

function loadMovies()
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
    
    fetch(`api/search?${parameters}`)
        .then(response => response.json())
        .then(movies => {
            if (movies.length > 0)
            {
                movies.forEach(movie => {
                    createMovieEntry(movie);
                    fetch(`api/poster?id=${movie.id}`)
                        .then(response => response.json())
                        .then(result => {
                            if (result.successful)
                            {
                                $(`#${movie.id}-poster`).attr("src", result.path);
                            }
                        });
                });
            }
            else
            {
                $("#movie-list").html(`<span id="no-result-message">No result found.</span>`);
            }
            
            if (getParameterByName("offset") === "0")
            {
                $("#prev-button").remove();
            }
            if (movies.length < parseInt(getParameterByName("numResults")))
            {
                $("#next-button").remove();
            }
        });
}

function registerListeners()
{
    $("#browse-button").click(() => $('#browse-modal').modal("toggle"));
    $("#filter-button").click(() => $('#filter-modal').modal("toggle"));
    $("#filter-apply-button").click((event) => {
        $("#filter-modal-errors").html("");
        if ($("#sort-by-1-options").val() !== $("#sort-by-2-options").val())
        {
            let parameters = new URLSearchParams({
                title: getParameterByName("title"),
                year: getParameterByName("year"),
                director: getParameterByName("director"),
                star: getParameterByName("star"),
                numResults: $("#results-per-page-options").val(),
                offset: 0,
                selectedGenre: getParameterByName("selectedGenre"),
                selectedTitle: getParameterByName("selectedTitle"),
                sortBy1: $("#sort-by-1-options").val(),
                sortOrder1: $("input:radio[name=sort-order-1-options]:checked").val(),
                sortBy2: $("#sort-by-2-options").val(),
                sortOrder2: $("input:radio[name=sort-order-2-options]:checked").val(),
            });
            
            window.location.href = `index.html?${parameters}`;
        }
        else
        {
            $("#filter-modal-errors").html(`<span class="filter-modal-error-message"><i class="las la-exclamation-circle"></i>&nbsp;Please check your sorting settings.</span>`);
        }
    });
    
    let calcOffset = parseInt(getParameterByName("offset")) - parseInt(getParameterByName("numResults"));
    let prevParameters = new URLSearchParams({
        title: getParameterByName("title"),
        year: getParameterByName("year"),
        director: getParameterByName("director"),
        star: getParameterByName("star"),
        numResults: getParameterByName("numResults"),
        offset: (calcOffset > 0) ? calcOffset : 0,
        selectedGenre: getParameterByName("selectedGenre"),
        selectedTitle: getParameterByName("selectedTitle"),
        sortBy1: getParameterByName("sortBy1"),
        sortOrder1: getParameterByName("sortOrder1"),
        sortBy2: getParameterByName("sortBy2"),
        sortOrder2: getParameterByName("sortOrder2")
    });
    $("#prev-button").attr("href", `index.html?${prevParameters}`);

    let nextParameters = new URLSearchParams({
        title: getParameterByName("title"),
        year: getParameterByName("year"),
        director: getParameterByName("director"),
        star: getParameterByName("star"),
        numResults: getParameterByName("numResults"),
        offset: parseInt(getParameterByName("offset")) + parseInt(getParameterByName("numResults")),
        selectedGenre: getParameterByName("selectedGenre"),
        selectedTitle: getParameterByName("selectedTitle"),
        sortBy1: getParameterByName("sortBy1"),
        sortOrder1: getParameterByName("sortOrder1"),
        sortBy2: getParameterByName("sortBy2"),
        sortOrder2: getParameterByName("sortOrder2")
    });
    $("#next-button").attr("href", `index.html?${nextParameters}`);
}

$(document).ready(() => {
    initializeSidebar();
    initializeAutoComplete();
    registerListeners();
    setGenres();
    setTitles();
    loadMovies();
});
