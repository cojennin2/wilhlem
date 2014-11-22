var CONFIG = {
    apiPath: 'http://localhost:3000/'
};

var AverageCastAge = React.createClass({displayName: 'AverageCastAge',
    render: function() {
        return (
            React.createElement("h5", null, "Average age of cast: ", React.createElement("span", null, this.props.age))
        );
    }
});

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

        return (
            React.createElement("div", {id: "cast-information"}, 
                React.createElement(AverageCastAge, {age: this.props.age}), 
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
        var movieID = this.state.movies[index].id;

        $.ajax({
            url: CONFIG.apiPath + 'movies/' + movieID + '/cast',
            dataType: 'json',
            success: function(data) {
                var cast = data;
                $.ajax({
                    url: CONFIG.apiPath + 'movies/' + movieID + '/average-age-of-cast',
                    dataType: 'json',
                    success: function(data) {
                        this.setState({cast: cast, movies: this.state.movies, age: data.average_age});
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
                 this.state.cast.length ? React.createElement(CastList, {cast: this.state.cast, age: this.state.age}) : null
            )
        )

    }
});

React.render(
    React.createElement(App, null),
    document.getElementById('wilhelm')
);