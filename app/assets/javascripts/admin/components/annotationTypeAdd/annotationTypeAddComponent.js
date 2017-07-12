/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Used for all 3 different study annotation types: collection event, participant and specimen link
   * annotation types.
   *
   * Property 'onSubmit' is the function that is called when the user submits the form.
   *
   * @return {object} An AngularJS directive.
   */
  var component = {
    templateUrl : '/assets/javascripts/admin/components/annotationTypeAdd/annotationTypeAdd.html',
    controller: AnnotationTypeAddController,
    controllerAs: 'vm',
    bindings: {
      onSubmit: '&', // annotation type is passed as parameter
      onCancel: '&'
    }
  };

  AnnotationTypeAddController.$inject = [
    'AnnotationType',
    'AnnotationValueType',
    'annotationValueTypeLabelService'
  ];
  /*
   * Controller for this component.
   */
  function AnnotationTypeAddController(AnnotationType,
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

  return component;
});
