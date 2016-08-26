/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   * Used for all 3 different study annotation types: collection event, participant and specimen link
   * annotation types.
   *
   * Property 'onSubmit' is the function that is called when the user submits the form.
   */
  function annotationTypeAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        onSubmit: '&', // annotation type is passed as parameter
        onCancel: '&'
      },
      templateUrl : '/assets/javascripts/admin/directives/annotationTypeAdd/annotationTypeAdd.html',
      controller: AnnotationTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  AnnotationTypeAddCtrl.$inject = [
    'AnnotationType',
    'AnnotationValueType'
  ];

  function AnnotationTypeAddCtrl(AnnotationType,
                                 AnnotationValueType) {
    var vm = this;

    vm.annotationType        = new AnnotationType();
    vm.valueTypes            = _.values(AnnotationValueType);

    vm.valueTypeChange       = valueTypeChange;
    vm.maxValueCountRequired = maxValueCountRequired;
    vm.optionAdd             = optionAdd;
    vm.optionRemove          = optionRemove;
    vm.removeButtonDisabled  = removeButtonDisabled;
    vm.submit                = submit;
    vm.cancel                = cancel;

    //--

    /**
     * Called when the user changes the annotation type's value type.
     */
    function valueTypeChange() {
      vm.annotationType.valueTypeChanged();
    }

    /**
     * Used to disable the submit button if the user has not entered a value for the type
     * of selection: either single selection or multiple selections.
     */
    function maxValueCountRequired() {
      return ! vm.annotationType.isMaxValueCountValid();
    }

    /**
     * Used to add an option. Should only be called when the value type is 'Select'.
     */
    function optionAdd() {
      vm.annotationType.addOption();
    }

    /**
     * Used to remove an option. Should only be called when the value type is 'Select'.
     */
    function optionRemove($index) {
      vm.annotationType.removeOption($index);
    }

    /**
     * Determines if the remove button for an option is disabled.
     *
     * It is disabled only when there is a single option available.
     */
    function removeButtonDisabled() {
      return (vm.annotationType.options.length <= 1);
    }

    /**
     * Called when the user presses the submit button on the form.
     */
    function submit(annotationType) {
      annotationType.maxValueCount = parseInt(annotationType.maxValueCount);
      vm.onSubmit()(annotationType);
    }

    /**
     * Called when the user presses the cancel button on the form.
     */
    function cancel() {
      vm.onCancel()();
    }

  }

  return annotationTypeAddDirective;
});
