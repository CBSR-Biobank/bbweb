define(['./module'], function(module) {
  'use strict';

  module.controller('CentresTableCtrl', CentresTableCtrl);

  CentresTableCtrl.$inject = ['$scope', 'panelTableService', 'centres'];

  /**
   * Displays a list of centres in an ng-table.
   */
  function CentresTableCtrl($scope, panelTableService, centres) {
    var vm = this;
    vm.centres = centres;
    vm.tableParams = panelTableService.getTableParams(centres);
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473
  }

});
