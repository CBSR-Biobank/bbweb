/**
 * Centre administration controllers.
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  var mod = angular.module('admin.centres.controllers', ['centres.services']);

  /**
   * Displays a list of centres with each in its own mini-panel.
   *
   */
  mod.controller('CentresCtrl', [
    '$rootScope', '$scope', '$state', 'CentreService',
    function($rootScope, $scope, $state, CentreService) {
      $rootScope.pageTitle = 'Biobank centres';
      $scope.centres = [];

      CentreService.list().then(function(centres) {
        $scope.centres = _.sortBy(centres, function(centre) { return centre.name; });
      });
    }
  ]);

  /**
   * Displays a list of centres in an ng-table.
   */
  mod.controller('CentresTableCtrl', [
    '$scope',
    '$rootScope',
    '$filter',
    '$state',
    'panelTableService',
    'CentreService',
    function($scope,
             $rootScope,
             $filter,
             $state,
             panelTableService,
             CentreService) {

      var updateData = function() {
        CentreService.list().then(function(centres) {
          $scope.centres = centres;
          $scope.tableParams.reload();
        });
      };

      var getTableData = function() {
        return $scope.centres;
      };

      $rootScope.pageTitle = 'Biobank centres';
      $scope.centres = [];
      $scope.tableParams = panelTableService.getTableParamsWithCallback(getTableData);
      $scope.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473
      updateData();
    }
  ]);

  /**
   * Called to add a centre.
   */
  mod.controller('CentreAddCtrl', [
    '$scope', '$state', 'centreEditService', 'user', 'centre',
    function($scope, $state, centreEditService, user, centre) {
      $scope.title =  'Add new centre';
      $scope.centre = centre;

      var callback = function () {
        $state.go('admin.centres');
      };

      centreEditService.edit($scope, callback, callback, callback);
    }
  ]);

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  mod.controller('CentreSummaryTabCtrl', [
    '$scope', '$rootScope', '$state', '$filter', 'user', 'centre',
    function($scope, $rootScope, $state, $filter, user, centre) {

      $scope.centre = centre;
      $scope.description = $scope.centre.description;
      $scope.descriptionToggle = true;
      $scope.descriptionToggleLength = 100;

      $scope.changeStatus = function(centre) {
        if (centre.id) {
          alert('change status of ' + centre.name);
          return;
        }
        throw new Error('centre does not have an ID');
      };

      $scope.truncateDescriptionToggle = function() {
        if ($scope.descriptionToggle) {
          $scope.description = $filter('truncate')(
            $scope.centre.description, $scope.descriptionToggleLength);
        } else {
          $scope.description = $scope.centre.description;
        }
        $scope.descriptionToggle = !$scope.descriptionToggle;
      };

    }
  ]);

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  // mod.controller('CentreLocationTabCtrl', [
  //   '$scope', '$rootScope', '$state', 'centre',
  //   function($scope, $rootScope, $state, centre) {

  //   }]);


  return mod;
});
