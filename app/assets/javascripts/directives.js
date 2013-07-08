/*jshint globalstrict:true */
/*global angular:true */
'use strict';

angular.module('elasticjs.directives', [])
    .directive('appVersion', ['version', function (version) {
        return function (scope, elm, attrs) {
            elm.text(version);
        };
    }]).directive('bar', function () {
        return {
            restrict: 'E',
            scope: {
                onClick: '=',
                bind: '=',
                field: '@'
            },
            link: function (scope, element, attrs) {

                scope.$watch('bind', function (data) {

                    if (data) {
                        // provide new input domain to the x,y scales
                        x.domain([0, d3.max(data, function (d) {
                            return d.count;
                        })]);
                        y.domain(data.map(function (d) {
                            return d.term;
                        }));
                    }

                });


                var width = 300;
                var height = 250;

                var x = d3.scale.linear().range([0, width]);
                var y = d3.scale.ordinal().rangeBands([0, height], .1);

                var svg = d3.select(element[0])
                    .append('svg')
                    .attr('preserveAspectRatio', 'xMaxYMin meet')
                    .attr('viewBox', '0 0 ' + (width + 75) + ' ' + height)
                    .append('g');


            }
        };
    })

    .directive('chart', function () {
        return {
            restrict: 'E',
            template: '<div></div>',
            scope: {
                chartData: "=value"
            },
            transclude:true,
            replace: true,


            link: function (scope, element, attrs) {
                var chartsDefaults = {
                    chart: {
                        renderTo: element[0],
                        type: attrs.type || null,
                        height: attrs.height || null,
                        width: attrs.width || null
                    }
                };

                //Update when charts data changes
                scope.$watch(function() { return scope.chartData; }, function(value) {
                    if(!value) return;

                    console.log(value)
                    // We need deep copy in order to NOT override original chart object.
                    // This allows us to override chart data member and still the keep
                    // our original renderTo will be the same
                    var deepCopy = true;
                    var newSettings = {};
                    $.extend(deepCopy, newSettings, chartsDefaults, scope.chartData);
                    var chart = new Highcharts.Chart(newSettings);
                });
            }
        }

    });

;




