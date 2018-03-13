/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

let service;

/**
 * An AngularJS service that allows for the creation of a modal to let the user tag a {@link
 * domain.centres.Shipment Shipment} as {@link domain.centres.ShipmentState SENT}.
 *
 * @memberOf centres.services
 */
class ShipmentSkipToSentModalService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this, { $uibModal });
    service = this;
  }

  /**
   * Opens a modal to let the user tag a {@link domain.centres.Shipment Shipment} as {@link
   * domain.centres.ShipmentState SENT}.
   *
   * If the user presses the `OK` button, then an object containing the time the shipment was packed and
   * sent is returned.
   *
   * @return {object} The **UI Bootstrap Modal** instance.
   */
  open() {
    let modal = null;

    class ModalController {

      constructor($uibModalInstance) {
        'ngInject';
        Object.assign(this, { $uibModalInstance });

        this.timePacked = new Date();
        this.timeSent   = new Date();
      }

      timePackedOnEdit(datetime) {
        this.timePacked = datetime;
      }

      timeSentOnEdit(datetime) {
        this.timeSent = datetime;
      }

      okPressed() {
        this.$uibModalInstance.close({ timePacked: this.timePacked, timeSent: this.timeSent });
      }

      cancelPressed() {
        this.$uibModalInstance.dismiss('cancel');
      }

    }

    modal = service.$uibModal.open({
      template: require('./shipmentSkipToSentModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: true,
      modalFade: true
    });

    return modal;
  }

}

export default ngModule => ngModule.service('shipmentSkipToSentModalService',
                                           ShipmentSkipToSentModalService)
