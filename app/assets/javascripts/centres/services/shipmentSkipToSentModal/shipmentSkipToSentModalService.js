/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  shipmentSkipToSentModalService.$inject = [
    '$uibModal'
  ];

  /**
   * Opens a modal that allows the user to select a centre location.
   */
  function shipmentSkipToSentModalService($uibModal) {
    var service = {
      open: openModal
    };
    return service;

    //-------

    function openModal() {
      var modal;

      modal = $uibModal.open({
        templateUrl: '/assets/javascripts/centres/services/shipmentSkipToSentModal/shipmentSkipToSentModal.html',
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

        vm.timePacked       = new Date();
        vm.timeSent         = new Date();
        vm.timePackedOnEdit = timePackedOnEdit;
        vm.timeSentOnEdit   = timeSentOnEdit;
        vm.okPressed        = okPressed;
        vm.cancelPressed    = cancelPressed;

        //---

        function timePackedOnEdit(datetime) {
          vm.timePacked = datetime;
        }

        function timeSentOnEdit(datetime) {
          vm.timeSent = datetime;
        }

        function okPressed() {
          $uibModalInstance.close({ timePacked: vm.timePacked, timeSent: vm.timeSent });
        }

        function cancelPressed() {
          $uibModalInstance.dismiss('cancel');
        }
      }
    }

  }

  return shipmentSkipToSentModalService;
});
