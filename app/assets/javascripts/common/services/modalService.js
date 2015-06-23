/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
  'use strict';

  modalService.$inject = ['$modal'];

  /**
   * Originally the code was taken from the URL given below, but then it was modified.
   *
   * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
   *
   */
  function modalService($modal) {
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

      controller.$inject = ['$scope', '$modalInstance'];

      angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);
      angular.extend(tempModalOptions, modalOptions, customModalOptions);

      if (!tempModalDefaults.controller) {
        tempModalDefaults.controller = controller;
      }

      return $modal.open(tempModalDefaults).result;

      //--

      function controller($scope, $modalInstance) {
        $scope.modalOptions = tempModalOptions;
        $scope.modalOptions.ok = function (result) {
          $modalInstance.close(result);
        };
          $scope.modalOptions.close = function () {
            $modalInstance.dismiss('cancel');
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

      controller.$inject = ['$scope', '$modalInstance', 'defaultValue'];

      return $modal.open({
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

      function controller ($scope, $modalInstance, defaultValue) {
        $scope.modal = {
          value: defaultValue,
          type: type,
          title: title,
          label: label
        };

        $scope.modal.ok = function () {
          $modalInstance.close($scope.modal.value);
        };
        $scope.modal.close = function () {
          $modalInstance.dismiss('cancel');
        };
      }
    }

    /**
     * Displays a modal asking for current password, new password, and confirm new password.
     */
    function passwordUpdateModal() {
      controller.$inject = [ '$scope', '$modalInstance'];

      return $modal.open({
        templateUrl: '/assets/javascripts/common/services/passwordUpdateModal.html',
        controller:  controller,
        backdrop:    true,
        keyboard:    true,
        modalFade:   true
      }).result;

      //---

      function controller($scope, $modalInstance) {
        $scope.model = {
          currentPassword: '',
          newPassword:     '',
          confirmPassword: '',
          ok:              onOk,
          close:           onClose
        };

        function onOk() {
          $modalInstance.close({
            currentPassword: $scope.model.currentPassword,
            newPassword: $scope.model.newPassword
          });
        }

        function onClose() {
          $modalInstance.dismiss('cancel');
        }
      }
    }

  }

  return modalService;
});
