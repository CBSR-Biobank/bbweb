define(['../module', 'angular'], function(module, angular) {
  'use strict';

  module.service('domainEntityModalService', domainEntityModalService);

  domainEntityModalService.$inject = ['$modal', 'tableService'];

  function domainEntityModalService($modal, tableService) {
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
        var tableParameters = { count: 20 };
        var tableSettings = { counts: [] };

        $scope.modalOptions = {};
        $scope.modalOptions.title = title;
        $scope.modalOptions.data = data;
        $scope.modalOptions.tableParams =
          tableService.getTableParams($scope.modalOptions.data, tableParameters, tableSettings);
        $scope.modalOptions.ok = ok;

        //--

        function ok() {
          $modalInstance.close();
        }
      }];

      return $modal.open(tempModalDefaults).result;
    }
  }

});
