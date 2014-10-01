define(['../module'], function(module) {
  'use strict';

  module.controller('StudiesTableCtrl', StudiesTableCtrl);

  StudiesTableCtrl.$inject = ['$filter', '$state', 'panelTableService', 'studies'];

  /**
   * Displays a list of studies in an ng-table.
   */
  function StudiesTableCtrl($filter, $state, panelTableService, studies) {
    var vm = this;
    vm.studies = studies;
    vm.tableParams = panelTableService.getTableParams(vm.studies);
    vm.tableParams.settings().vm = vm;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473
  }

});
