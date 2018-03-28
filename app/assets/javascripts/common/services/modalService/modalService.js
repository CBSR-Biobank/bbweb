/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS Service that provides methods for opening *UI Bootstrap* modals.
 *
 * The original concept for this code was taken from the URL given below.
 *
 * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
 *
 * @memberOf common.services
 */
class ModalService {

  constructor($uibModal, gettextCatalog) {
    'ngInject';
    Object.assign(this, { $uibModal, gettextCatalog });
    this.modalDefaults = {
      backdrop:    true,
      keyboard:    true,
      modalFade:   true,
      template: require('./modal.html')
    };

    this.modalOptions = {
      actionButtonText: gettextCatalog.getString('OK'),
      headerHtml:       gettextCatalog.getString('Proceed?'),
      bodyHtml:         gettextCatalog.getString('Perform this action?')
    };
  }

  /**
   * Opens a modal with the given options.
   *
   * @param {object} customModalDefaults - defaults used to open the modal.
   *
   * @param {object} customModalOptions - options available to the modal through the scope.
   */
  show(customModalDefaults, customModalOptions) {
    const tempModalDefaults = {},
          tempModalOptions = {};

    class ModalController {

      constructor($scope, $uibModalInstance) {
        'ngInject';
        $scope.modalOptions = tempModalOptions;
        $scope.modalOptions.ok = (result) => {
          $uibModalInstance.close(result);
        };
        $scope.modalOptions.close = () => {
          $uibModalInstance.dismiss('cancel');
        };
      }
    }

    Object.assign(tempModalDefaults, this.modalDefaults, customModalDefaults);
    Object.assign(tempModalOptions, this.modalOptions, customModalOptions);
    tempModalDefaults.controller = tempModalDefaults.controller || ModalController;

    return this.$uibModal.open(tempModalDefaults).result;
  }

  /**
   * Opens a modal with the given options.
   *
   * @param {object} customModalDefaults - defaults used to open the modal.
   *
   * @param {object} customModalOptions - options available to the modal through the scope.
   */
  showModal(customModalDefaults = {}, customModalOptions) {
    customModalDefaults.backdrop = 'static';
    return this.show(customModalDefaults, customModalOptions);
  }

  /**
   * Opens a modal with a single button labelled with `OK`.
   *
   * @param {string} headerHTML - string that may contain HTML tags shown in the modal's header.
   *
   * @param {string} bodyHTML - string that may contain HTML tags shown in the modal's body.
   */
  modalOk(headerHtml, bodyHtml) {
    const modalDefaults = {
      template: require('./modalOk.html')
    };
    const modalOptions = {
      headerHtml: headerHtml,
      bodyHtml: bodyHtml
    };
    return this.showModal(modalDefaults, modalOptions);
  }

  /**
   * Opens a modal with a two buttons: labelled with `OK` and `Cancel`.
   *
   * @param {string} headerHTML - string that may contain HTML tags shown in the modal's header.
   *
   * @param {string} bodyHTML - string that may contain HTML tags shown in the modal's body.
   */
  modalOkCancel(headerHtml, bodyHtml) {
    const modalOptions = {
      closeButtonText: this.gettextCatalog.getString('Cancel'),
      headerHtml: headerHtml,
      bodyHtml: bodyHtml
    };
    return this.showModal({}, modalOptions);
  }

}

export default ngModule => ngModule.service('modalService', ModalService)
