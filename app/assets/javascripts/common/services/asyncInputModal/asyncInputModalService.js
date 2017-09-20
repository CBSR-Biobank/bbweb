/**
 *
 */
define(function () {
  'use strict';

  asyncInputModalService.$inject = ['$uibModal'];

  /*
   * This service provides a single function which opens a modal.
   */
  function asyncInputModalService($uibModal) {
    var service = {
      open: openModal
    };
    return service;

    //-------

    /**
     * A modal that allows the user to select a value returned by the server.
     *
     * @param {string} heading the heading to display as the modal's title.
     *
     * @param {string} label the label to display next to the input field.
     *
     * @param {string} placeholder a message to display in the input field when no value is present.
     *
     * @param {string} noResultsMessage the message to display to the user if the input they provided does not
     *                 yield any results.
     *
     * @param {function} getResults the function that is called to get the values from the server.
     *
     * @return {uibModalInstance} The instance of the modal that was opened. This is a ui-bootstrap class.
     */
    function openModal(heading,
                       label,
                       placeholder,
                       noResultsMessage,
                       getResults) {
      var modal = $uibModal.open({
        templateUrl: '/assets/javascripts/common/services/asyncInputModal/asyncInputModal.html',
        controller: ModalController,
        controllerAs: 'vm',
        backdrop: true,
        keyboard: true,
        modalFade: true
      });

      return modal;

      //--

      function ModalController() {
        var vm = this;

        vm.value            = undefined;
        vm.heading          = heading;
        vm.label            = label;
        vm.placeholder      = placeholder;
        vm.noResultsMessage = noResultsMessage;
        vm.okPressed        = okPressed;
        vm.closePressed     = closePressed;
        vm.getValues        = getValues;
        vm.valueSelected    = valueSelected;

        //--

        function okPressed() {
          modal.close(vm.value);
        }

        function closePressed() {
          modal.dismiss('cancel');
        }

        function getValues(viewValue) {
          return getResults(viewValue);
        }

        function valueSelected(item) {
          vm.value = item;
        }
      }
    }
  }

  return asyncInputModalService;
});
