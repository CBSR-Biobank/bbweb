/** Study service */
define(['../module'], function(module) {
  'use strict';

  /**
   * Called to edit or update a study.
   */
  module.controller('StudyEditCtrl', StudyEditCtrl);

  StudyEditCtrl.$inject = [
    '$scope', '$state', 'stateHelper', 'StudyService', 'modelObjUpdateError', 'user', 'study', 'returnState',
  ];

  function StudyEditCtrl($scope, $state, stateHelper, StudyService, modelObjUpdateError, user, study, returnState) {
    var action = (study.id) ? 'Update' : 'Add';
    $scope.title =  action + ' study';
    $scope.study = study;
    $scope.submit = submit;
    $scope.cancel = cancel;

    //--

    function gotoReturnState() {
      var params = returnState.params || {};
      var options = returnState.options || {};
      return $state.go(returnState.name, params, options);
    }

    function submit(study) {
      StudyService.addOrUpdate(study)
        .then(gotoReturnState)
        .catch(function(error) {
          modelObjUpdateError.handleError(
            error,
            'study',
            returnState.name,
            returnState.params,
            returnState.options);
        });
    }

    function cancel() {
      gotoReturnState();
    }
  }

});

