/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

let service;

/**
 * An AngularJS service that allows for the creation of a modal to let the user tag a {@link
 * domain.centres.Shipment Shipment} as {@link domain.centres.ShipmentState UNPACKED}.
 *
 * @memberOf centres.services
 */
class ShipmentSkipToUnpackedModalService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this, { $uibModal });
    service = this;
  }

  /**
   * Opens a modal to let the user tag a {@link domain.centres.Shipment Shipment} as {@link
   * domain.centres.ShipmentState UNPACKED}.
   *
   * If the user presses the `OK` button, then an object containing the time the shipment was received and
   * unpacked is returned.
   *
   * @return {object} The **UI Bootstrap Modal** instance.
   */
  open() {
    let modal = null;

    class ModalController {

      constructor($uibModalInstance) {
        'ngInject';
        Object.assign(this, { $uibModalInstance });

        this.timeReceived = new Date();
        this.timeUnpacked = new Date();
      }

       timeReceivedOnEdit(datetime) {
        this.timeReceived = datetime;
      }

       timeUnpackedOnEdit(datetime) {
        this.timeUnpacked = datetime;
      }

       okPressed() {
         this.$uibModalInstance.close({
           timeReceived: this.timeReceived,
           timeUnpacked: this.timeUnpacked
         });
      }

       cancelPressed() {
        this.$uibModalInstance.dismiss('cancel');
      }
    }

    modal = service.$uibModal.open({
      template: require('./shipmentSkipToUnpackedModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: true,
      modalFade: true
    });

    return modal;

  }

}

export default ngModule => ngModule.service('shipmentSkipToUnpackedModalService',
                                           ShipmentSkipToUnpackedModalService)
