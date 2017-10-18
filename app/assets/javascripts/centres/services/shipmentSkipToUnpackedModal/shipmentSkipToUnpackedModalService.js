/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Opens a modal that allows the user to select a centre location.
 */
/* @ngInject */
function shipmentSkipToUnpackedModalService($uibModal) {
  var service = {
    open: openModal
  };
  return service;

  //-------

  function openModal() {
    var modal;

    modal = $uibModal.open({
      template: require('./shipmentSkipToUnpackedModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: true,
      modalFade: true
    });

    ModalController.$inject = [
      '$uibModalInstance'
    ];

    return modal;

    //--

    function ModalController($uibModalInstance) {
      var vm = this;

      vm.timeReceived       = new Date();
      vm.timeUnpacked       = new Date();
      vm.timeReceivedOnEdit = timeReceivedOnEdit;
      vm.timeUnpackedOnEdit = timeUnpackedOnEdit;
      vm.okPressed          = okPressed;
      vm.cancelPressed      = cancelPressed;

      //---

      function timeReceivedOnEdit(datetime) {
        vm.timeReceived = datetime;
      }

      function timeUnpackedOnEdit(datetime) {
        vm.timeUnpacked = datetime;
      }

      function okPressed() {
        $uibModalInstance.close({ timeReceived: vm.timeReceived, timeUnpacked: vm.timeUnpacked });
      }

      function cancelPressed() {
        $uibModalInstance.dismiss('cancel');
      }
    }
  }

}

export default ngModule => ngModule.service('shipmentSkipToUnpackedModalService',
                                           shipmentSkipToUnpackedModalService)
