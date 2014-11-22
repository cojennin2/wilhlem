var CONFIG = {
    apiPath: 'http://localhost:3000/'
};

var AverageCastAge = React.createClass({
    render: function() {
        return (
            <h5>Average age of cast: <span>{this.props.age}</span></h5>
        );
    }
});

var CastList = React.createClass({
    render: function() {
        var cast = this.props.cast.map(function(cast) {
            return (
                <tr>
                    <td key={cast.name}>{cast.name}</td>
                    <td>{cast.character}</td>
                </tr>
            );
        });

        return (
            <div id="cast-information">
                <AverageCastAge age={this.props.age} />
                <table className="table table-striped col-sm-6">
                    <thead>
                        <tr>
                            <td>Name</td>
                            <td>Character</td>
                        </tr>
                    </thead>
                    <tbody>
                    {cast}
                    </tbody>
                </table>
            </div>
        );
    }
});

var MovieList = React.createClass({
    render: function() {
        var movies = this.props.movies.map(function(movie, i) {
            var boundClick = this.props.onClick.bind(this, i);
            return (
                <tr>
                    <td key={movie.title}><a href="javascript:void(0)" onClick={boundClick}>{movie.title}</a></td>
                    <td>{movie.release_date}</td>
                </tr>
            )
        }.bind(this));

        return (
            <table className="table table-striped col-sm-6">
                <thead>
                    <tr>
                        <td>Name</td>
                        <td>Release Date</td>
                    </tr>
                </thead>
                <tbody>
                {movies}
                </tbody>
            </table>
        );
    }
});

var App = React.createClass({
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
            <div id="app">
                <MovieList onClick={this.updateCast} movies={this.state.movies} />
                { this.state.cast.length ? <CastList cast={this.state.cast} age={this.state.age} /> : null }
            </div>
        )

    }
});

React.render(
    <App />,
    document.getElementById('wilhelm')
);