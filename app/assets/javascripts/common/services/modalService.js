/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
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
      showModal:           showModal,
      show:                show,
      modalOk:             modalOk,
      modalStringInput:    modalStringInput,
      passwordUpdateModal: passwordUpdateModal
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

    /**
     * Displays a modal asking user to enter a string.
     */
    function modalStringInput(type,
                              title,
                              label,
                              defaultValue) {

      controller.$inject = ['$scope', '$uibModalInstance', 'defaultValue'];

      return $uibModal.open({
        templateUrl: '/assets/javascripts/common/services/modalStringInput.html',
        controller: controller,
        resolve: {
          defaultValue: function () {
            return defaultValue;
          }
        },
        backdrop: true,
        keyboard: true,
        modalFade: true
      }).result;

      //--

      function controller ($scope, $uibModalInstance, defaultValue) {
        $scope.modal = {
          value: defaultValue,
          type: type,
          title: title,
          label: label
        };

        $scope.modal.ok = function () {
          $uibModalInstance.close($scope.modal.value);
        };
        $scope.modal.close = function () {
          $uibModalInstance.dismiss('cancel');
        };
      }
    }

    /**
     * Displays a modal asking for current password, new password, and confirm new password.
     */
    function passwordUpdateModal() {
      controller.$inject = [ '$scope', '$uibModalInstance'];

      return $uibModal.open({
        templateUrl: '/assets/javascripts/common/services/passwordUpdateModal.html',
        controller:  controller,
        backdrop:    true,
        keyboard:    true,
        modalFade:   true
      }).result;

      //---

      function controller($scope, $uibModalInstance) {
        $scope.model = {
          currentPassword: '',
          newPassword:     '',
          confirmPassword: '',
          ok:              onOk,
          close:           onClose
        };

        function onOk() {
          $uibModalInstance.close({
            currentPassword: $scope.model.currentPassword,
            newPassword: $scope.model.newPassword
          });
        }

        function onClose() {
          $uibModalInstance.dismiss('cancel');
        }
      }
    }

  }

  return modalService;
});
