define(['../module', 'angular'], function(module, angular) {
  'use strict';

  module.service('domainEntityModalService', domainEntityModalService);

  domainEntityModalService.$inject = ['$modal', 'panelTableService'];

  function domainEntityModalService($modal, panelTableService) {
    var modalDefaults = {
      backdrop: true,
      keyboard: true,
      modalFade: true,
      templateUrl: '/assets/javascripts/common/services/domainEntityModal.html'
    };

    var service = {
      show: show
    };

    return service;

    //---

    function show (title, data) {
      var tempModalDefaults = {};
      angular.extend(tempModalDefaults, modalDefaults);

      tempModalDefaults.controller = ['$scope', '$modalInstance', function ($scope, $modalInstance) {
        var tableParameters = { count: 20, sorting: {} };
        var tableSettings = { counts: [] };

        $scope.modalOptions = {
          title: title,
          data: data
        };

        $scope.modalOptions.tableParams =
          panelTableService.getTableParams($scope.modalOptions.data, tableParameters, tableSettings);

        $scope.modalOptions.ok = function () {
          $modalInstance.close();
        };
      }];

      return $modal.open(tempModalDefaults).result;
    }
  }

});
