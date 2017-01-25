/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  annotationTypeUpdateModalService.$inject = [ '$uibModal' ];

  /**
   * A modal that allows the user to modify an SELECT annotation type.
   *
   * @param {object} $uibModal - The "UI Bootstrap" modal service.
   *
   * @return {object} An AngularJS service.
   */
  function annotationTypeUpdateModalService($uibModal) {
    var service = {
      openModal: openModal
    };
    return service;

    //-------

    /**
     * Opens a modal, where the user is allowed to add, remove, or change the options associated with a Select
     * annotation type.
     *
     * @param {domain.annotations.AnnotationType} annotationType - The annotation type to modify.
     *
     * @return {object} The "UI Bootstrap" modal instance.
     */
    function openModal(annotationType) {
      var modal;

      if (!annotationType.isSingleSelect() && !annotationType.isMultipleSelect()) {
        throw new Error('invalid annotation type: ' + annotationType.valueType);
      }

      modal = $uibModal.open({
        templateUrl: '/assets/javascripts/admin/services/annotationTypeUpdateModal/annotationTypeUpdateModal.html',
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

        vm.options              = _.clone(annotationType.options);
        vm.optionAdd            = optionAdd;
        vm.optionRemove         = optionRemove;
        vm.removeButtonDisabled = removeButtonDisabled;
        vm.okPressed            = okPressed;
        vm.closePressed         = closePressed;

        //--

        /*
         * Used to add an option. Should only be called when the value type is 'Select'.
         */
        function optionAdd() {
          vm.options.push('');
        }

        /*
         * Used to remove an option. Should only be called when the value type is 'Select'.
         */
        function optionRemove(index) {
          if (vm.options.length <= 1) {
            throw new Error('options is empty, cannot remove any more options');
          }
          vm.options.splice(index, 1);
        }

        /*
         * Determines if the remove button for an option is disabled.
         *
         * It is disabled only when there is a single option available.
         */
        function removeButtonDisabled() {
          return (vm.options.length <= 1);
        }

        function okPressed() {
          modal.close(vm.options);
        }

        function closePressed() {
          modal.dismiss('cancel');
        }
      }

    }

  }

  return annotationTypeUpdateModalService;
});
