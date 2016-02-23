/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  AnnotationTypeEditCtrl.$inject = [
    '$state',
    'domainEntityService',
    'notificationsService',
    'AnnotationType',
    'AnnotationValueType',
    'study'
  ];

  /**
   * Used for all 3 different study annotation types: collection event, participant and specimen link
   * annotation types.
   *
   * Function 'addOrUpdateFn' is the function that is called when the user submits the form. This fuction
   * should return a promise.
   */
  function AnnotationTypeEditCtrl($state,
                                  domainEntityService,
                                  notificationsService,
                                  AnnotationType,
                                  AnnotationValueType,
                                  study) {
    var vm = this,
        possibleReturnStateNames = [
          'home.admin.studies.study.collection.view',
          'home.admin.studies.study.participants',
          'home.admin.studies.study.processing'
        ],
        returnState;

    returnState = determineReturnState();

    vm.study                 = study;
    vm.annotationType        = new AnnotationType();
    vm.title                 = 'Add Annotation Type';
    vm.valueTypes            = AnnotationValueType.values();

    vm.valueTypeChange       = valueTypeChange;
    vm.maxValueCountRequired = maxValueCountRequired;
    vm.optionAdd             = optionAdd;
    vm.optionRemove          = optionRemove;
    vm.removeButtonDisabled  = removeButtonDisabled;
    vm.submit                = submit;
    vm.cancel                = cancel;

    vm.annotationType.studyId = vm.study.id;

    //--

    /**
     * Determines the state to transition to when the user submits the form or cancels it.
     */
    function determineReturnState() {
      var returnStateName = _.filter(possibleReturnStateNames, function(name) {
        return ($state.current.name.indexOf(name) >= 0);
      });

      if (returnStateName.length !== 1) {
        throw new Error('invalid current state name: ' + $state.current.name);
      }

      return {
        name:    _.first(returnStateName),
        params:  { studyId: study.id },
        options: { reload: true }
      };
    }

    /**
     * Transitions to the return state.
     */
    function gotoReturnState(state) {
      return $state.go(state.name, state.params, state.options);
    }

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
    function optionRemove(option) {
      vm.annotationType.removeOption(option);
    }

    /**
     * Determines if the remove button for an option is disabled.
     *
     * It is disabled only when there is a single option available.
     */
    function removeButtonDisabled() {
      return (vm.annotationType.options.length <= 1);
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState(returnState);
    }

    /**
     * Reloads the state without reloading the form.
     */
    function submitError(error) {
      return domainEntityService.updateErrorModal(error, 'study');
    }

    /**
     * Called when the user presses the submit button on the form.
     *
     * Since this form is used for collection event, participant and specimen link annotation types, the
     * function to call to submit the  changes is passed in as a parameter to the controller. It is assumed
     * that this function returns a promise.
     */
    function submit(annotationType) {
      annotationType.addOrUpdate().then(submitSuccess).catch(submitError);
    }

    /**
     * Called when the user presses the cancel button on the form.
     */
    function cancel() {
      gotoReturnState(returnState);
    }
  }

  return AnnotationTypeEditCtrl;
});
