/**
 * Tabs used when displaying a study.
 */
define(['angular', '../controllers'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('study.directives.studyTabs', []);
  mod.directive('studySummaryTab', function() {
    return {
      restrict: 'E',
      templateUrl: '/assets/templates/study/studySummaryTab.html'
    };
  });

  mod.directive('studyParticipantsTab', ['$log', '$route', '$modal', '$filter', 'ngTableParams', 'studyService', 'studyService', function($log, $route, $modal, $filter, ngTableParams, studyService) {
    return {
      restrict: 'E',
      templateUrl: '/assets/templates/study/studyParticipantsTab.html',
      controller: function($scope) {
        /* jshint ignore:start */
        $scope.tableParams = new ngTableParams({
          page: 1,            // show first page
          count: 10,          // count per page
          sorting: {
            name: 'asc'     // initial sorting
          }
        }, {
          counts: [], // hide page counts control
          total: 0,           // length of data
          getData: function($defer, params) {
            var study = { id: $route.current.params.id };
            studyService.participantInfo(study).then(function(response) {
              var orderedData = params.sorting()
                ? $filter('orderBy')(response.data, params.orderBy())
                : response.data;
              params.total(orderedData.length);
              $defer.resolve(orderedData.slice(
                (params.page() - 1) * params.count(),
                params.page() * params.count()));
            });
          }
        });
        /* jshint ignore:end */

        $scope.changeSelection = function(annotType) {
          $log.debug(annotType);

          var modalInstance = $modal.open({
            templateUrl: '/assets/templates/study/annotationType.html',
            controller: controllers.AnnotationTypeCtrl,
            size: 'sm',
            resolve: {
              annotType: function () {
                return annotType;
              }
            }
          });

          modalInstance.result.then(function (selectedItem) {
            $scope.selected = selectedItem;
          });
        };
      }
    };
  }]);

  return mod;
});
