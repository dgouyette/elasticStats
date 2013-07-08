'use strict';

angular.module('statsApp', [
        'controllers',
        'dangle',
        'elasticjs.filters',
        'elasticjs.services',
        'elasticjs.directives',
        'elasticjs.service'
    ]).config(['$routeProvider', function($routeProvider) {
        $routeProvider
            .when('/search', {
                templateUrl: '/assets/search.html'
            })
            .when('/results', {
                templateUrl: '/assets/results.html'
            })
            .otherwise({
                redirectTo: '/search'
            });
    }]);