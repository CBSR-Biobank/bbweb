define(['../module', 'angular'], function(module, angular) {
  'use strict';

  module.service('modalService', ModalService);

  ModalService.$inject = ['$modal'];

  /**
   * Originally the code was taken from the URL given below, but then it was modified.
   *
   * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
   *
   */
  function ModalService($modal) {
    var modalDefaults = {
      backdrop: true,
      keyboard: true,
      modalFade: true,
      templateUrl: '/assets/javascripts/common/modal.html'
    };
    var modalOptions = {
      //closeButtonText: 'Close',
      actionButtonText: 'OK',
      headerHtml: 'Proceed?',
      bodyHtml: 'Perform this action?'
    };
    var service = {
      showModal: showModal,
      show: show,
      modalOk: modalOk
    };

    return service;

    //-------

    function showModal(customModalDefaults, customModalOptions) {
      if (!customModalDefaults) { customModalDefaults = {}; }
      customModalDefaults.backdrop = 'static';
      return show(customModalDefaults, customModalOptions);
    }

    function show(customModalDefaults, customModalOptions) {
      //Create temp objects to work with since we're in a singleton service
      var tempModalDefaults = {};
      var tempModalOptions = {};

      //Map angular-ui modal custom defaults to modal defaults defined in service
      angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

      //Map modal.html $scope custom properties to defaults defined in service
      angular.extend(tempModalOptions, modalOptions, customModalOptions);

      if (!tempModalDefaults.controller) {
        tempModalDefaults.controller = ['$scope', '$modalInstance', function ($scope, $modalInstance) {
          $scope.modalOptions = tempModalOptions;
          $scope.modalOptions.ok = function (result) {
            $modalInstance.close(result);
          };
          $scope.modalOptions.close = function () {
            $modalInstance.dismiss('cancel');
          };
        }];
      }

      return $modal.open(tempModalDefaults).result;
    }

    function modalOk(headerHtml, bodyHtml) {
      var modalDefaults = {
        templateUrl: '/assets/javascripts/common/modalOk.html'
      };
      var modalOptions = {
        headerHtml: headerHtml,
        bodyHtml: bodyHtml
      };
      return showModal(modalDefaults, modalOptions);
    }
  }

});
