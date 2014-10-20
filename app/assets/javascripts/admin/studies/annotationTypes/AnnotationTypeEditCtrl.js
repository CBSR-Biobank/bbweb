define(['../../module'], function(module) {
  'use strict';

  module.controller('AnnotationTypeEditCtrl', AnnotationTypeEditCtrl);

  AnnotationTypeEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'stateHelper',
    'domainEntityUpdateError',
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
                                  domainEntityUpdateError,
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

    vm.optionAdd = optionAdd;
    vm.removeOption = removeOption;
    vm.removeButtonDisabled = removeButtonDisabled;
    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function determineReturnState() {
      if (($state.current.name === 'admin.studies.study.collection.ceventAnnotTypeAdd') ||
          ($state.current.name === 'admin.studies.study.collection.ceventAnnotTypeUpdate')) {
        return 'admin.studies.study.collection';
      } else if (($state.current.name === 'admin.studies.study.participants.annotTypeAdd') ||
          ($state.current.name === 'admin.studies.study.participants.annotTypeUpdate')) {
        return 'admin.studies.study.participants';
      } else if (($state.current.name === 'admin.studies.study.processing.spcLinkAnnotTypeAdd') ||
          ($state.current.name === 'admin.studies.study.processing.spcLinkAnnotTypeUpdate')) {
        return 'admin.studies.study.processing';
      }
      throw new Error('invalid state: ' + $state.current.name);
    }

    function gotoReturnState() {
      return stateHelper.reloadStateAndReinit(returnState, $stateParams, {reload: true});
    }

    function optionAdd() {
      if (!vm.annotType.options) {
        vm.annotType.options = [];
      }
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
          domainEntityUpdateError.handleError(error, 'study', returnState);
        });
    }

    function cancel () {
      gotoReturnState();
    }
  }

});
