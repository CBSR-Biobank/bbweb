/**
 * AngularJS Components used in Administration modules
 *
 * @namespace admin.common.components.annotationTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS component that allows the user to add an {@link domain.AnnotationType AnnotationType} using an
 * HTML form. The annotation type can then be added to one of these domain entities:
 *
 * - {@link domain.studies.CollectionEventType CollectionEventType}
 * - {@link domain.studies.Study Study}
 * - specimen link.
 *
 * @memberOf admin.common.components.annotationTypeAdd
 *
 * @param {admin.common.components.annotationTypeAdd.onSubmit} onSubmit The function that is called when
 * the user submits the form.
 *
 * @param {admin.common.components.annotationTypeAdd.onCancel} onCancel The function that is called when the
 * user presses the *Cancel* button on the form.
 */
const annotationTypeAddComponent = {
  template: require('./annotationTypeAdd.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    onSubmit: '&', // annotation type is passed as parameter
    onCancel: '&'
  }
};

/**
 * The callback function called by {@link
 * ng.admin.common.components.annotationTypeAdd.annotationTypeAddComponent annotationTypeAddComponent} after
 * the user presses the *Submit* button in the component's HTML form.
 *
 * @callback admin.common.components.annotationTypeAdd.onSubmit
 * @param {domain.AnnotationType} annotationType - the annotation type with the values entered by the user.
 */

/**
 * The callback function called by {@link
 * ng.admin.common.components.annotationTypeAdd.annotationTypeAddComponent annotationTypeAddComponent} after
 * the user presses the *Cancel* button in the component's HTML form.
 *
 * @callback admin.common.components.annotationTypeAdd.onCancel
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function Controller(AnnotationType,
                    AnnotationValueType,
                    annotationValueTypeLabelService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.annotationType        = new AnnotationType();
    vm.valueTypes            = annotationValueTypeLabelService.getLabels();

    vm.valueTypeChange       = valueTypeChange;
    vm.maxValueCountRequired = maxValueCountRequired;
    vm.optionAdd             = optionAdd;
    vm.optionRemove          = optionRemove;
    vm.removeButtonDisabled  = removeButtonDisabled;
    vm.submit                = submit;
    vm.cancel                = cancel;
  }

  /*
   * Called when the user changes the annotation type's value type.
   */
  function valueTypeChange() {
    vm.annotationType.valueTypeChanged();
  }

  /*
   * Used to disable the submit button if the user has not entered a value for the type
   * of selection: either single selection or multiple selections.
   */
  function maxValueCountRequired() {
    return ! vm.annotationType.isMaxValueCountValid();
  }

  /*
   * Used to add an option. Should only be called when the value type is 'Select'.
   */
  function optionAdd() {
    vm.annotationType.addOption();
  }

  /*
   * Used to remove an option. Should only be called when the value type is 'Select'.
   */
  function optionRemove(index) {
    vm.annotationType.removeOption(index);
  }

  /*
   * Determines if the remove button for an option is disabled.
   *
   * It is disabled only when there is a single option available.
   */
  function removeButtonDisabled() {
    return (vm.annotationType.options.length <= 1);
  }

  /*
   * Called when the user presses the submit button on the form.
   */
  function submit(annotationType) {
    annotationType.maxValueCount = parseInt(annotationType.maxValueCount);
    if (!annotationType.isValueTypeSelect()) {
      delete annotationType.maxValueCount;
    }
    vm.onSubmit()(annotationType);
  }

  /*
   * Called when the user presses the cancel button on the form.
   */
  function cancel() {
    vm.onCancel()();
  }

}

export default ngModule => ngModule.component('annotationTypeAdd', annotationTypeAddComponent)
