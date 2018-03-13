/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * An AngularJS service that allows the creation of a modal that modifies an {@link domain.AnnotationType
 * AnnotationType} with SELECT {@link domain.AnnotationValueType valueType}.
 *
 * @memberOf admin.common.services
 *
 * @param {UI_Bootstrap_Modal} $uibModal
 */
class AnnotationTypeUpdateModalService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this, { $uibModal });
  }

  /**
   * Opens a modal, where the user is allowed to add, remove, or change the options associated with a Select
   * annotation type.
   *
   * @param {domain.annotations.AnnotationType} annotationType - The annotation type to modify.
   *
   * @return {object} The "UI Bootstrap" modal instance.
   */
  openModal(annotationType) {
    var modal;

    if (!annotationType.isSingleSelect() && !annotationType.isMultipleSelect()) {
      throw new Error('invalid annotation type: ' + annotationType.valueType);
    }

    modal = this.$uibModal.open({
      template: require('./annotationTypeUpdateModal.html'),
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

export default ngModule => ngModule.service('annotationTypeUpdateModal', AnnotationTypeUpdateModalService)
