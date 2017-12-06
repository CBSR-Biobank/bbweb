/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * @param {<domain.AnnotationType>} annotationType the Annotation type to display.
 *
 * @param {boolean} readOnly When FALSE, user is allowed to make changes to the annotation type.
 *
 * @param {function} onUpdate the function has two parameters: the attribute that was modified ({string}), and
 * the updated annotation type ({@link domain.AnnotationType}).
 */
var component = {
  template: require('./annotationTypeView.html'),
  controller: AnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    annotationType: '<',
    readOnly:       '<',
    onUpdate:       '&'
  }
};

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

export default ngModule => ngModule.component('annotationTypeView', component)
