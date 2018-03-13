/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular'

/**
 * The original concept for this code was taken from the URL given below.
 *
 * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
 */
/* @ngInject */
function modalService($uibModal, gettextCatalog) {
  var modalDefaults = { backdrop:    true,
                        keyboard:    true,
                        modalFade:   true,
                        template: require('./modal.html')
                      },
      modalOptions = { actionButtonText: gettextCatalog.getString('OK'),
                       headerHtml:       gettextCatalog.getString('Proceed?'),
                       bodyHtml:         gettextCatalog.getString('Perform this action?')
                     };

  var service = {
    showModal:     showModal,
    show:          show,
    modalOk:       modalOk,
    modalOkCancel: modalOkCancel
  };

  return service;

  //-------

  function showModal(customModalDefaults, customModalOptions) {
    customModalDefaults = customModalDefaults || {};
    customModalDefaults.backdrop = 'static';
    return show(customModalDefaults, customModalOptions);
  }

  function show(customModalDefaults, customModalOptions) {
    var tempModalDefaults = {},
        tempModalOptions = {};

    ModalController.$inject = ['$scope', '$uibModalInstance'];
    angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);
    angular.extend(tempModalOptions, modalOptions, customModalOptions);
    tempModalDefaults.controller = tempModalDefaults.controller || ModalController;

    return $uibModal.open(tempModalDefaults).result;

    //--

    function ModalController($scope, $uibModalInstance) {
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
      template: require('./modalOk.html')
    };
    var modalOptions = {
      headerHtml: headerHtml,
      bodyHtml: bodyHtml
    };
    return showModal(modalDefaults, modalOptions);
  }

  function modalOkCancel(headerHtml, bodyHtml) {
    var modalOptions = {
      closeButtonText: gettextCatalog.getString('Cancel'),
      headerHtml: headerHtml,
      bodyHtml: bodyHtml
    };
    return showModal({}, modalOptions);
  }

}

export default ngModule => ngModule.service('modalService', modalService)
