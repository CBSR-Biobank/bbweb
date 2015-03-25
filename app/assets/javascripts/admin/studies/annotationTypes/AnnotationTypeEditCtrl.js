define(['underscore'], function(_) {
  'use strict';

  AnnotationTypeEditCtrl.$inject = [
    '$state',
    'domainEntityUpdateError',
    'notificationsService',
    'ParticipantAnnotationType',
    'AnnotationValueType',
    'study',
    'annotType'
  ];

  /**
   * Used for all 3 different study annotation types: collection event, participant and specimen link
   * annotation types.
   *
   * Function 'addOrUpdateFn' is the function that is called when the user submits the form. This fuction
   * should return a promise.
   */
  function AnnotationTypeEditCtrl($state,
                                  domainEntityUpdateError,
                                  notificationsService,
                                  ParticipantAnnotationType,
                                  AnnotationValueType,
                                  study,
                                  annotType) {
    var vm = this,
        action = annotType.isNew() ? 'Add' : 'Update',
        possibleReturnStateNames = [
          'home.admin.studies.study.collection',
          'home.admin.studies.study.participants',
          'home.admin.studies.study.processing'
        ],
        returnState;

    returnState = determineReturnState();

    vm.study                 = study;
    vm.annotType             = annotType;
    vm.title                 =  action + ' Annotation Type';
    vm.hasRequiredField      = (annotType instanceof ParticipantAnnotationType);
    vm.valueTypes            = AnnotationValueType.values();

    vm.valueTypeChange       = valueTypeChange;
    vm.maxValueCountRequired = maxValueCountRequired;
    vm.optionAdd             = optionAdd;
    vm.optionRemove          = optionRemove;
    vm.removeButtonDisabled  = removeButtonDisabled;
    vm.submit                = submit;
    vm.cancel                = cancel;

    vm.annotType.studyId = vm.study.id;

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
      vm.annotType.valueTypeChanged();
    }

    /**
     * Used to disable the submit button if the user has not entered a value for the type
     * of selection: either single selection or multiple selections.
     */
    function maxValueCountRequired() {
      return ! vm.annotType.maxValueCountValid();
    }

    /**
     * Used to add an option. Should only be called when the value type is 'Select'.
     */
    function optionAdd() {
      vm.annotType.addOption();
    }

    /**
     * Used to remove an option. Should only be called when the value type is 'Select'.
     */
    function optionRemove(option) {
      vm.annotType.removeOption(option);
    }

    /**
     * Determines if the remove button for an option is disabled.
     *
     * It is disabled only when there is a single option available.
     */
    function removeButtonDisabled() {
      return (vm.annotType.options.length <= 1);
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState(returnState);
    }

    /**
     * Reloads the state without reloading the form.
     */
    function submitError(error) {
      return domainEntityUpdateError.handleErrorNoStateChange(error, 'study');
    }

    /**
     * Called when the user presses the submit button on the form.
     *
     * Since this form is used for collection event, participant and specimen link annotation types, the
     * function to call to submit the  changes is passed in as a parameter to the controller. It is assumed
     * that this function returns a promise.
     */
    function submit(annotType) {
      annotType.addOrUpdate().then(submitSuccess).catch(submitError);
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
