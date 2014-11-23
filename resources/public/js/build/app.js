var CONFIG = {
    apiPath: 'http://localhost:8080/'
};

var CastList = React.createClass({displayName: 'CastList',
    render: function() {
        var cast = this.props.cast.map(function(cast) {
            return (
                React.createElement("tr", null, 
                    React.createElement("td", {key: cast.name}, cast.name), 
                    React.createElement("td", null, cast.character)
                )
            );
        });

        var age = Math.floor(this.props.age);
        var castLen = this.props.cast.length;

        return (
            React.createElement("div", {id: "cast-information"}, 
                React.createElement("h3", null, this.props.movie.title), 
                !age ? React.createElement("p", null, "No date of birth information on file for any of the actors in this film. ") : React.createElement("h5", null, "Average age of cast: ", React.createElement("span", null, age)), 
                React.createElement("table", {className: "table table-striped col-sm-6"}, 
                    React.createElement("thead", null, 
                        React.createElement("tr", null, 
                            React.createElement("td", null, "Name"), 
                            React.createElement("td", null, "Character")
                        )
                    ), 
                    React.createElement("tbody", null, 
                    cast
                    )
                )
            )
        );
    }
});

var MovieList = React.createClass({displayName: 'MovieList',
    render: function() {
        var movies = this.props.movies.map(function(movie, i) {
            var boundClick = this.props.onClick.bind(this, i);
            return (
                React.createElement("tr", null, 
                    React.createElement("td", {key: movie.title}, React.createElement("a", {href: "javascript:void(0)", onClick: boundClick}, movie.title)), 
                    React.createElement("td", null, movie.release_date)
                )
            )
        }.bind(this));

        return (
            React.createElement("table", {className: "table table-striped col-sm-6"}, 
                React.createElement("thead", null, 
                    React.createElement("tr", null, 
                        React.createElement("td", null, "Name"), 
                        React.createElement("td", null, "Release Date")
                    )
                ), 
                React.createElement("tbody", null, 
                movies
                )
            )
        );
    }
});

var App = React.createClass({displayName: 'App',
    getInitialState: function() {
        $.ajax({
            url: CONFIG.apiPath + 'movies/now-playing',
            dataType: 'json',
            success: function(data) {
                this.setState({movies: data, cast: []});
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)

        });

        return {
            movies: [],
            cast: []
        };
    },
    updateCast: function(index) {
        var activeMovie = this.state.movies[index]

        $.ajax({
            url: CONFIG.apiPath + 'movies/' + activeMovie.id + '/cast',
            dataType: 'json',
            success: function(data) {
                var cast = data;
                $.ajax({
                    url: CONFIG.apiPath + 'movies/' + activeMovie.id + '/average-age-of-cast',
                    dataType: 'json',
                    success: function(data) {
                        this.setState({cast: cast, movie: activeMovie, movies: this.state.movies, age: data.average_age});
                    }.bind(this),
                    error: function(xhr, status, err) {
                        console.error(this.props.url, status, err.toString());
                    }.bind(this)

                });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)

        });
    },
    render: function() {
        return (
            React.createElement("div", {id: "app"}, 
                React.createElement(MovieList, {onClick: this.updateCast, movies: this.state.movies}), 
                 this.state.cast.length ? React.createElement(CastList, {cast: this.state.cast, age: this.state.age, movie: this.state.movie}) : React.createElement("p", null, "No actors listed for this film.")
            )
        )

    }
});

React.render(
    React.createElement(App, null),
    document.getElementById('wilhelm')
);