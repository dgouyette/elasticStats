/*jshint globalstrict:true */
/*global angular:true */
'use strict';

angular.module('controllers', [])

.controller('SearchCtrl', function($scope, ejsResource, $http) {
        $scope.loadAnnees = function() {
            $http.get("/annees.json").success(function(data) {
                $scope.chartData = data;
            });
        };

        $scope.loadStatuts = function() {
            $http.get("/statuts.json").success(function(data) {
                $scope.chartData = data;
            });
        };

    });