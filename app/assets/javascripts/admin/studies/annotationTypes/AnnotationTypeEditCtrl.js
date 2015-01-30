define(['../../module'], function(module) {
  'use strict';

  module.controller('AnnotationTypeEditCtrl', AnnotationTypeEditCtrl);

  AnnotationTypeEditCtrl.$inject = [
    '$state',
    'stateHelper',
    'domainEntityUpdateError',
    'notificationsService',
    'study',
    'annotType',
    'addOrUpdateFn',
    'valueTypes'
  ];

  /**
   * Used for all 3 different study annotation types: collection event, participant and specimen link
   * annotation types.
   *
   * Function 'addOrUpdateFn' is the function that is called when the user submits the form. This fuction
   * should return a promise.
   */
  function AnnotationTypeEditCtrl($state,
                                  stateHelper,
                                  domainEntityUpdateError,
                                  notificationsService,
                                  study,
                                  annotType,
                                  addOrUpdateFn,
                                  valueTypes) {
    var vm = this;
    var action = (annotType.id) ? 'Update' : 'Add';
    var returnState = determineReturnState();

    vm.study = study;
    vm.annotType = annotType;
    vm.title =  action + ' Annotation Type';
    vm.hasRequiredField = (typeof annotType.required !== 'undefined');
    vm.valueTypes = valueTypes;
    vm.returnStateParams = {studyId: study.id};

    vm.valueTypeChange = valueTypeChange;
    vm.maxValueCountRequired = maxValueCountRequired;
    vm.optionAdd = optionAdd;
    vm.optionRemove = optionRemove;
    vm.removeButtonDisabled = removeButtonDisabled;
    vm.submit = submit;
    vm.cancel = cancel;

    //--

    /**
     * Determines the state to transition to when the user submits the form or cancels it.
     */
    function determineReturnState() {
      if ($state.current.name.indexOf('home.admin.studies.study.collection') >= 0) {
        return 'home.admin.studies.study.collection';
      } else if ($state.current.name.indexOf('home.admin.studies.study.participants') >= 0) {
        return 'home.admin.studies.study.participants';
      } else if ($state.current.name.indexOf('home.admin.studies.study.processing') >= 0) {
        return 'home.admin.studies.study.processing';
      }
      throw new Error('invalid state: ' + $state.current.name);
    }

    /**
     * Transitions to the return state.
     */
    function gotoReturnState() {
      return stateHelper.reloadStateAndReinit(returnState, vm.returnStateParams, {reload: true});
    }

    /**
     * Called when the user changes the annotation type's value type.
     */
    function valueTypeChange() {
      if (vm.annotType.valueType === 'Select') {
        // add an option if none exist
        if (!vm.annotType.options || (vm.annotType.options.length < 1)) {
          optionAdd();
        }
      } else {
        vm.annotType.options = undefined;
        vm.annotType.maxValueCount = 0;
      }
    }

    /**
     * Used to disable the submit button if the user has not entered a value for the type
     * of selection: either single selection or multiple selections.
     */
    function maxValueCountRequired() {
      return ((vm.annotType.maxValueCount < 1) || (vm.annotType.maxValueCount > 2));
    }

    /**
     * Used to add an option. Should only be called when the value type is 'Select'.
     */
    function optionAdd() {
      if (vm.annotType.valueType !== 'Select') {
        throw new Error('value type error: ' + vm.annotType.valueType);
      }

      if (!vm.annotType.options) {
        vm.annotType.options = [];
      }
      vm.annotType.options.push('');
    }

    /**
     * Used to remove an option. Should only be called when the value type is 'Select'.
     */
    function optionRemove(option) {
      if (vm.annotType.options.length <= 1) {
        throw new Error('invalid length for options');
      }

      var index = vm.annotType.options.indexOf(option);
      if (index > -1) {
        vm.annotType.options.splice(index, 1);
      }
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
      gotoReturnState();
    }

    /**
     * Called when the user presses the submit button on the form.
     *
     * Since this form is used for collection event, participant and specimen link annotation types, the
     * function to call to submit the  changes is passed in as a parameter to the controller. It is assumed
     * that this function returns a promise.
     */
    function submit(annotType) {
      addOrUpdateFn(annotType)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(error, 'study', returnState);
        });
    }

    /**
     * Called when the user presses the cancel button on the form.
     */
    function cancel() {
      gotoReturnState();
    }
  }

});
