/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * @class ng.admin.common.components.annotationTypeView
 *
 * An AngularJS component that allows the user to view a {@link domain.AnnotationType}.
 *
 * @memberOf ng.admin.common.components
 *
 * @param {domain.AnnotationType} annotationType the Annotation type to display.
 *
 * @param {boolean} readOnly When *FALSE*, user is allowed to make changes to the annotation type.
 *
 * @param {ng.admin.common.components.annotationTypeView.onUpdate} [onUpdate] Used only if `readOnly` is
 * *False*. This function called after the user made a change to the annotation type.
 */
var annotationTypeView = {
  template: require('./annotationTypeView.html'),
  controller: AnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    annotationType: '<',
    readOnly:       '<',
    onUpdate:       '&'
  }
};

/**
 * The callback function called by component {@link ng.admin.common.components.annotationTypeView
 * annotationTypeView} after the user has made changes to the annotation type.
 *
 * @callback ng.admin.common.components.annotationTypeView.onUpdate
 * @param {string} attribute - the attribute that was modified
 * @param {domain.AnnotationType} annotationType - the updated annotation type.
 */

/* @ngInject */
function AnnotationTypeViewController($state,
                                      gettextCatalog,
                                      modalInput,
                                      annotationTypeUpdateModal,
                                      annotationValueTypeLabelService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.annotationTypeValueTypeLabel =
      annotationValueTypeLabelService.valueTypeToLabelFunc(vm.annotationType.valueType,
                                                           vm.annotationType.isSingleSelect());

    vm.editName                     = editName;
    vm.editRequired                 = editRequired;
    vm.editDescription              = editDescription;
    vm.editSelectionOptions         = editSelectionOptions;
    vm.back                         = back;
  }

  function editName() {
    modalInput.text(gettextCatalog.getString('Edit Annotation name'),
                    gettextCatalog.getString('Name'),
                    vm.annotationType.name,
                    { required: true, minLength: 2 }).result
      .then(function (name) {
        vm.annotationType.name = name;
        vm.onUpdate()('name', vm.annotationType);
      });
  }

  function editDescription() {
    modalInput.textArea(gettextCatalog.getString('Edit Annotation description'),
                        gettextCatalog.getString('Description'),
                        vm.annotationType.description)
      .result.then(function (description) {
        var annotationType = _.extend({}, vm.annotationType, { description: description });
        vm.onUpdate()('description', annotationType);
      });
  }

  function editRequired() {
    modalInput.boolean(gettextCatalog.getString('Edit Annotation required'),
                       gettextCatalog.getString('Required'),
                       vm.annotationType.required.toString(),
                       { required: true }).result
      .then(function (required) {
        vm.annotationType.required = (required === 'true' );
        vm.onUpdate()('required', vm.annotationType);
      });
  }

  function editSelectionOptions() {
    annotationTypeUpdateModal.openModal(vm.annotationType).result.then(function (options) {
      var annotationType = _.extend({}, vm.annotationType, { options: options });
      vm.onUpdate()(annotationType);
    });
  }

  function back() {
    $state.go('^', {}, { reload: true });
  }

}

export default ngModule => ngModule.component('annotationTypeView', annotationTypeView)
