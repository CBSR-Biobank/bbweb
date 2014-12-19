define(['../module'], function(module) {
  'use strict';

  module.service('domainEntityModalService', domainEntityModalService);

  domainEntityModalService.$inject = ['$modal', 'tableService'];

  function domainEntityModalService($modal, tableService) {
    var service = {
      show: show
    };

    return service;

    //---

    function show (title, data) {
      var modalOptions = {
        backdrop: true,
        keyboard: true,
        modalFade: true,
        templateUrl: '/assets/javascripts/common/services/domainEntityModal.html',
        controller: controller
      };

      controller.$inject = ['$scope', '$modalInstance'];

      function controller($scope, $modalInstance) {
        var tableParameters = { count: 20 };
        var tableSettings = { counts: [] };

        $scope.modalOptions = {
          title: title,
          data: data,
          tableParams: tableService.getTableParams(data, tableParameters, tableSettings),
          ok: ok
        };

        //--

        function ok() {
          $modalInstance.close();
        }
      }

      return $modal.open(modalOptions).result;
    }
  }

});
