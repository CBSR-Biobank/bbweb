define(['../../module'], function(module) {
  'use strict';

  module.controller('AnnotationTypeEditCtrl', AnnotationTypeEditCtrl);

  AnnotationTypeEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'stateHelper',
    'modelObjUpdateError',
    'study',
    'annotType',
    'addOrUpdateFn',
    'valueTypes'
  ];

  /** Used for all 3 different study annotation types.
   */
  function AnnotationTypeEditCtrl($state,
                                  $stateParams,
                                  stateHelper,
                                  modelObjUpdateError,
                                  study,
                                  annotType,
                                  addOrUpdateFn,
                                  valueTypes) {
    var vm = this;
    var action = (annotType.id) ? 'Update' : 'Add';
    var returnState = $state.current.data.returnState;

    vm.study = study;
    vm.annotType = annotType;
    vm.title =  action + ' Annotation Type';
    vm.hasRequiredField = (typeof annotType.required !== 'undefined');
    vm.valueTypes = valueTypes;

    vm.optionAdd = optionAdd;
    vm.removeOption = removeOption;
    vm.removeButtonDisabled = removeButtonDisabled;
    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      var stateParam = {};
      if (annotType.studyId) {
        stateParam.studyId = annotType.studyId;
      }
      return stateHelper.reloadStateAndReinit(returnState, stateParam, {reload: true});
    }

    function optionAdd() {
      vm.annotType.options.push('');
    }

    function removeOption(option) {
      if (vm.annotType.options.length <= 1) {
        throw new Error('invalid length for options');
      }

      var index = vm.annotType.options.indexOf(option);
      if (index > -1) {
        vm.annotType.options.splice(index, 1);
      }
    }

    function removeButtonDisabled() {
      return vm.annotType.options.length <= 1;
    }

    function submit (annotType) {
      addOrUpdateFn(annotType)
        .then(gotoReturnState)
        .catch(function(error) {
          modelObjUpdateError.handleError(error, 'study', returnState);
        });
    }

    function cancel () {
      gotoReturnState();
    }
  }

});
