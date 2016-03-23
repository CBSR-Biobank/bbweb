/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  modalService.$inject = ['$uibModal'];

  /**
   * Originally the code was taken from the URL given below, but then it was modified.
   *
   * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
   *
   */
  function modalService($uibModal) {
    var modalDefaults = { backdrop: true,
                          keyboard: true,
                          modalFade: true,
                          templateUrl: '/assets/javascripts/common/modal.html'
                        },
        modalOptions = { actionButtonText: 'OK', //closeButtonText: 'Close',
                         headerHtml: 'Proceed?',
                         bodyHtml: 'Perform this action?'
                       };

    var service = {
      showModal: showModal,
      show:      show,
      modalOk:   modalOk
    };

    return service;

    //-------

    function showModal(customModalDefaults, customModalOptions) {
      if (!customModalDefaults) { customModalDefaults = {}; }
      customModalDefaults.backdrop = 'static';
      return show(customModalDefaults, customModalOptions);
    }

    function show(customModalDefaults, customModalOptions) {
      var tempModalDefaults = {},
          tempModalOptions = {};

      controller.$inject = ['$scope', '$uibModalInstance'];

      angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);
      angular.extend(tempModalOptions, modalOptions, customModalOptions);

      if (!tempModalDefaults.controller) {
        tempModalDefaults.controller = controller;
      }

      return $uibModal.open(tempModalDefaults).result;

      //--

      function controller($scope, $uibModalInstance) {
        $scope.modalOptions = tempModalOptions;
        $scope.modalOptions.ok = function (result) {
          $uibModalInstance.close(result);
        };
          $scope.modalOptions.close = function () {
            $uibModalInstance.dismiss('cancel');
          };
      }
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

  return modalService;
});
