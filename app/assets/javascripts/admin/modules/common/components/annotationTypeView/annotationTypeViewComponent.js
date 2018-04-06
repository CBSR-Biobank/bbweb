/**
 * AngularJS Components used in Administration modules
 *
 * @namespace admin.common.components.annotationTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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

    vm.editName             = editName;
    vm.editRequired         = editRequired;
    vm.editDescription      = editDescription;
    vm.editSelectionOptions = editSelectionOptions;
    vm.back                 = back;
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
        var annotationType = Object.assign({}, vm.annotationType, { description: description });
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
      var annotationType = Object.assign({}, vm.annotationType, { options: options });
      vm.onUpdate()(annotationType);
    });
  }

  function back() {
    $state.go('^', {}, { reload: true });
  }

}

/**
 * An AngularJS component that allows the user to view a {@link domain.annotations.AnnotationType}.
 *
 * @memberOf admin.common.components.annotationTypeView
 *
 * @param {domain.annotations.AnnotationType} annotationType the Annotation type to display.
 *
 * @param {boolean} readOnly When *FALSE*, user is allowed to make changes to the annotation type.
 *
 * @param {admin.common.components.annotationTypeView.onUpdate} [onUpdate] Used only if `readOnly` is
 * *False*. This function called after the user made a change to the annotation type.
 */
const annotationTypeViewComponent = {
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
 * The callback function called by {@link
 * ng.admin.common.components.annotationTypeView.annotationTypeViewComponent annotationTypeViewComponent}
 * after the user has made changes to the annotation type.
 *
 * @callback admin.common.components.annotationTypeView.onUpdate
 * @param {string} attribute - the attribute that was modified
 * @param {domain.annotations.AnnotationType} annotationType - the updated annotation type.
 */

export default ngModule => ngModule.component('annotationTypeView', annotationTypeViewComponent)
